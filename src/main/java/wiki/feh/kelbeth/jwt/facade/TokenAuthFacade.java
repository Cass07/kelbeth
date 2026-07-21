package wiki.feh.kelbeth.jwt.facade;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import wiki.feh.kelbeth.jwt.dto.TokenClaimDto;
import wiki.feh.kelbeth.jwt.service.TokenAuthService;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenAuthFacade {
	private final TokenAuthService tokenAuthService;

	// Token Validation함
	// 존재한다면 그대로 dto 리턴

	public Mono<TokenClaimDto> validateAndParseToken(String token) {
		// 토큰 검증과 파싱
		return Mono.fromCallable(() -> tokenAuthService.validateAndParseAccessToken(token))
			.doOnNext(dto -> log.info("Token validated successfully for userId: {}, jti: {}", dto.userId(), dto.jti()));
	}
}
