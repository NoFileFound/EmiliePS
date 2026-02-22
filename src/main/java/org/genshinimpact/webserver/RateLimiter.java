package org.genshinimpact.webserver;

// Imports
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiter {
    @Bean
    public Cache<String, AtomicInteger> requestRateCache() {
        return Caffeine.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).maximumSize(10000).build();
    }
}