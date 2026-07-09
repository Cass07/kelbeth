package wiki.feh.kelbeth.tokenapi.domain;

import java.util.Map;

import lombok.Getter;

@Getter
public final class APIAccessToken extends JWT {
	private static final long DURATION_MILLI = 1_000L * 60 * 60 * 24; // 24시간 second

	public APIAccessToken(Map<String, String> claims) {
		super(claims);
	}

	@Override
	public long getDurationMilli() {
		return DURATION_MILLI;
	}
}
