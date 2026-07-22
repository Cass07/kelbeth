package wiki.feh.kelbeth.tokenapi.service;

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;
import wiki.feh.kelbeth.tokenapi.dto.TokenStringPairDto;

/**
 * 테스트용을 위해 로그인 및 토큰 갱신, 세션 해제 시를 위해서
 * Redis set, delete 두 케이스만 만듬
 */
@RequiredArgsConstructor
@Service
public class TokenAPIRedisCacheService {
	private final ReactiveRedisOperations<String, Object> redisOperations;
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public Mono<Boolean> setValue(String key, Object value, Duration duration) {
		return redisOperations.opsForValue().set(key, value, duration);
	}

	public Mono<Boolean> deleteValue(String key) {
		return redisOperations.opsForValue().delete(key);
	}

	public Mono<String> getValue(String key) {
		return redisOperations.opsForValue().get(key).map(String::valueOf);
	}

	/**
	 * `SET key value NX PX duration` 명령어와 동일한 기능을 수행
	 * @param key redis key
	 * @param value redis value
	 * @param duration ttl
	 * @return 성공 시 true, 실패 시 false 반환
	 */
	public Mono<Boolean> setValueIfAbsent(String key, Object value, Duration duration) {
		return redisOperations.opsForValue().setIfAbsent(key, value, duration);
	}

	/**
	 * Redis에 TokenStringPairDto를 json 형태로 저장하고, 만료 시간은 5초로 설정
	 * @param jti JWT ID
	 * @param tokenStringPairDto 저장할 TokenStringPairDto 객체
	 * @return 성공 시 true, 실패 시 false 반환
	 */
	public Mono<Boolean> setTokenStringPair(String jti, TokenStringPairDto tokenStringPairDto) {
		String tokenStringPairJson = objectMapper.writeValueAsString(tokenStringPairDto);

		return redisOperations.opsForValue().set(jti, tokenStringPairJson, Duration.ofSeconds(5L));
	}
}
