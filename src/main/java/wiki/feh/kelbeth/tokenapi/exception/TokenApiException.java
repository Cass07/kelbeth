package wiki.feh.kelbeth.tokenapi.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class TokenApiException extends RuntimeException {
	private final String message;
	private final HttpStatus status;

	public TokenApiException(String message, HttpStatus status) {
		super(message);
		this.message = message;
		this.status = status;
	}
}
