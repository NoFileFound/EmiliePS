package org.genshinimpact.webserver.services;

// Imports
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class HeartbeatService {
    @Getter private final Cache<String, Instant> heartBeatCache;

    public HeartbeatService() {
        this.heartBeatCache = Caffeine.newBuilder().expireAfterWrite(12, TimeUnit.HOURS).maximumSize(100000).build();
    }
}