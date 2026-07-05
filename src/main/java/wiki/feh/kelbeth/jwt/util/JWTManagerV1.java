package wiki.feh.kelbeth.jwt.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 토큰 Validation, Claim 추출, 생성 클래스
 * 토큰 정책의 관리와 정첵상의 유효성 검증은 토큰 클래스가 담당하고,
 * 이 클래스는 JWT 서명만 검증한다
 */
@Component
public class JWTManagerV1 implements IJWTManager {

	private final SecretKey key;
	private static final String ISSUER = "kelbeth";

	public JWTManagerV1(@Value("${spring.jwt.secret}") String secretKey) {
		byte[] keyBytes = Base64.getDecoder().decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * JWT 토큰 유효성 검증 (아마도 사용 X)
	 * @param token 토큰 String
	 * @return 토큰의 유효성 여부
	 */
	@Override
	public boolean validateToken(String token) {

		try {
			Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token);
		} catch (Exception e) {
			// exception handling은 나중에한대
			return false;
		}
		return true;
	}

	/**
	 * JWT 토큰에서 클레임 추출
	 * JWT String 받아와서 엔티티로 초기화할 때 클레임 추출하면서, 같이 유효성 검사도 체크가능
	 * Exception 발생하면 유효하지 않은 토큰으로 판단하고 처리할것
	 * @param token 토큰 String
	 * @return Claim HashMap
	 * @throws JwtException 유효하지 않은 토큰일 경우 발생
	 */
	@Override
	public HashMap<String, String> getClaims(String token) throws JwtException {
		Jws<Claims> claims = Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token);

		HashMap<String, String> claimsMap = new HashMap<>();

		claims.getPayload().forEach((k, v) -> claimsMap.put(k, v.toString()));

		return claimsMap;

	}

	/**
	 * JWT 토큰 생성
	 * @param claims 클레임 HashMap
	 * @param issuedAt 발급 시간 (JWT 클래스에서 지정하여 넘겨준다)
	 * @param expirationMillis 만료 시간 길이
	 * @return JWT 토큰 String
	 */
	@Override
	public String generateToken(HashMap<String, String> claims, LocalDateTime issuedAt, long expirationMillis) {
		Timestamp issuedAtTimestamp = Timestamp.valueOf(issuedAt);
		Timestamp expirationTimestamp = new Timestamp(issuedAtTimestamp.getTime() + expirationMillis);

		return Jwts.builder()
			.issuer(JWTManagerV1.ISSUER)
			.issuedAt(issuedAtTimestamp)
			.expiration(expirationTimestamp)
			.claims(claims)
			.signWith(key, Jwts.SIG.HS256)
			.compact();
	}
}
