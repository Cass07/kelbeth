package wiki.feh.kelbeth.jwt.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SessionCache {
	private String sessionId;
	private String uuid;

	private LocalDateTime expiration;

	public SessionCache(String sessionId, String uuid) {
		this.sessionId = sessionId;
		this.uuid = uuid;
		this.expiration = null;
	}

	public SessionCache updateUUID(String uuid) {
		return new SessionCache(this.sessionId, uuid, this.expiration);
	}
}
