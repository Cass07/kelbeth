package wiki.feh.kelbeth.filter;

import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wiki.feh.kelbeth.jwt.facade.TokenAuthFacade;

@RequiredArgsConstructor
@NullMarked
@Slf4j
@Component
public class JWTFilter extends AbstractGatewayFilterFactory<JWTFilter.Config> {
	private static final String HEADER_AUTH_USER_ID = "X-Auth-UserId";
	private static final String HEADER_AUTH_JTI = "X-Auth-Jti";

	private final TokenAuthFacade tokenAuthFacade;

	public static class Config {
		// Put the configuration properties for your filter here
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				log.warn("Missing or invalid Authorization header");
				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			}

			String token = authHeader.substring(7);

			return tokenAuthFacade.validateAndParseToken(token)
				.flatMap(claimDto -> {
					String userId = claimDto.userId();
					String jti = claimDto.jti();

					log.info("JWT claims: {} {}", userId, jti);

					var mutatedRequest = exchange.getRequest().mutate()
						.headers(headers -> {
							// 클라이언트가 임의로 넣은 내부 헤더 제거
							headers.remove(HEADER_AUTH_USER_ID);
							headers.remove(HEADER_AUTH_JTI);

							// Gateway가 검증 후 신뢰 가능한 값만 재주입
							headers.add(HEADER_AUTH_USER_ID, userId);
							headers.add(HEADER_AUTH_JTI, jti);
						})
						.build();

					var mutatedExchange = exchange.mutate()
						.request(mutatedRequest)
						.build();

					return chain.filter(mutatedExchange);
				})
				.onErrorResume(e -> {
					log.warn("Invalid JWT token: {}", e.getMessage());
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					return exchange.getResponse().setComplete();
				});
		};
	}


}
