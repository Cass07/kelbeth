package wiki.feh.kelbeth.exception;

public class InvalidTokenException extends JWTAuthException{
	public InvalidTokenException() {
		super("Invalid token");
	}
}
