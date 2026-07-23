package wiki.feh.kelbeth.tokenapi.exception;

import org.springframework.http.HttpStatus;

public class InvalidClaimException extends TokenApiException {
	public InvalidClaimException() {
		super("Claim is Null or Do not have required key",
			HttpStatus.UNAUTHORIZED);
	}
}
