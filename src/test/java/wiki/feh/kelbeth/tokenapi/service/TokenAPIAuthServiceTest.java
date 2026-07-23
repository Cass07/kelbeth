package wiki.feh.kelbeth.tokenapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import wiki.feh.kelbeth.helper.TestConstants;
import wiki.feh.kelbeth.jwt.util.IJwtManager;
import wiki.feh.kelbeth.tokenapi.domain.APIAccessToken;
import wiki.feh.kelbeth.tokenapi.domain.APIRefreshToken;
import wiki.feh.kelbeth.tokenapi.dto.TokenPairDto;
import wiki.feh.kelbeth.tokenapi.exception.InvalidRefreshTokenException;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TokenAPIAuthServiceTest {
	@InjectMocks
	TokenAPIAuthService tokenAPIAuthService;

	@Mock
	IJwtManager jwtManager;

	@DisplayName("generateToken 성공")
	@Test
	void testGenerateToken() {
		// Given
		String result = "generatedTokenString";

		doReturn(result).when(jwtManager).generateToken(any(), any(), anyLong());

		// When
		var token = tokenAPIAuthService.generateToken(new HashMap<>(), LocalDateTime.now(), 3600L);

		// Then
		assertNotNull(token);
		assertEquals(result, token);
	}

	@DisplayName("generateTokenStringPair 성공")
	@Test
	void testGenerateTokenStringPair() {
		// Given
		String accessToken = "generatedAccessTokenString";
		String refreshToken = "generatedRefreshTokenString";

		APIAccessToken accessTokenObj = new APIAccessToken(TestConstants.ACCESS_CLAIMS);
		APIRefreshToken refreshTokenObj = new APIRefreshToken(TestConstants.REFRESH_CLAIMS);

		TokenPairDto tokenPairDto = new TokenPairDto(accessTokenObj, refreshTokenObj);

		doReturn(accessToken).when(jwtManager).generateToken(any(), any(), eq(accessTokenObj.getDurationMilli()));
		doReturn(refreshToken).when(jwtManager).generateToken(any(), any(), eq(refreshTokenObj.getDurationMilli()));

		// When
		var tokenStringPair = tokenAPIAuthService.generateTokenStringPair(tokenPairDto, LocalDateTime.now());

		// then
		assertNotNull(tokenStringPair);
		assertEquals(accessToken, tokenStringPair.accessToken());
		assertEquals(refreshToken, tokenStringPair.refreshToken());
	}

	@DisplayName("Refresh Token parsing 성공")
	@Test
	void testParseRefreshToken() {
		// Given
		String refreshToken = "validRefreshTokenString";

		doReturn(new HashMap<>(TestConstants.REFRESH_CLAIMS)).when(jwtManager).validateAndParseClaim(refreshToken);

		// When
		var result = tokenAPIAuthService.parseRefreshToken(refreshToken);

		// Then
		assertNotNull(result);
		assertEquals("testUser", result.getUserId());
		assertEquals("testSessionId", result.getSessionId());
		assertEquals("testJti", result.getJti());
		assertEquals("REFRESH_TOKEN", result.getType().toString());
	}

	@DisplayName("Refresh Token parsing 실패 - 잘못된 토큰 타입")
	@Test
	void testParseRefreshTokenInvalidType() {
		// Given
		String refreshToken = "invalidRefreshTokenString";

		doReturn(new HashMap<>(TestConstants.ACCESS_CLAIMS)).when(jwtManager).validateAndParseClaim(refreshToken);

		// When & Then
		assertThrows(InvalidRefreshTokenException.class, () -> {
			tokenAPIAuthService.parseRefreshToken(refreshToken);
		});
	}

}