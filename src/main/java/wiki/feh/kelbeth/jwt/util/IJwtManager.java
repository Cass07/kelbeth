package wiki.feh.kelbeth.jwt.util;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.JwtException;

public interface IJwtManager {
	boolean validateToken(String token);
	HashMap<String, String> validateAndParseClaim(String token) throws JwtException;
	String generateToken(Map<String, String> claims, LocalDateTime issuedAt,long expirationMillis);
}
