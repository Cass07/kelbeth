package wiki.feh.kelbeth.jwt.service;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import wiki.feh.kelbeth.exception.InvalidTokenClaimException;
import wiki.feh.kelbeth.exception.InvalidTokenException;
import wiki.feh.kelbeth.exception.NotAccessTypeException;
import wiki.feh.kelbeth.jwt.dto.TokenClaimDto;
import wiki.feh.kelbeth.jwt.util.IJwtManager;
import wiki.feh.kelbeth.tokenapi.domain.JWT_TYPE;

@RequiredArgsConstructor
@Service
public class TokenAuthService {
	@Qualifier("jwtManagerV1")
	private final IJwtManager jwtManager;

	public TokenClaimDto validateAndParseAccessToken(String token) {
		try {
			HashMap<String, String> claims = jwtManager.validateAndParseClaim(token);
			if(!claims.get("type").equals(JWT_TYPE.ACCESS_TOKEN.toString())) {
				throw new NotAccessTypeException();
			}

			return new TokenClaimDto(
				claims.get("userId"),
				claims.get("jti"),
				claims.get("type")
			);
		} catch (NullPointerException _) {
			throw new InvalidTokenClaimException();
		} catch (JwtException _) {
			throw new InvalidTokenException();
		}
	}
}
