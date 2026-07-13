package wiki.feh.kelbeth.jwt.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SessionCache {
	private String sessionId;
	private String jti;

	private LocalDateTime expiration;

	public SessionCache(String sessionId, String jti) {
		this.sessionId = sessionId;
		this.jti = jti;
		this.expiration = null;
	}
}
