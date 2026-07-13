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
		String sessionId = "testSessionId";

		doReturn(Mono.just(true)).when(tokenAPIRedisCacheService).deleteValue(sessionId);

		// When
		var resultMono = tokenAPIFacade.logout(sessionId);

		// Then
		var result = resultMono.block();
		assertEquals(Boolean.TRUE, result);
	}

	@DisplayName("로그아웃 실패 - Redis 삭제 실패")
	@Test
	void testLogoutFailure() {
		// Given
		String sessionId = "testSessionId";

		doReturn(Mono.just(false)).when(tokenAPIRedisCacheService).deleteValue(sessionId);

		// When
		var resultMono = tokenAPIFacade.logout(sessionId);

		// Then
		assertThrows(RuntimeException.class, resultMono::block);
	}

}