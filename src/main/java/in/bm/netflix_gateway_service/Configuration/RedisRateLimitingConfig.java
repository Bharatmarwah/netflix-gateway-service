package in.bm.netflix_gateway_service.Configuration;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RedisRateLimitingConfig {

    @Bean
    public RedisRateLimiter redisRateLimiter(){
        return new RedisRateLimiter(10,20);
    }

    // defaultBurstCapacity means initially user can make 20 requests (if used) after that user can make (defaultReplenishRate) 10 requests per second

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange ->
                Mono.justOrEmpty(exchange.getRequest()
                        .getHeaders()
                        .getFirst("X-User-Id"))
                        .defaultIfEmpty("anonymous");
    }

}
