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

import wiki.feh.kelbeth.helper.TestConstants;
import wiki.feh.kelbeth.jwt.service.TokenAuthService;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TokenAuthFacadeTest {
	@InjectMocks
	TokenAuthFacade tokenAuthFacade;

	@Mock
	TokenAuthService tokenAuthService;


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


	@DisplayName("토큰 검증 성공")
	@Test
	void testValidateAndParseToken() {
		// Given
		String token = "validAccessToken";

		doReturn(TestConstants.ACCESS_CLAIM_DTO).when(tokenAuthService).validateAndParseAccessToken(token);

		// When
		var resultMono = tokenAuthFacade.validateAndParseToken(token);

		// Then
		var result = resultMono.block();
		assertNotNull(result);
		assertEquals(TestConstants.ACCESS_CLAIM_DTO.userId(), result.userId());
		assertEquals(TestConstants.ACCESS_CLAIM_DTO.jti(), result.jti());
	}

}