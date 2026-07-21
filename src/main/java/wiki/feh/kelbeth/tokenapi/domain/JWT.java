package wiki.feh.kelbeth.tokenapi.domain;

import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;

@Getter
public abstract sealed class JWT permits APIAccessToken, APIRefreshToken {
	@Getter(AccessLevel.PROTECTED)
	private final Map<String, String> claims;

	private final String userId;
	private String jti;

	protected JWT(String userId) {
		this.userId = userId;
		this.jti = java.util.UUID.randomUUID().toString();
		this.claims = Map.of(
			"userId", this.userId,
			"jti", this.jti,
			"type", this.getType().getName()
		);
	}

	protected JWT(Map<String, String> claims) {
		this.claims = claims;
		this.userId = claims.get("userId");
		this.jti = claims.get("jti");
	}

	protected void regenerateUUID() {
		this.jti = java.util.UUID.randomUUID().toString();
	}

	public abstract JWT_TYPE getType();

	public abstract long getDurationMilli();

	public abstract Map<String, String> getRenewedClaims();
}
