package wiki.feh.kelbeth.exception;

public class NotAccessTypeException extends JWTAuthException {
  public NotAccessTypeException() {
		super("Token type is not access token");
	}
}
