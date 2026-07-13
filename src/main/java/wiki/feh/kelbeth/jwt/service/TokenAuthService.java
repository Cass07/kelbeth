package wiki.feh.kelbeth.jwt.service;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import wiki.feh.kelbeth.jwt.dto.TokenClaimDto;
import wiki.feh.kelbeth.jwt.util.IJwtManager;
import wiki.feh.kelbeth.tokenapi.domain.JWT_TYPE;

@RequiredArgsConstructor
@Service
public class TokenAuthService {
	@Qualifier("jwtManagerV1")
	private final IJwtManager jwtManager;

	public TokenClaimDto validateAndParseAccessToken(String token) {
		HashMap<String, String> claims = jwtManager.validateAndParseClaim(token);
		try {
			if(!claims.get("type").equals(JWT_TYPE.ACCESS_TOKEN.toString())) {
				throw new IllegalArgumentException("Invalid access token type");
			}

			return new TokenClaimDto(
				claims.get("userId"),
				claims.get("sid"),
				claims.get("jti"),
				claims.get("type")
			);
		} catch (NullPointerException _) {
			throw new IllegalArgumentException("Invalid token claims");
		}
	}
}
