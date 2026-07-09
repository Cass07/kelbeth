package wiki.feh.kelbeth.tokenapi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class TokenAPIDto {

	@Getter
	public static class LoginRequest {
		private String userId;
	}
}
