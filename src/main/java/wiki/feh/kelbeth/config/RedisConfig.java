package wiki.feh.kelbeth.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableAutoConfiguration(exclude = {
		DataRedisAutoConfiguration.class,
		DataRedisReactiveAutoConfiguration.class
})
@Configuration
public class RedisConfig {

	@Bean
	public ReactiveRedisConnectionFactory redisConnectionFactory() {
		// 임시로 localhost:6379로 연결하도록 설정
		return new LettuceConnectionFactory("localhost", 6379);
	}

	@Bean
	public ReactiveRedisOperations<String, Object> redisTemplate() {
		return new ReactiveRedisTemplate<>(redisConnectionFactory(),
				RedisSerializationContext.<String, Object>newSerializationContext(new StringRedisSerializer())
						.hashKey(new StringRedisSerializer())
						.hashValue(new StringRedisSerializer())
						.build());
	}
}
