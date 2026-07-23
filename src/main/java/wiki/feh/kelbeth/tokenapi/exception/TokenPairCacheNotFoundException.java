package wiki.feh.kelbeth.tokenapi.exception;

import org.springframework.http.HttpStatus;

public class TokenPairCacheNotFoundException extends TokenApiException {
	public TokenPairCacheNotFoundException() {
		super("Token pair cache not found",
			HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
