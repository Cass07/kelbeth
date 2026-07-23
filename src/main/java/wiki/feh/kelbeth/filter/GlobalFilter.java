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

	private static final String HEADER_REQUEST_ID = "X-Request-ID";

	@Override
	public org.springframework.cloud.gateway.filter.GatewayFilter apply(Config config) {
		return (ServerWebExchange exchange, GatewayFilterChain chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			ServerHttpResponse response = exchange.getResponse();

			// 요청 처리 전 로직
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();

			// requestId 생성, 존재하면 그대로 사용
			String requestIdHeader = request.getHeaders().getFirst(HEADER_REQUEST_ID);
			String requestId = (requestIdHeader != null && !requestIdHeader.isEmpty()) ? requestIdHeader :
				java.util.UUID.randomUUID().toString();

			log.info("[GlobalFilter] ID: {} Request Path: {}", requestId, request.getPath());

			// header에 requestId 추가
			var mutatedRequest = request.mutate()
				.header(HEADER_REQUEST_ID, requestId)
				.build();

			var mutatedExchange = exchange.mutate()
				.request(mutatedRequest)
				.build();

			return chain.filter(mutatedExchange)
				.onErrorResume(throwable -> {
					// 필터에서 예상 못 한 예외 발생했을때 처리 로직
					log.error("[GlobalFilter] ID: {} Exception occurred: {}", requestId, throwable.getMessage());
					response.setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
					return response.setComplete();
				})
				.doFinally(_ -> {
					// 응답 처리 후 로직
					stopWatch.stop();
					log.info("[GlobalFilter] ID: {} Response Status Code: {}", requestId, response.getStatusCode());
					log.info("[GlobalFilter] ID: {} Request processing time: {} ms", requestId,
						stopWatch.getTotalTimeMillis());
					exchange.getResponse().getHeaders().add(HEADER_REQUEST_ID, requestId);
				});
		};
	}

	public static class Config {
	}
}
