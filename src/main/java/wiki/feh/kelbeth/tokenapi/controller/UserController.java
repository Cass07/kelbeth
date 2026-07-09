package wiki.feh.kelbeth.tokenapi.controller;

import java.time.Duration;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import wiki.feh.kelbeth.tokenapi.dto.TokenAPIDto;
import wiki.feh.kelbeth.tokenapi.dto.TokenStringPairDto;
import wiki.feh.kelbeth.tokenapi.facade.TokenAPIFacade;

@RequiredArgsConstructor
@RestController
public class UserController {

	private final TokenAPIFacade tokenAPIFacade;

	@PostMapping("/route/user/refresh")
	public Mono<Object> refreshToken(
		@CookieValue(value = "refreshToken", required = false) String refreshToken,
		ServerWebExchange exchange
	) {
		if (refreshToken == null) {
			return Mono.just("Refresh token is missing");
		}

		return tokenAPIFacade.refresh(refreshToken)
			.flatMap(tp -> {
				exchange.getResponse().addCookie(
					org.springframework.http.ResponseCookie.from("refreshToken", tp.refreshToken())
						.httpOnly(true)
						// localhost 환경 테스트 용으로 secure와 sameSite 쿠키 옵션은 주석 처리
						//.secure(true)
						//.sameSite("Strict")
						.path("/user/refresh")
						.maxAge(Duration.ofDays(7))
						.build());
				return Mono.just(tp);
			});
	}

	@PostMapping("/user/login")
	public Mono<TokenStringPairDto> login(
		@RequestBody TokenAPIDto.LoginRequest body,
		ServerWebExchange exchange
	) {
		return tokenAPIFacade.login(body.getUserId())
			.flatMap(tokenPair -> {
				exchange.getResponse().addCookie(
					org.springframework.http.ResponseCookie.from("refreshToken", tokenPair.refreshToken())
						.httpOnly(true)
						// localhost 환경 테스트 용으로 secure와 sameSite 쿠키 옵션은 주석 처리
						//.secure(true)
						//.sameSite("Strict")
						.path("/user/refresh")
						.maxAge(Duration.ofDays(7))
						.build());
				return Mono.just(tokenPair);
			});
	}

	@PostMapping("/route/user/logout")
	public Mono<String> logout(
		@RequestHeader("X-Auth-UserId") String userId,
		@RequestHeader("X-Auth-SessionId") String sessionId,
		@RequestHeader("X-Auth-Jti") String jti
	) {
		return tokenAPIFacade.logout(sessionId)
			.map(deleted -> {
				if (Boolean.TRUE.equals(deleted)) {
					return "Logout successful for userId: " + userId;
				} else {
					return "Logout failed for userId: " + userId;
				}
			});
	}
}
