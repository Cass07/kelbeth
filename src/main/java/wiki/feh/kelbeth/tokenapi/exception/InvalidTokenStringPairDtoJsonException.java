package wiki.feh.kelbeth.tokenapi.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenStringPairDtoJsonException extends TokenApiException {
	public InvalidTokenStringPairDtoJsonException() {
		super("Failed to parse JSON to TokenStringPairDto",
			HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
