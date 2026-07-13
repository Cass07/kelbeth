package wiki.feh.kelbeth.tokenapi.domain;

import lombok.Getter;

@Getter
public enum JWT_TYPE {
	ACCESS_TOKEN("ACCESS_TOKEN"),
	REFRESH_TOKEN("REFRESH_TOKEN");

	private final String name;

	JWT_TYPE(String type) {
		this.name = type;
	}

	public static JWT_TYPE fromString(String type) {
		for (JWT_TYPE jwtType : JWT_TYPE.values()) {
			if (jwtType.name.equalsIgnoreCase(type)) {
				return jwtType;
			}
		}
		throw new IllegalArgumentException("Unknown JWT_TYPE: " + type);
	}
}
