package cn.citytag.base.event;


import cn.citytag.base.dao.MediaInfo;

/**
 * Created by yangfeng01 on 2017/12/26.
 * 发布动态传递的Event
 */
public class PublishEvent extends BaseEvent {

	public static final int ADD = 11;
	public static final int DELETE = 12;
	public static final int PREVIEW = 13;
	public static final int ADD_MEDIA = 14;
	public static final int MOVE = 15;
	private int operation;
	private MediaInfo mediaInfo;

	public int getOperation() {
		return operation;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}

	public MediaInfo getMediaInfo() {
		return mediaInfo;
	}

	public void setMediaInfo(MediaInfo mediaInfo) {
		this.mediaInfo = mediaInfo;
	}
}
