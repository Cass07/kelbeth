package wiki.feh.kelbeth.filter;

import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;

@NullMarked
@Slf4j
@Component
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {

	public GlobalFilter() {
		super(Config.class);
	}

	public static class Config {
	}

	@Override
	public org.springframework.cloud.gateway.filter.GatewayFilter apply(Config config) {
		return (ServerWebExchange exchange, GatewayFilterChain chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			ServerHttpResponse response = exchange.getResponse();

			// 요청 처리 전 로직
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			log.info("[GlobalFilter] Request Path: {}", request.getPath());

			return chain.filter(exchange)
				.onErrorResume(throwable -> {
					// 예외 처리 로직
					log.error("[GlobalFilter] Exception occurred: {}", throwable.getMessage());
					response.setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
					return response.setComplete();
				})
				.doFinally(_ -> {
					// 응답 처리 후 로직
					stopWatch.stop();
					log.info("[GlobalFilter] Response Status Code: {}", response.getStatusCode());
					log.info("[GlobalFilter] Request processing time: {} ms", stopWatch.getTotalTimeMillis());
				});
		};
	}
}
