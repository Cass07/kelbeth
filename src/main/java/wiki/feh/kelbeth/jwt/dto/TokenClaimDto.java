package wiki.feh.kelbeth.jwt.dto;

public record TokenClaimDto(
	String userId,
	String sessionId,
	String jti,
	String type
) {}
