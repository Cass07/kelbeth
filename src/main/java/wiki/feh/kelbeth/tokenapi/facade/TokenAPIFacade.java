package wiki.feh.kelbeth.tokenapi.facade;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import wiki.feh.kelbeth.tokenapi.domain.APIRefreshToken;
import wiki.feh.kelbeth.tokenapi.dto.TokenPairDto;
import wiki.feh.kelbeth.tokenapi.dto.TokenStringPairDto;
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
				return tokenAPIRedisCacheService.setValue(rt.getSessionId(), rt.getJti(), Duration.ofMillis(rt.getDurationMilli()) );
			})
			.map(saved -> {
				if (Boolean.FALSE.equals(saved)) {
					throw new RuntimeException("Failed to save refresh token in Redis");
				}
				return tokenPairDto;
			})
			.map(tokenPair -> tokenAPIAuthService.generateTokenStringPair(tokenPair, issuedAt));
	}

	public Mono<Boolean> logout(String sessionId) {
		return tokenAPIRedisCacheService.deleteValue(sessionId)
			.map(deleted -> {
				if (Boolean.FALSE.equals(deleted)) {
					throw new RuntimeException("Failed to delete refresh token from Redis");
				}
				return deleted;
			});
	}

	public Mono<TokenStringPairDto> refresh(String refreshToken) {
		// String Token으로부터 APIRefreshToken 객체를 생성 (이 떄 유효성 체크 같이 됨)
		APIRefreshToken apiRefreshToken = tokenAPIAuthService.parseRefreshToken(refreshToken);
		String sessionId = apiRefreshToken.getSessionId();
		String jti = apiRefreshToken.getJti();

		// APIRefreshToken 객체를 사용하여 Redis에서 해당 세션을 확인하고 유효성을 검증
		// Redisdㅔ서 세션을 검색한 value 값이 현재 토큰의 jti와 일치하는지 확인
		// 일치한다면, 새로운 토큰 페어를 생성하고, Redis에 새로운 세션 정보를 갱신

		// 일치하지 않는다면, redis에 저장된 세션을 삭제하고, 유효하지 않은 토큰으로 처리

		return tokenAPIRedisCacheService.getValue(sessionId)
			.flatMap(storedJti -> {
				if (storedJti == null || !storedJti.equals(jti)) {
					// Redis에 저장된 jti가 없거나 일치하지 않으면 세션 삭제
					return tokenAPIRedisCacheService.deleteValue(sessionId)
						.then(Mono.error(new RuntimeException("Invalid refresh token")));
				} else {
					// Redis에 저장된 jti가 일치하면 새로운 토큰 페어 생성 및 Redis 갱신
					TokenPairDto newTokenPair = apiRefreshToken.regenerate();
					LocalDateTime issuedAt = LocalDateTime.now();
					return tokenAPIRedisCacheService.setValue(newTokenPair.refreshToken().getSessionId(),
							newTokenPair.refreshToken().getJti(),
							Duration.ofMillis(newTokenPair.refreshToken().getDurationMilli()))
						.map(saved -> {
							if (Boolean.FALSE.equals(saved)) {
								throw new RuntimeException("Failed to save new refresh token in Redis");
							}
							return tokenAPIAuthService.generateTokenStringPair(newTokenPair, issuedAt);
						});
				}
			});

	}

}
