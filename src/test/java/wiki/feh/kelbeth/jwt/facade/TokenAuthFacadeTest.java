package wiki.feh.kelbeth.jwt.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import reactor.core.publisher.Mono;
import wiki.feh.kelbeth.helper.TestConstants;
import wiki.feh.kelbeth.jwt.domain.SessionCache;
import wiki.feh.kelbeth.jwt.service.RedisSessionCacheService;
import wiki.feh.kelbeth.jwt.service.TokenAuthService;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TokenAuthFacadeTest {
	@InjectMocks
	TokenAuthFacade tokenAuthFacade;

	@Mock
	TokenAuthService tokenAuthService;

	@Mock
	RedisSessionCacheService redisSessionCacheService;

	@DisplayName("토큰 검증 실패 - 유효하지 않은 토큰")
	@Test
	void testValidateAndParseTokenInvalidToken() {
		// Given
		String token = "invalidAccessToken";

		doThrow(new IllegalArgumentException("Invalid token")).when(tokenAuthService).validateAndParseAccessToken(token);

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			tokenAuthFacade.validateAndParseToken(token).block();
		});
	}

	@DisplayName("토큰 검증 실패 - 세션 캐시 없음")
	@Test
	void testValidateAndParseTokenNoSessionCache() {
		// Given
		String token = "validAccessToken";

		doReturn(TestConstants.ACCESS_CLAIM_DTO).when(tokenAuthService).validateAndParseAccessToken(token);
		doReturn(Mono.empty()).when(redisSessionCacheService).getValue(TestConstants.ACCESS_CLAIM_DTO.sessionId());

		// When & Then
		assertThrows(RuntimeException.class, () -> {
			tokenAuthFacade.validateAndParseToken(token).block();
		});
	}

	@DisplayName("토큰 검증 실패 - 세션 jti 불일치")
	@Test
	void testValidateAndParseTokenSessionJtiMismatch() {
		// Given
		String token = "validAccessToken";
		SessionCache sessionCache = new SessionCache(TestConstants.ACCESS_CLAIMS.get("sid"), "differentJti");

		doReturn(TestConstants.ACCESS_CLAIM_DTO).when(tokenAuthService).validateAndParseAccessToken(token);
		doReturn(Mono.just(sessionCache)).when(redisSessionCacheService).getValue(TestConstants.ACCESS_CLAIM_DTO.sessionId());
		doReturn(Mono.just(true)).when(redisSessionCacheService).deleteValue(TestConstants.ACCESS_CLAIM_DTO.sessionId());

		// When & Then
		assertThrows(RuntimeException.class, () -> {
			tokenAuthFacade.validateAndParseToken(token).block();
		});

		// 삭제 메서드가 호출되었는지 검증
		verify(redisSessionCacheService, times(1)).deleteValue(TestConstants.ACCESS_CLAIM_DTO.sessionId());
	}


	@DisplayName("토큰 검증 및 세션 검증 성공")
	@Test
	void testValidateAndParseToken() {
		// Given
		String token = "validAccessToken";
		SessionCache sessionCache = new SessionCache(TestConstants.ACCESS_CLAIMS.get("sid"), TestConstants.ACCESS_CLAIMS.get("jti"));

		doReturn(TestConstants.ACCESS_CLAIM_DTO).when(tokenAuthService).validateAndParseAccessToken(token);
		doReturn(Mono.just(sessionCache)).when(redisSessionCacheService).getValue(TestConstants.ACCESS_CLAIM_DTO.sessionId());

		// When
		var resultMono = tokenAuthFacade.validateAndParseToken(token);

		// Then
		var result = resultMono.block();
		assertNotNull(result);
		assertEquals(TestConstants.ACCESS_CLAIM_DTO.userId(), result.userId());
		assertEquals(TestConstants.ACCESS_CLAIM_DTO.sessionId(), result.sessionId());
		assertEquals(TestConstants.ACCESS_CLAIM_DTO.jti(), result.jti());
	}

}