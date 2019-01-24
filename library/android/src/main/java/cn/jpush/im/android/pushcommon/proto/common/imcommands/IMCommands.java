package cn.jpush.im.android.pushcommon.proto.common.imcommands;


public class IMCommands {
	public static final int PUSH_MSG_TYPE_FOR_IM = 100;

	public interface Login {
		public static final int CMD = 1;
		public static final int VERSION = 1;
	}

	public interface Logout {
		public static final int CMD = 2;
		public static final int VERSION = 1;
	}

    public interface SingleMsg {
		public static final int CMD = 3;
		public static final int VERSION = 1;
	}

    public interface GroupMsg {
		public static final int CMD = 4;
		public static final int VERSION = 1;
	}

    public interface AddFriend {
		public static final int CMD = 5;
		public static final int VERSION = 1;
	}

    public interface DelFriend {
		public static final int CMD = 6;
		public static final int VERSION = 1;
	}

    public interface UpdateMemo {
		public static final int CMD = 7;
		public static final int VERSION = 1;
	}

    public interface CreateGroup {
		public static final int CMD = 8;
		public static final int VERSION = 1;
	}

    public interface ExitGroup {
		public static final int CMD = 9;
		public static final int VERSION = 1;
	}

    public interface AddGroupMember {
		public static final int CMD = 10;
		public static final int VERSION = 1;
	}

    public interface DelGroupMember {
		public static final int CMD = 11;
		public static final int VERSION = 1;
	}

    public interface UpdateGroupInfo {
		public static final int CMD = 12;
		public static final int VERSION = 1;
	}

    public interface EventNotification {
		public static final int CMD = 13;
		public static final int VERSION = 1;
	}

    public interface ChatMsgSync {
		public static final int CMD = 14;
		public static final int VERSION = 1;
	}

	public interface EventSync {
		public static final int CMD = 15;
		public static final int VERSION = 1;
	}

	public interface EventAnswer {
		public static final int CMD = 16;
		public static final int VERSION = 1;
	}

    public interface AddBlackList {
        public static final int CMD = 18;
        public static final int VERSION = 1;
    }

    public interface DelBlackList {
        public static final int CMD = 19;
        public static final int VERSION = 1;
    }

	public interface ReportInformation {
		public static final int CMD = 23;
		public static final int VERSION = 1;
	}

	public interface AddMsgnoDisturbSingle {
		public static final int CMD = 31;
		public static final int VERSION = 1;
	}

	public interface DeleteMsgnoDisturbSingle {
		public static final int CMD = 32;
		public static final int VERSION = 1;
	}

	public interface AddMsgnoDisturbGroup {
		public static final int CMD = 33;
		public static final int VERSION = 1;
	}

	public interface DeleteMsgnoDisturbGroup {
		public static final int CMD = 34;
		public static final int VERSION = 1;
	}

	public interface AddMsgnoDisturbGlobal {
		public static final int CMD = 35;
		public static final int VERSION = 1;
	}

	public interface DeleteMsgnoDisturbGlobal {
		public static final int CMD = 36;
		public static final int VERSION = 1;
	}

	public interface AddGroupToBlock {
		public static final int CMD = 42;
		public static final int VERSION = 1;
	}

	public interface DelGroupFromBlock {
		public static final int CMD = 43;
		public static final int VERSION = 1;
	}

	public interface AddMsgReceipt {
		public static final int CMD = 37;
		public static final int VERSION = 1;
	}

	public interface DelMsgReceipt {
		public static final int CMD = 38;
		public static final int VERSION = 1;
	}

	public interface SyncCheck {
		public static final int CMD = 39;
		public static final int VERSION = 1;
	}

	public interface SyncConversationResp {
		public static final int CMD = 40;
		public static final int VERSION = 1;
	}
	public interface SyncConversationACK {
		public static final int CMD = 41;
		public static final int VERSION = 1;
    }

    public interface MsgRetract {
        public static final int CMD = 55;
        public static final int VERSION = 1;
    }

	public interface TransCommand {
		public static final int CMD = 57;
		public static final int VERSION = 1;
	}

	public interface CommandNotification {
		public static final int CMD = 58;
		public static final int VERSION = 1;
	}

    public interface SyncEventResp {
        int CMD = 60;
        int VERSION = 1;
    }

    public interface SyncEventACK {
        int CMD = 61;
        int VERSION = 1;
    }

	public interface UpdateUnreadCount {
		int CMD = 66;
		int VERSION = 1;
	}

	public interface SyncMsgReceiptResp {
		int CMD = 63;
		int VERSION = 1;
	}

	public interface SyncMsgReceiptACK {
		int CMD = 64;
		int VERSION = 1;
	}

	public interface MsgReceiptReport {
		int CMD = 67;
		int VERSION = 1;
	}

	public interface MsgReceiptChange {
		int CMD = 68;
		int VERSION = 1;
	}
}

