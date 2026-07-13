package wiki.feh.kelbeth.jwt.facade;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import wiki.feh.kelbeth.jwt.dto.TokenClaimDto;
import wiki.feh.kelbeth.jwt.service.RedisSessionCacheService;
import wiki.feh.kelbeth.jwt.service.TokenAuthService;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenAuthFacade {
	private final TokenAuthService tokenAuthService;
	private final RedisSessionCacheService redisSessionCacheService;

	// Token Validation함
	// sessionId, jtl을 통해 Redis에 존재하는지 확인함
	// 존재한다면 그대로 dto 리턴
	// 존재하지 않는다면 로그인 세션이 없으므로 인증 실패함
	// 존재하는데 jtl이 일치하지 않는다면 redis 데이터를 삭제하고 로그인 실패함

	public Mono<TokenClaimDto> validateAndParseToken(String token) {
		// 토큰 검증과 파싱
		return Mono.fromCallable(() -> tokenAuthService.validateAndParseAccessToken(token))
			.doOnNext(dto -> log.info("Token validated successfully for userId: {}, sessionId: {}, jti: {}", dto.userId(), dto.sessionId(), dto.jti()))
			.flatMap(claimDto ->
				// Redis에서 세션 확인
				redisSessionCacheService.getValue(claimDto.sessionId())
					// 조회된 세션이 존재하지 않으면 에러 반환
					.switchIfEmpty(Mono.error(new RuntimeException("Session not found")))
					// 세션이 존재할 경우 jti를 비교하여 일치하지 않으면 세션 삭제 후 에러 반환
					.flatMap(sessionCache -> {
						if (!sessionCache.getJti().equals(claimDto.jti())) {
							return redisSessionCacheService.deleteValue(claimDto.sessionId())
								.then(Mono.error(new RuntimeException("Invalid session jti")));
						}
						return Mono.just(claimDto);
					})
			);
	}
}
