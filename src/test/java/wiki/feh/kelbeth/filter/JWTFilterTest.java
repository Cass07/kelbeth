package wiki.feh.kelbeth.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wiki.feh.kelbeth.helper.TestConstants;
import wiki.feh.kelbeth.jwt.facade.TokenAuthFacade;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class JWTFilterTest {
	@InjectMocks
	JWTFilter jwtFilter;

	@Mock
	TokenAuthFacade tokenAuthFacade;

	@Mock
	GatewayFilterChain mockFilterChain;

	@DisplayName("bearer header missing - UNAUTHORIZED 리턴해야함")
	@Test
	void testApplyBearerHeaderMissing() {
		// Given
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		// When
		GatewayFilter filter = jwtFilter.apply(new JWTFilter.Config());

		Mono<Void> result = filter.filter(exchange, mockFilterChain);

		// Then
		StepVerifier.create(result)
				.expectComplete()
				.verify();

		assertThat(Objects.requireNonNull(exchange.getResponse().getStatusCode())).isEqualTo(HttpStatus.UNAUTHORIZED);
		verifyNoInteractions(mockFilterChain);
	}

	@DisplayName("bearer header present - tokenAuthFacade.validateAndParseToken fail - UNAUTHORIZED")
	@Test
	void testApplyBearerHeaderPresentTokenAuthFail() {
		// Given
		String token = "invalidAccessToken";
		MockServerHttpRequest request = MockServerHttpRequest.get("/test")
				.header("Authorization", "Bearer " + token)
				.build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		doReturn(Mono.error(new IllegalArgumentException("Invalid token"))).when(tokenAuthFacade).validateAndParseToken(token);

		// When
		GatewayFilter filter = jwtFilter.apply(new JWTFilter.Config());

		Mono<Void> result = filter.filter(exchange, mockFilterChain);

		// Then
		StepVerifier.create(result)
			.expectComplete()
			.verify();

		//validateAndParseToken 호출 검증
		verify(tokenAuthFacade, times(1)).validateAndParseToken(token);

		assertThat(Objects.requireNonNull(exchange.getResponse().getStatusCode())).isEqualTo(HttpStatus.UNAUTHORIZED);
		verifyNoInteractions(mockFilterChain);
	}

	@DisplayName("bearer header present - 토큰 인증 성공 - filterChain 호출")
	@Test
	void testApplyBearerHeaderPresentTokenAuthSuccess() {
		// Given
		String token = "validAccessToken";
		MockServerHttpRequest request = MockServerHttpRequest.get("/test")
			.header("Authorization", "Bearer " + token)
			.build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		doReturn(Mono.just(TestConstants.ACCESS_CLAIM_DTO)).when(tokenAuthFacade).validateAndParseToken(token);

		// When
		GatewayFilter filter = jwtFilter.apply(new JWTFilter.Config());

		Mono<Void> result = filter.filter(exchange, mockFilterChain);

		// Then
		StepVerifier.create(result)
			.expectComplete()
			.verify();

		//validateAndParseToken 호출 검증
		verify(tokenAuthFacade, times(1)).validateAndParseToken(token);

		// Captor로 exchange 객체를 캡처하여 filterChain.filter 호출 시 전달된 exchange와 헤더 검증
		ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
		verify(mockFilterChain, times(1)).filter(exchangeCaptor.capture());

		ServerWebExchange capturedExchange = exchangeCaptor.getValue();
		assertThat(capturedExchange.getRequest().getHeaders().getFirst("X-Auth-UserId")).isEqualTo(TestConstants.ACCESS_CLAIMS.get("userId"));
		assertThat(capturedExchange.getRequest().getHeaders().getFirst("X-Auth-SessionId")).isEqualTo(TestConstants.ACCESS_CLAIMS.get("sid"));
		assertThat(capturedExchange.getRequest().getHeaders().getFirst("X-Auth-Jti")).isEqualTo(TestConstants.ACCESS_CLAIMS.get("jti"));
	}


}