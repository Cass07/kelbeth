package wiki.feh.kelbeth.tokenapi.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import wiki.feh.kelbeth.jwt.dto.TokenClaimDto;
import wiki.feh.kelbeth.jwt.util.IJwtManager;
import wiki.feh.kelbeth.tokenapi.domain.APIRefreshToken;
import wiki.feh.kelbeth.tokenapi.dto.TokenPairDto;
import wiki.feh.kelbeth.tokenapi.dto.TokenStringPairDto;

/**
 * JWTManager가지고 실제 토큰 검증 및 파싱을 수행하는 서비스
 * Gateway Filter에서 사용하는 Service랑 중복되는 기능이 있는데
 * 지금은 테스트용이라 같은 패키지 내에 있는거고 프로덕션에서는 분리되니까 일부러 별개의 서비스로 구현함
 */
@RequiredArgsConstructor
@Service
public class TokenAPIAuthService {
	@Qualifier("jwtManagerV1")
	private final IJwtManager jwtManager;

	public TokenClaimDto validateAndParseToken(String token) {
		Map<String, String> claims = jwtManager.validateAndParseClaim(token);
		try {
			return new TokenClaimDto(
				claims.get("userId"),
				claims.get("sessionId"),
				claims.get("jti")
			);
		} catch (NullPointerException _) {
			throw new IllegalArgumentException("Invalid token claims");
		}
	}

	public String generateToken(Map<String, String> claims, LocalDateTime issuedAt,  long durationMilli) {
		return jwtManager.generateToken(claims, issuedAt, durationMilli);
	}

	public TokenStringPairDto generateTokenStringPair(TokenPairDto tokenPairDto, LocalDateTime issuedAt) {
		String accessTokenString = generateToken(tokenPairDto.accessToken().getRenewedClaims(),
			issuedAt,
			tokenPairDto.accessToken().getDurationMilli());
		String refreshTokenString = generateToken(tokenPairDto.refreshToken().getRenewedClaims(),
			issuedAt,
			tokenPairDto.refreshToken().getDurationMilli());
		return new TokenStringPairDto(accessTokenString, refreshTokenString);
	}

	public APIRefreshToken parseRefreshToken(String token) {
		Map<String, String> claims = jwtManager.validateAndParseClaim(token);
		try {
			return new APIRefreshToken(claims);
		} catch (NullPointerException _) {
			throw new IllegalArgumentException("Invalid refresh token claims");
		}
	}
}
