package wiki.feh.kelbeth.exception;

import lombok.Getter;

@Getter
public class JWTAuthException extends RuntimeException {
	private final String message;

	public JWTAuthException(String message) {
		super(message);
		this.message = message;
	}
}
