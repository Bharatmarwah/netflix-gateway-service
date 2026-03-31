package in.bm.netflix_gateway_service.Configuration;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    private final RedisRateLimiter authRateLimiter;

    private final KeyResolver keyResolver;

    public GatewayRouteConfig(RedisRateLimiter authRateLimiter, KeyResolver keyResolver) {
        this.authRateLimiter = authRateLimiter;
        this.keyResolver = keyResolver;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route("auth",
                        r -> r.path("/auth/user/**")
                                .filters(f -> f.requestRateLimiter(c->c.setRateLimiter(authRateLimiter).setKeyResolver(keyResolver)))
                                .uri("lb://NETFLIX-AUTH-SERVICE")
                ).build();
    }





}
