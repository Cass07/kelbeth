package wiki.feh.kelbeth.tokenapi.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;

@Getter
public final class APIAccessToken extends JWT {
	private static final long DURATION_MILLI = 1_000L * 60 * 60 * 24; // 24시간 second
	private static final JWT_TYPE TYPE = JWT_TYPE.ACCESS_TOKEN;

	public APIAccessToken(Map<String, String> claims) {
		super(claims);

		if(!Objects.equals(claims.get("type"), TYPE.getName())) {
			throw new IllegalArgumentException("Invalid claims for APIRefreshToken");
		}
	}

	public APIAccessToken(String userId) {
		super(userId);
	}

	@Override
	public Map<String, String> getRenewedClaims() {
		HashMap<String, String> newClaims = new java.util.HashMap<>();
		newClaims.put("userId", this.getUserId());
		newClaims.put("jti", this.getJti());
		newClaims.put("type", this.getType().getName());
		return Map.copyOf(newClaims);
	}

	@Override
	public long getDurationMilli() {
		return DURATION_MILLI;
	}

	@Override
	public JWT_TYPE getType() {
		return TYPE;
	}
}
