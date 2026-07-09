package wiki.feh.kelbeth.tokenapi.dto;

import wiki.feh.kelbeth.tokenapi.domain.APIAccessToken;
import wiki.feh.kelbeth.tokenapi.domain.APIRefreshToken;

public record TokenPairDto (
	APIAccessToken accessToken,
	APIRefreshToken refreshToken
) {}
