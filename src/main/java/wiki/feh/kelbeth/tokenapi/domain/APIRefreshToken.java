package wiki.feh.kelbeth.tokenapi.domain;

import java.util.Map;

import wiki.feh.kelbeth.tokenapi.dto.TokenPairDto;

public final class APIRefreshToken extends JWT {
	private static final long DURATION_MILLI = 1_000L * 60 * 60 * 24 * 7; // 1주일 second

	public APIRefreshToken(Map<String, String> claims) {
		super(claims);
	}

	public APIRefreshToken(String userId) {
		super(userId);
	}

	@Override
	public long getDurationMilli() {
		return DURATION_MILLI;
	}

	public TokenPairDto regenerate() {
		// uuid 갱신
		super.regenerateUUID();

		// 갱신된 claims 반환
		Map<String, String> renewedClaims = super.getRenewedClaims();

		// accessToken 객체 생성
		APIAccessToken newAccessToken = new APIAccessToken(renewedClaims);

		// 두 객체 반환
		return new TokenPairDto(newAccessToken, this);

	}
}
