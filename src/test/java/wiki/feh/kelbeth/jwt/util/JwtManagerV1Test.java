package wiki.feh.kelbeth.jwt.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class JwtManagerV1Test {

	@InjectMocks
	JwtManagerV1 jwtManagerV1 = new JwtManagerV1("DuDD2CVguvB7TniastytzX5NLm8ESDNrywszQZ4OQ8f");

	@DisplayName("ValidateToken 성공")
	@Test
	void testValidateTokenSuccess() {
		// Given
		String token = jwtManagerV1.generateToken(new HashMap<>(), LocalDateTime.now(), 60000);

		// When
		boolean isValid = jwtManagerV1.validateToken(token);

		// Then
		assertTrue(isValid);
	}

	@DisplayName("ValidateToken 실패")
	@Test
	void testValidateTokenFailure() {
		// Given
		String token = "invalidToken";

		// When
		boolean isValid = jwtManagerV1.validateToken(token);

		// Then
		assertFalse(isValid);
	}

	@DisplayName("GetClaims 성공")
	@Test
	void testValidateAndParseClaimSuccess() {
		// Given
		HashMap<String, String> claims = new HashMap<>();
		claims.put("userId", "12345");
		String token = jwtManagerV1.generateToken(claims, LocalDateTime.now(), 60000);

		// When
		HashMap<String, String> extractedClaims = jwtManagerV1.validateAndParseClaim(token);

		// Then
		assertEquals("12345", extractedClaims.get("userId"));
	}

	@DisplayName("GetClaims에서 유효하지 않은 토큰으로 Exception 발생")
	@Test
	void testValidateAndParseClaimInvalidToken() {
		// Given
		String token = "invalidToken";

		// When & Then
		assertThrows(io.jsonwebtoken.JwtException.class, () -> jwtManagerV1.validateAndParseClaim(token));
	}


}