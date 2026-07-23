package wiki.feh.kelbeth.exception;

public class InvalidTokenClaimException extends JWTAuthException {
	public InvalidTokenClaimException() {
		super("Invalid token claims");
	}
}
