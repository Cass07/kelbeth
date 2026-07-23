package wiki.feh.kelbeth.tokenapi.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TokenApiExceptionHandler {
	@ExceptionHandler(TokenApiException.class)
	public ResponseEntity<String> handleTokenApiException(TokenApiException ex) {
		return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
	}
}
