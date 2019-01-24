package cn.jpush.im.android.api.enums;

public enum MessageStatus {
	created,send_success, send_fail, send_going, send_draft, receive_success, receive_going, receive_fail;
	public static MessageStatus get(int index) {
		return MessageStatus.values()[index];
	}

	public static boolean isSendStatus(MessageStatus status) {
		if (status == MessageStatus.send_success || status == MessageStatus.send_fail
				|| status == MessageStatus.send_going || status == MessageStatus.send_draft) {
			return true;
		}
		return false;
	}

	public static boolean isReceiveStatus(MessageStatus status) {
		if (isSendStatus(status) || status == MessageStatus.created) {
			return false;
		}
		return true;
	}
}
