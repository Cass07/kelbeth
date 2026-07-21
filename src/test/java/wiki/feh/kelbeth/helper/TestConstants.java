package wiki.feh.kelbeth.helper;

import java.util.Map;

import lombok.NoArgsConstructor;
import wiki.feh.kelbeth.jwt.dto.TokenClaimDto;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class TestConstants {
	public static final Map<String, String> REFRESH_CLAIMS = Map.of(
		"userId", "testUser",
		"sid", "testSessionId",
		"jti", "testJti",
		"type", "REFRESH_TOKEN"
	);

	public static final Map<String, String> ACCESS_CLAIMS = Map.of(
		"userId", "testUser",
		"jti", "testJti",
		"type", "ACCESS_TOKEN"
	);

	public static final TokenClaimDto ACCESS_CLAIM_DTO = new TokenClaimDto(
		ACCESS_CLAIMS.get("userId"),
		ACCESS_CLAIMS.get("jti"),
		ACCESS_CLAIMS.get("type")
	);

}
