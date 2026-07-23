package wiki.feh.kelbeth.tokenapi.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends TokenApiException {
	public InvalidRefreshTokenException() {
		super("Invalid refresh token",
			HttpStatus.UNAUTHORIZED);
	}
}
