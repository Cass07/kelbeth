package wiki.feh.kelbeth.tokenapi.domain;

import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;

@Getter
public abstract sealed class JWT permits APIAccessToken, APIRefreshToken {
	@Getter(AccessLevel.NONE)
	private final Map<String, String> claims;

	private final String userId;
	private String jti;
	private String sessionId;

	protected JWT(String userId) {
		this.userId = userId;
		this.jti = java.util.UUID.randomUUID().toString();
		this.sessionId = java.util.UUID.randomUUID().toString();
		this.claims = Map.of(
			"userId", this.userId,
			"jti", this.jti,
			"sid", this.sessionId
		);
	}

	protected JWT(Map<String, String> claims) {
		this.claims = claims;
		this.userId = claims.get("userId");
		this.jti = claims.get("jti");
		this.sessionId = claims.get("sid");
	}

	protected void regenerateUUID() {
		this.jti = java.util.UUID.randomUUID().toString();
	}

	public abstract long getDurationMilli();

	public Map<String, String> getRenewedClaims() {
		Map<String, String> newClaims = new java.util.HashMap<>(this.claims);
		newClaims.put("jti", this.jti);
		newClaims.put("sid", this.sessionId);

		return Map.copyOf(newClaims);
	}
}
