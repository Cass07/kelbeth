package wiki.feh.kelbeth.tokenapi.service;

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * 테스트용을 위해 로그인 및 토큰 갱신, 세션 해제 시를 위해서
 * Redis set, delete 두 케이스만 만듬
 */
@RequiredArgsConstructor
@Service
public class TokenAPIRedisCacheService {
	private final ReactiveRedisOperations<String, Object> redisOperations;

	public Mono<Boolean> setValue(String key, Object value, Duration duration) {
		return redisOperations.opsForValue().set(key, value, duration);
	}

	public Mono<Boolean> deleteValue(String key) {
		return redisOperations.opsForValue().delete(key);
	}

	public Mono<String> getValue(String key) {
		return redisOperations.opsForValue().get(key).map(String::valueOf);
	}
}
