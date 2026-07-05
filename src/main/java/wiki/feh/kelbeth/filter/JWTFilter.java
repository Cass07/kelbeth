package wiki.feh.kelbeth.filter;

import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wiki.feh.kelbeth.jwt.util.JWTManagerV1;

@RequiredArgsConstructor
@Slf4j
@Component
public class JWTFilter extends AbstractGatewayFilterFactory<JWTFilter.Config> {
	private final JWTManagerV1 jwtManager;

	public static class Config {
		// Put the configuration properties for your filter here
	}

	@Override
	public org.springframework.cloud.gateway.filter.GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			String token = exchange.getRequest().getHeaders().getFirst("Authorization");
			if (token != null && token.startsWith("Bearer ")) {
				token = token.substring(7);
				try {
					if (!jwtManager.validateToken(token)) {
						log.warn("Invalid JWT token");
						exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
						return exchange.getResponse().setComplete();
					}
				} catch (Exception e) {
					log.error("Error validating JWT token", e);
					exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
					return exchange.getResponse().setComplete();
				}
			} else {
				log.warn("Missing or invalid Authorization header");
				exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			}
			return chain.filter(exchange);
		};
	}


}
