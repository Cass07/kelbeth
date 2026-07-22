package wiki.feh.kelbeth.tokenapi.dto;

import tools.jackson.databind.ObjectMapper;

public record TokenStringPairDto(
	String accessToken,
	String refreshToken
) {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	public static TokenStringPairDto fromJson(String json) {
		try {
			return objectMapper.readValue(json, TokenStringPairDto.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse JSON to TokenStringPairDto", e);
		}
	}
}
