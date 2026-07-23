package wiki.feh.kelbeth.tokenapi.exception;

import org.springframework.http.HttpStatus;

public class InvalidAccessTokenException extends TokenApiException {
	public InvalidAccessTokenException() {
		super("Invalid access token",
			HttpStatus.UNAUTHORIZED);
	}
}
