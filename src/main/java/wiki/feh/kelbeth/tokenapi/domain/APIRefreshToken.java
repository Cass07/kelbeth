package wiki.feh.kelbeth.tokenapi.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import wiki.feh.kelbeth.tokenapi.dto.TokenPairDto;

public final class APIRefreshToken extends JWT {
	private static final long DURATION_MILLI = 1_000L * 60 * 60 * 24 * 7; // 1주일 second
	private static final JWT_TYPE TYPE = JWT_TYPE.REFRESH_TOKEN;

	public APIRefreshToken(Map<String, String> claims) {
		super(claims);

		if(!Objects.equals(claims.get("type"), TYPE.getName())) {
			throw new IllegalArgumentException("Invalid claims for APIRefreshToken");
		}
	}

	public APIRefreshToken(String userId) {
		super(userId);
	}

	@Override
	public long getDurationMilli() {
		return DURATION_MILLI;
	}

	@Override
	public JWT_TYPE getType() {
		return TYPE;
	}

	private Map<String, String> getRenewedClaimsAccessType() {
		Map<String, String> renewedClaims = new HashMap<>(super.getRenewedClaims());
		renewedClaims.put("type", JWT_TYPE.ACCESS_TOKEN.getName());
		return Map.copyOf(renewedClaims);
	}

	public TokenPairDto regenerate() {
		// uuid 갱신
		super.regenerateUUID();

		// 갱신된 claims 반환
		Map<String, String> renewedClaims = this.getRenewedClaimsAccessType();

		// accessToken 객체 생성
		APIAccessToken newAccessToken = new APIAccessToken(renewedClaims);

		// 두 객체 반환
		return new TokenPairDto(newAccessToken, this);

	}
}
