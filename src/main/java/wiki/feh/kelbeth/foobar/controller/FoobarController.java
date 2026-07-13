package wiki.feh.kelbeth.foobar.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class FoobarController {

	/**
	 * JWTFilter의 로그인 인증 테스트용 엔드포인트
	 */
	@GetMapping("/route/foobar")
	public Mono<String> foobar(
		@RequestHeader("X-Auth-UserId") String userId
	) {
		return Mono.just("Hello, " + userId + "!");
	}
}
