package wiki.feh.kelbeth.tokenapi.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;
import wiki.feh.kelbeth.tokenapi.dto.TokenPairDto;
import wiki.feh.kelbeth.tokenapi.exception.InvalidClaimException;
import wiki.feh.kelbeth.tokenapi.exception.InvalidRefreshTokenException;

@Getter
public final class APIRefreshToken extends JWT {
	private static final long DURATION_MILLI = 1_000L * 60 * 60 * 24 * 7; // 1주일 second
	private static final JWT_TYPE TYPE = JWT_TYPE.REFRESH_TOKEN;

	private final String sessionId;

	public APIRefreshToken(Map<String, String> claims) {
		super(claims);

		if (!Objects.equals(claims.get("type"), TYPE.getName())) {
			throw new InvalidRefreshTokenException();
		}

		if (claims.get("sid") == null) {
			throw new InvalidClaimException();
		}

		this.sessionId = claims.get("sid");
	}

	public APIRefreshToken(String userId) {
		super(userId);
		this.sessionId = java.util.UUID.randomUUID().toString();
	}

	@Override
	public long getDurationMilli() {
		return DURATION_MILLI;
	}

	@Override
	public JWT_TYPE getType() {
		return TYPE;
	}

	@Override
	public Map<String, String> getRenewedClaims() {
		HashMap<String, String> newClaims = new java.util.HashMap<>(this.getClaims());
		newClaims.put("jti", this.getJti());
		newClaims.put("sid", this.getSessionId());
		newClaims.put("type", this.getType().getName());

		return Map.copyOf(newClaims);
	}

	public TokenPairDto regenerate() {
		// uuid 갱신
		super.regenerateUUID();

		// 새로운 AccessToken 생성
		APIAccessToken newAccessToken = new APIAccessToken(this.getUserId());

		// 두 객체 반환
		return new TokenPairDto(newAccessToken, this);

	}
}
