package wiki.feh.kelbeth.jwt.service;

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import wiki.feh.kelbeth.jwt.domain.SessionCache;

@RequiredArgsConstructor
@Service
public class RedisSessionCacheService {
	private final ReactiveRedisOperations<String, Object> redisOperations;

	public Mono<SessionCache> getValue(String key) {
		return redisOperations.opsForValue().get(key).map(value -> new SessionCache(key, String.valueOf(value)));
	}

	public Mono<Boolean> setValue(String key, Object value) {
		return redisOperations.opsForValue().set(key, value);
	}

	public Mono<Boolean> setValue(SessionCache sessionCache) {
		return redisOperations.opsForValue().set(sessionCache.getSessionId(), sessionCache.getUuid());
	}

	public Mono<Boolean> deleteValue(String key) {
		return redisOperations.opsForValue().delete(key);
	}

	public Mono<Boolean> exists(String key) {
		return redisOperations.hasKey(key);
	}

	public Mono<Boolean> setValueWithExpiration(String key, Object value, Duration duration) {
		return redisOperations.opsForValue().set(key, value, duration);
	}
}
