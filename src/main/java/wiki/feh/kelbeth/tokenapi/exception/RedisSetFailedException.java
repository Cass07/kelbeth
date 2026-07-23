package wiki.feh.kelbeth.tokenapi.exception;

import org.springframework.http.HttpStatus;

public class RedisSetFailedException extends TokenApiException {
	public RedisSetFailedException(String message) {
		super(message,
			HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
