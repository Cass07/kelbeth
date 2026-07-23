package wiki.feh.kelbeth.tokenapi.facade;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import wiki.feh.kelbeth.tokenapi.domain.APIRefreshToken;
import wiki.feh.kelbeth.tokenapi.dto.TokenPairDto;
import wiki.feh.kelbeth.tokenapi.dto.TokenStringPairDto;
import wiki.feh.kelbeth.tokenapi.exception.RedisSetFailedException;
import wiki.feh.kelbeth.tokenapi.exception.SessionNotFoundException;
import wiki.feh.kelbeth.tokenapi.exception.TokenPairCacheNotFoundException;
import wiki.feh.kelbeth.tokenapi.service.TokenAPIAuthService;
import wiki.feh.kelbeth.tokenapi.service.TokenAPIRedisCacheService;

@Component
@RequiredArgsConstructor
public class TokenAPIFacade {
	private final TokenAPIRedisCacheService tokenAPIRedisCacheService;
	private final TokenAPIAuthService tokenAPIAuthService;

	public Mono<TokenStringPairDto> login(String userId) {
		APIRefreshToken refreshToken = new APIRefreshToken(userId);
		TokenPairDto tokenPairDto = refreshToken.regenerate();

		LocalDateTime issuedAt = LocalDateTime.now();

		return Mono.just(tokenPairDto)
			.flatMap(tokenPair -> {
				// Refresh Token Redis에 저장
				APIRefreshToken rt = tokenPair.refreshToken();
				return tokenAPIRedisCacheService.setValue(rt.getSessionId(), rt.getJti(),
					Duration.ofMillis(rt.getDurationMilli()));
			})
			.filter(Boolean.TRUE::equals)
			.switchIfEmpty(Mono.error(new RedisSetFailedException("Failed to save refresh token in Redis")))
			.map(_ -> tokenAPIAuthService.generateTokenStringPair(tokenPairDto, issuedAt));
	}

	public Mono<String> logout(String refreshToken) {
		APIRefreshToken apiRefreshToken = tokenAPIAuthService.parseRefreshToken(refreshToken);
		String sessionId = apiRefreshToken.getSessionId();
		return tokenAPIRedisCacheService.deleteValue(sessionId)
			.filter(Boolean.TRUE::equals)
			.switchIfEmpty(Mono.error(new RedisSetFailedException("Failed to delete refresh token from Redis")))
			.thenReturn(apiRefreshToken.getUserId());
	}

	public Mono<TokenStringPairDto> refresh(String refreshToken) {
		APIRefreshToken apiRefreshToken = tokenAPIAuthService.parseRefreshToken(refreshToken);
		String sessionId = apiRefreshToken.getSessionId();
		String jti = apiRefreshToken.getJti();

		return tokenAPIRedisCacheService.getValue(sessionId)
			// session id 조회 결과가 없다면 로그인 세션이 만료
			.switchIfEmpty(Mono.error(new SessionNotFoundException()))
			// jti가 일치하면 refresh 가능한 상태로, 락 선점 시도
			.filter(storedJti -> storedJti.equals(jti))
			.flatMap(_ -> getJtiLockAndGenerateTokenPair(apiRefreshToken))
			// jti가 일치하지 않거나 없으면 세션 삭제
			.switchIfEmpty(Mono.defer(() -> deleteInvalidSessionAndError(sessionId)));
	}

	private Mono<TokenStringPairDto> getJtiLockAndGenerateTokenPair(APIRefreshToken apiRefreshToken) {
		return getJtiLockFromRedis(apiRefreshToken.getJti())
			.filter(Boolean.TRUE::equals)
			.flatMap(_ -> generateNewTokenPairAndSaveToRedis(apiRefreshToken, apiRefreshToken.getJti()))
			.switchIfEmpty(Mono.defer(() -> readTokenStringPairFromRedis(apiRefreshToken.getJti())));
	}

	private Mono<TokenStringPairDto> deleteInvalidSessionAndError(String sessionId) {
		return tokenAPIRedisCacheService.deleteValue(sessionId)
			.then(Mono.error(new SessionNotFoundException()));
	}

	// 90ms 동안 jti lock을 Redis에 저장하고, 성공하면 true 반환, 실패하면 false 반환
	private Mono<Boolean> getJtiLockFromRedis(String jti) {
		return tokenAPIRedisCacheService.setValueIfAbsent(jti, "LOCK", Duration.ofMillis(90));
	}

	/**
	 * 새로운 토큰 페어를 생성하고 Redis에 저장하는 메서드
	 * @param apiRefreshToken refresh token 객체
	 * @return TokenStringPairDto
	 */
	private Mono<TokenStringPairDto> generateNewTokenPairAndSaveToRedis(APIRefreshToken apiRefreshToken, String oldJti) {
		TokenPairDto newTokenPair = apiRefreshToken.regenerate();
		LocalDateTime issuedAt = LocalDateTime.now();
		TokenStringPairDto tokenStringPairDto = tokenAPIAuthService.generateTokenStringPair(newTokenPair, issuedAt);
		return tokenAPIRedisCacheService.setValue(newTokenPair.refreshToken().getSessionId(),
				newTokenPair.refreshToken().getJti(),
				Duration.ofMillis(newTokenPair.refreshToken().getDurationMilli()))
			.filter(Boolean.TRUE::equals)
			.switchIfEmpty(Mono.error(new RedisSetFailedException("Failed to save new refresh token in Redis")))
			.flatMap(_ -> tokenAPIRedisCacheService.setTokenStringPair(oldJti, tokenStringPairDto))
			.filter(Boolean.TRUE::equals)
			.switchIfEmpty(Mono.error(new RedisSetFailedException("Failed to save token string pair in Redis")))
			.thenReturn(tokenStringPairDto);
	}

	private Mono<TokenStringPairDto> readTokenStringPairFromRedis(String jti) {
		// race condition 방지를 위해 500ms 지연 후 Redis 에서 토큰 페어를 읽어옴
		return Mono.delay(Duration.ofMillis(500))
			.flatMap(_ -> tokenAPIRedisCacheService.getValue(jti))
			.switchIfEmpty(Mono.error(new TokenPairCacheNotFoundException()))
			.map(TokenStringPairDto::fromJson);
	}

}
