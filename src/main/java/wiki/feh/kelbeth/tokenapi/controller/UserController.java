package wiki.feh.kelbeth.tokenapi.controller;

import java.time.Duration;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

	private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

	@PostMapping("/route/user/refresh")
	public Mono<Object> refreshToken(
		@CookieValue(value = "refreshToken", required = false) String refreshToken,
		ServerWebExchange exchange
	) {
		if (refreshToken == null) {
			return Mono.just("Refresh token is missing");
		}

		return tokenAPIFacade.refresh(refreshToken)
			.map(tp -> {
				setRefreshTokenCookies(exchange, tp.refreshToken());
				return tp;
			});
	}

	@PostMapping("/user/login")
	public Mono<TokenStringPairDto> login(
		@RequestBody TokenAPIDto.LoginRequest body,
		ServerWebExchange exchange
	) {
		return tokenAPIFacade.login(body.getUserId())
			.map(tokenPair -> {
				setRefreshTokenCookies(exchange, tokenPair.refreshToken());
				return tokenPair;
			});
	}

	@PostMapping("/route/user/logout")
	public Mono<String> logout(
		@CookieValue(value = "refreshToken", required = false) String refreshToken,
		ServerWebExchange exchange
	) {
		return tokenAPIFacade.logout(refreshToken)
			.doFinally(_ -> deleteRefreshTokenCookies(exchange))
			.map(userId -> "User " + userId + " logged out successfully")
			.onErrorReturn("Logout failed: " + refreshToken);
	}

	private void setRefreshTokenCookies(ServerWebExchange exchange, String refreshToken) {
		addCookie(exchange, REFRESH_TOKEN_COOKIE_NAME, refreshToken, Duration.ofDays(7), "/user/refresh");
		addCookie(exchange, REFRESH_TOKEN_COOKIE_NAME, refreshToken, Duration.ofDays(7), "/user/logout");
	}

	private void deleteRefreshTokenCookies(ServerWebExchange exchange) {
		addCookie(exchange, REFRESH_TOKEN_COOKIE_NAME, "", Duration.ZERO, "/user/refresh");
		addCookie(exchange, REFRESH_TOKEN_COOKIE_NAME, "", Duration.ZERO, "/user/logout");
	}

	private void addCookie(ServerWebExchange exchange, String name, String value, Duration maxAge, String path) {
		exchange.getResponse().addCookie(
			org.springframework.http.ResponseCookie.from(name, value)
				.httpOnly(true)
				// localhost 환경 테스트 용으로 secure와 sameSite 쿠키 옵션은 주석 처리
				//.secure(true)
				//.sameSite("Strict")
				.path(path)
				.maxAge(maxAge)
				.build());
	}
}
