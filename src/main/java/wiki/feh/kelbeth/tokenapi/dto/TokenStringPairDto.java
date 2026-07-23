package wiki.feh.kelbeth.tokenapi.dto;

import tools.jackson.databind.ObjectMapper;
import wiki.feh.kelbeth.tokenapi.exception.InvalidTokenStringPairDtoJsonException;

public record TokenStringPairDto(
	String accessToken,
	String refreshToken
) {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	public static TokenStringPairDto fromJson(String json) {
		try {
			return objectMapper.readValue(json, TokenStringPairDto.class);
		} catch (Exception _) {
			throw new InvalidTokenStringPairDtoJsonException();
		}
	}
}
