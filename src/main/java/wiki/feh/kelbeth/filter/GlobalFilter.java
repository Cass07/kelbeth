package wiki.feh.kelbeth.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Order(-1)
@Component
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {

	private final StopWatch stopWatch;

	public GlobalFilter() {
		super(Config.class);
		this.stopWatch = new StopWatch();
	}

	public static class Config {
	}

	@Override
	public org.springframework.cloud.gateway.filter.GatewayFilter apply(Config config) {
		return (ServerWebExchange exchange, GatewayFilterChain chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			ServerHttpResponse response = exchange.getResponse();

			// 요청 처리 전 로직
			stopWatch.start();
			log.info("[GlobalFilter] Request Path: {}", request.getPath());

			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				// 응답 처리 후 로직
				stopWatch.stop();
				log.info("[GlobalFilter] Response Status Code: {}", response.getStatusCode());
				log.info("[GlobalFilter] Request processing time: {} ms", stopWatch.getTotalTimeMillis());
			}));
		};
	}
}
