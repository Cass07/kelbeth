package wiki.feh.kelbeth.tokenapi.dto;

public record TokenStringPairDto(
	String accessToken,
	String refreshToken
) {
}
