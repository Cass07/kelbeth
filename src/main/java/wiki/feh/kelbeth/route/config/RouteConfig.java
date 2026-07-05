package wiki.feh.kelbeth.route.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import wiki.feh.kelbeth.filter.GlobalFilter;

@RequiredArgsConstructor
@Configuration
public class RouteConfig {
	private final GlobalFilter globalFilter;

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		// 테스트용으로 일단 깃헙 아무 프로젝트 파일 연결시켜놓음
		return builder.routes()
				.route("route_test", r -> r.path( "/routeTest/**")
					.filters(f -> f
						.setPath("/Cass07/FgoCalc/master/Data/updateDate.txt")
						.filter(globalFilter.apply(new GlobalFilter.Config())))
						.uri("https://raw.githubusercontent.com"))
				.build();
	}
}
