package cn.jinelei.smart.archwiki.models;

public enum NetworkType {
	CONNECT_WIFI_AND_CELLULAR(0, "CONNECT_WIFI_AND_CELLULAR"),
	CONNECT_WIFI_DISCONNECT_CELLULAR(2, "CONNECT_WIFI_DISCONNECT_CELLULAR"),
	DISCONNECT_WIFI_CONNECT_CELLULAR(4, "DISCONNECT_WIFI_CONNECT_CELLULAR"),
	DISCONNECT_WIFI_AND_CELLULAR(8, "DISCONNECT_WIFI_AND_CELLULAR");

	private int code;
	private String name;

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	NetworkType(int code, String name) {
		this.name = name;
		this.code = code;
	}

	@Override
	public String toString() {
		return "NetworkType{" +
			"code=" + code +
			", name='" + name + '\'' +
			'}';
	}

	public NetworkType valueOf(Object object) {
		int code = -1;
		String name = "";
		if (object instanceof Integer) {
			code = (int) object;
		}
		if (object instanceof String) {
			name = String.valueOf(object);
		}
		for (NetworkType networkType : NetworkType.class.getEnumConstants()) {
			if (networkType.code == code || networkType.name.equals(name)) {
				return networkType;
			}
		}
		return null;
	}
}
