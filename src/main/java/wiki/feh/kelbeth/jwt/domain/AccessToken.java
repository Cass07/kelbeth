package wiki.feh.kelbeth.jwt.domain;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class AccessToken {
	private String token;
	private LocalDateTime expiration;

	public AccessToken(String token, LocalDateTime expiration) {
		this.token = token;
		this.expiration = expiration;
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiration);
	}

}
