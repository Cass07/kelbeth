package wiki.feh.kelbeth.jwt.dto;

public record TokenClaimDto(
	String userId,
	String jti,
	String type
) {}
