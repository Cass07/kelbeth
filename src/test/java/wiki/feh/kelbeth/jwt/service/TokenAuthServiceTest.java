package wiki.feh.kelbeth.jwt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import wiki.feh.kelbeth.helper.TestConstants;
import wiki.feh.kelbeth.jwt.dto.TokenClaimDto;
import wiki.feh.kelbeth.jwt.util.IJwtManager;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TokenAuthServiceTest {
	@InjectMocks
	private TokenAuthService tokenAuthService;

	@Mock
	private IJwtManager jwtManager;

	@DisplayName("validateAndParseAccessToken 성공")
	@Test
	void testValidateAndParseAccessTokenSuccess() {
		// Given
		String token = "validAccessToken";
		doReturn(new HashMap<>(TestConstants.ACCESS_CLAIMS)).when(jwtManager).validateAndParseClaim(token);

		// When
		TokenClaimDto result = tokenAuthService.validateAndParseAccessToken(token);

		// then
		assertNotNull(result);
		assertEquals(TestConstants.ACCESS_CLAIMS.get("userId"), result.userId());
		assertEquals(TestConstants.ACCESS_CLAIMS.get("sid"), result.sessionId());
		assertEquals(TestConstants.ACCESS_CLAIMS.get("jti"), result.jti());
		assertEquals(TestConstants.ACCESS_CLAIMS.get("type"), result.type());
	}

	@DisplayName("validateAndParseAccessToken 실패 - Refresh 토큰 타입")
	@Test
	void testValidateAndParseAccessTokenFailureInvalidType() {
		// Given
		String token = "invalidAccessToken";
		doReturn(new HashMap<>(TestConstants.REFRESH_CLAIMS)).when(jwtManager).validateAndParseClaim(token);

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			tokenAuthService.validateAndParseAccessToken(token);
		});

	}

}