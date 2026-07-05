package wiki.feh.kelbeth.jwt.util;

import java.time.LocalDateTime;
import java.util.HashMap;

public interface IJWTManager {
	boolean validateToken(String token);
	HashMap<String, String> getClaims(String token);
	String generateToken(HashMap<String, String> claims, LocalDateTime issuedAt,long expirationMillis);
}
