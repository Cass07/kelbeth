package wiki.feh.kelbeth.tokenapi.exception;

import org.springframework.http.HttpStatus;

public class SessionNotFoundException extends TokenApiException {
	public SessionNotFoundException() {
		super("Session not found",
			HttpStatus.UNAUTHORIZED);
	}
}
