package in.bm.netflix_gateway_service.GlobalFilter;

import in.bm.netflix_gateway_service.Service.TokenValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Component
@RequiredArgsConstructor
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private final TokenValidation tokenValidation;

    boolean isAuthPublicPath(String path) {
        return path.startsWith("/auth/user/login/password");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isAuthPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        boolean isValidate = tokenValidation.validateToken(token);

        if (isValidate) {

            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();

            String ipAddress;
            if (remoteAddress != null && remoteAddress.getAddress() != null) {
                ipAddress = remoteAddress.getAddress().getHostAddress();
            } else {
                ipAddress = null;
            }

            String userId = tokenValidation.extractUserId(token);

            exchange = exchange.mutate()
                    .request(r -> r.headers(h -> {
                        h.add("X-User-Id", userId);
                        if (ipAddress != null) {
                            h.add("X-Ip-Address", ipAddress);
                        }
                    }))
                    .build();

            return chain.filter(exchange);

        } else {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1; // the less the value, the higher priority of the filter
    }
}
