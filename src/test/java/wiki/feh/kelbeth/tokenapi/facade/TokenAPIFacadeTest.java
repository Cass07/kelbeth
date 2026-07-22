package wiki.feh.kelbeth.tokenapi.facade;

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
import wiki.feh.kelbeth.tokenapi.dto.TokenStringPairDto;
import wiki.feh.kelbeth.tokenapi.service.TokenAPIAuthService;
import wiki.feh.kelbeth.tokenapi.service.TokenAPIRedisCacheService;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TokenAPIFacadeTest {

	@InjectMocks
	TokenAPIFacade tokenAPIFacade;

	@Mock
	TokenAPIRedisCacheService tokenAPIRedisCacheService;

	@Mock
	TokenAPIAuthService tokenAPIAuthService;

	@DisplayName("로그인 토큰 생성 성공")
	@Test
	void testGenerateToken() {
		// Given
		String userId = "testUser";

		String accessTokenString = "accessTokenString";
		String refreshTokenString = "refreshTokenString";

		TokenStringPairDto tokenPairDto = new TokenStringPairDto(accessTokenString, refreshTokenString);

		doReturn(Mono.just(true)).when(tokenAPIRedisCacheService).setValue(anyString(), anyString(), any());

		doReturn(tokenPairDto).when(tokenAPIAuthService).generateTokenStringPair(any(), any());

		// When
		var resultMono = tokenAPIFacade.login(userId);

		// Then
		var result = resultMono.block();
		assertNotNull(result);
		assertEquals(accessTokenString, result.accessToken());
		assertEquals(refreshTokenString, result.refreshToken());
	}

	@DisplayName("로그인 토큰 생성 실패 - Redis 저장 실패")
	@Test
	void testGenerateTokenRedisFailure() {
		// Given
		String userId = "testUser";

		doReturn(Mono.just(false)).when(tokenAPIRedisCacheService).setValue(anyString(), anyString(), any());

		// When
		var resultMono = tokenAPIFacade.login(userId);

		// Then
		assertThrows(RuntimeException.class, resultMono::block);
	}

	@DisplayName("로그아웃 성공")
	@Test
	void testLogoutSuccess() {
		// Given
		String sessionId = TestConstants.REFRESH_TOKEN.getSessionId();
		String userId = TestConstants.REFRESH_TOKEN.getUserId();
		String refreshToken = "testRefreshToken";

		doReturn(TestConstants.REFRESH_TOKEN).when(tokenAPIAuthService).parseRefreshToken(refreshToken);
		doReturn(Mono.just(true)).when(tokenAPIRedisCacheService).deleteValue(sessionId);

		// When
		var resultMono = tokenAPIFacade.logout(refreshToken);

		// Then
		var result = resultMono.block();
		assertEquals(userId, result);
	}

	@DisplayName("로그아웃 실패 - Redis 삭제 실패")
	@Test
	void testLogoutFailure() {
		// Given
		String sessionId = TestConstants.REFRESH_TOKEN.getSessionId();
		String refreshToken = "testRefreshToken";

		doReturn(TestConstants.REFRESH_TOKEN).when(tokenAPIAuthService).parseRefreshToken(refreshToken);
		doReturn(Mono.just(false)).when(tokenAPIRedisCacheService).deleteValue(sessionId);

		// When
		var resultMono = tokenAPIFacade.logout(refreshToken);

		// Then
		assertThrows(RuntimeException.class, resultMono::block);
	}

	@DisplayName("Refresh 실패 - session 조회 결과 없음")
	@Test
	void testRefreshFailureSessionNotFound() {
		// Given
		String refreshToken = "testRefreshToken";
		String sessionId = TestConstants.REFRESH_TOKEN.getSessionId();

		doReturn(TestConstants.REFRESH_TOKEN).when(tokenAPIAuthService).parseRefreshToken(refreshToken);
		doReturn(Mono.empty()).when(tokenAPIRedisCacheService).getValue(sessionId);

		// When
		var resultMono = tokenAPIFacade.refresh(refreshToken);

		// Then
		try {
			resultMono.block();
		} catch (RuntimeException e) {
			assertEquals("Session not found in Redis", e.getMessage());
		}
	}

	@DisplayName("Refresh 실패 - jti 불일치")
	@Test
	void testRefreshFailureJtiMismatch() {
		// Given
		String refreshToken = "testRefreshToken";
		String sessionId = TestConstants.REFRESH_TOKEN.getSessionId();

		doReturn(TestConstants.REFRESH_TOKEN).when(tokenAPIAuthService).parseRefreshToken(refreshToken);
		doReturn(Mono.just("differentJti")).when(tokenAPIRedisCacheService).getValue(sessionId);
		doReturn(Mono.just(true)).when(tokenAPIRedisCacheService).deleteValue(sessionId);

		// When
		var resultMono = tokenAPIFacade.refresh(refreshToken);

		// Then
		try {
			resultMono.block();
		} catch (RuntimeException e) {
			assertEquals("Invalid refresh token", e.getMessage());
		}
	}

	@DisplayName("Refresh 성공 - jti 일치 - lock 선점 성공")
	@Test
	void testRefreshSuccessJtiMatchLockSuccess() {
		// Given
		String refreshToken = "testRefreshToken";
		String sessionId = TestConstants.REFRESH_TOKEN.getSessionId();
		String jti = TestConstants.REFRESH_TOKEN.getJti();
		TokenStringPairDto newTokenPair = new TokenStringPairDto("newAccessToken", "newRefreshToken");

		doReturn(TestConstants.REFRESH_TOKEN).when(tokenAPIAuthService).parseRefreshToken(refreshToken);
		doReturn(Mono.just(jti)).when(tokenAPIRedisCacheService).getValue(sessionId);
		doReturn(Mono.just(true)).when(tokenAPIRedisCacheService).setValueIfAbsent(jti, "LOCK", java.time.Duration.ofMillis(90));
		doReturn(Mono.just(true)).when(tokenAPIRedisCacheService).setValue(anyString(), anyString(), any());
		doReturn(Mono.just(true)).when(tokenAPIRedisCacheService).setTokenStringPair(anyString(), any());
		doReturn(newTokenPair).when(tokenAPIAuthService).generateTokenStringPair(any(), any());

		// When
		var resultMono = tokenAPIFacade.refresh(refreshToken);

		// Then
		var result = resultMono.block();
		assertNotNull(result);
		assertEquals(newTokenPair.accessToken(), result.accessToken());
		assertEquals(newTokenPair.refreshToken(), result.refreshToken());

		verify(tokenAPIAuthService).parseRefreshToken(refreshToken);
		verify(tokenAPIRedisCacheService).getValue(sessionId);
		verify(tokenAPIRedisCacheService).setValueIfAbsent(jti, "LOCK", java.time.Duration.ofMillis(90));
		verify(tokenAPIRedisCacheService).setValue(anyString(), anyString(), any());
		verify(tokenAPIRedisCacheService).setTokenStringPair(anyString(), any());
		verify(tokenAPIAuthService).generateTokenStringPair(any(), any());
	}

	@DisplayName("Refresh 성공 - jti 일치 - lock 선점 실패 - 기존 토큰 페어 조회")
	@Test
	void testRefreshSuccessJtiMatchLockFailure() {
		// Given
		String refreshToken = "testRefreshToken";
		String sessionId = TestConstants.REFRESH_TOKEN.getSessionId();
		String jti = TestConstants.REFRESH_TOKEN.getJti();
		TokenStringPairDto newTokenPair = new TokenStringPairDto("existingAccessToken", "existingRefreshToken");
		String existingTokenPairJson = "{\"accessToken\":\"existingAccessToken\",\"refreshToken\":\"existingRefreshToken\"}";

		doReturn(TestConstants.REFRESH_TOKEN).when(tokenAPIAuthService).parseRefreshToken(refreshToken);
		doReturn(Mono.just(jti)).when(tokenAPIRedisCacheService).getValue(sessionId);
		doReturn(Mono.just(existingTokenPairJson)).when(tokenAPIRedisCacheService).getValue(jti);
		doReturn(Mono.just(false)).when(tokenAPIRedisCacheService).setValueIfAbsent(jti, "LOCK", java.time.Duration.ofMillis(90));

		// When
		var resultMono = tokenAPIFacade.refresh(refreshToken);

		// Then
		var result = resultMono.block();
		assertNotNull(result);
		assertEquals(newTokenPair.accessToken(), result.accessToken());
		assertEquals(newTokenPair.refreshToken(), result.refreshToken());

		verify(tokenAPIAuthService).parseRefreshToken(refreshToken);
		verify(tokenAPIRedisCacheService).getValue(sessionId);
		verify(tokenAPIRedisCacheService).getValue(jti);
		verify(tokenAPIRedisCacheService).setValueIfAbsent(jti, "LOCK", java.time.Duration.ofMillis(90));
	}

	@DisplayName("Refresh 실패 - jti 일치 - lock 선점 실패 - 기존 토큰 페어 조회 실패")
	@Test
	void testRefreshFailureJtiMatchLockFailureExistingTokenPairNotFound() {
		// Given
		String refreshToken = "testRefreshToken";
		String sessionId = TestConstants.REFRESH_TOKEN.getSessionId();
		String jti = TestConstants.REFRESH_TOKEN.getJti();

		doReturn(TestConstants.REFRESH_TOKEN).when(tokenAPIAuthService).parseRefreshToken(refreshToken);
		doReturn(Mono.just(jti)).when(tokenAPIRedisCacheService).getValue(sessionId);
		doReturn(Mono.empty()).when(tokenAPIRedisCacheService).getValue(jti);
		doReturn(Mono.just(false)).when(tokenAPIRedisCacheService).setValueIfAbsent(jti, "LOCK", java.time.Duration.ofMillis(90));

		// When
		var resultMono = tokenAPIFacade.refresh(refreshToken);

		// Then
		try {
			resultMono.block();
		} catch (RuntimeException e) {
			assertEquals("Token string pair not found in Redis", e.getMessage());
		}

		verify(tokenAPIAuthService).parseRefreshToken(refreshToken);
		verify(tokenAPIRedisCacheService).getValue(sessionId);
		verify(tokenAPIRedisCacheService).getValue(jti);
		verify(tokenAPIRedisCacheService).setValueIfAbsent(jti, "LOCK", java.time.Duration.ofMillis(90));
	}

}