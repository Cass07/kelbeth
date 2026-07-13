package wiki.feh.kelbeth.route.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import wiki.feh.kelbeth.filter.GlobalFilter;
import wiki.feh.kelbeth.filter.JWTFilter;

@RequiredArgsConstructor
@Configuration
public class RouteConfig {
	private final GlobalFilter globalFilter;
	private final JWTFilter jwtFilter;

	private static final String LOCAL_URL = "http://localhost:8080";

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		// 테스트용으로 일단 깃헙 아무 프로젝트 파일 연결시켜놓음
		return builder.routes()
			.route("route_test", r -> r.path("/routeTest/**")
				.filters(f -> f
					.setPath("/Cass07/FgoCalc/master/Data/updateDate.txt")
					.filter(globalFilter.apply(new GlobalFilter.Config())))
				.uri("https://raw.githubusercontent.com"))
			.route("route_user_refresh", r -> r.path("/user/refresh")
				.filters(f -> f
					.rewritePath("/(?<path>.*)", "/route/${path}")
					.filter(globalFilter.apply(new GlobalFilter.Config())))
				.uri(LOCAL_URL))
			.route("route_user_logout", r -> r.path("/user/logout")
				.filters(f -> f
					.setPath("/route/user/logout")
					.filter(globalFilter.apply(new GlobalFilter.Config()))
					.filter(jwtFilter.apply(new JWTFilter.Config()))
				)
				.uri(LOCAL_URL))
			.route("route_foobar", r -> r.path("/foobar")
				.filters(f -> f
					.setPath("/route/foobar")
					.filter(globalFilter.apply(new GlobalFilter.Config()))
					.filter(jwtFilter.apply(new JWTFilter.Config()))
				)
				.uri(LOCAL_URL))
			.build();
	}
}
