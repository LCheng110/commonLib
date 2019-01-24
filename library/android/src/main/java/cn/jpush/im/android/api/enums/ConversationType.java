package cn.jpush.im.android.api.enums;

public enum ConversationType {
	single("S"), group("G");

	private String label;

	private ConversationType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static ConversationType get(int index) {
		return ConversationType.values()[index];
	}

	public static ConversationType valueOfLabel(String label) {
		if ("S".equals(label)) {
			return single;
		} else if ("G".equals(label)) {
			return group;
		} else {
			throw new IllegalArgumentException("Invalid label string");
		}
	}

}
