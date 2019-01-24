package jiguang.chat.entity;

import cn.jpush.im.android.api.model.Conversation;

/**
 * Created by yangfeng01 on 2018/6/28.
 */
public class LocationEvent {

	private EventType type;
	private Conversation conversation;

	private double latitude;
	private double longitude;
	private boolean sendLocation;
	private String poi;
	private String locDesc;

	public LocationEvent(EventType type, Conversation conversation, double latitude, double longitude, String locDesc,
						 boolean sendLocation, String poi) {
		this.type = type;
		this.conversation = conversation;
		this.latitude = latitude;
		this.longitude = longitude;
		this.locDesc = locDesc;
		this.sendLocation = sendLocation;
		this.poi = poi;
	}

	public static Event.Builder newBuilder() {
		return new Event.Builder();
	}

	public EventType getType() {
		return type;
	}

	public Conversation getConversation() {
		return conversation;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getLocDesc() {
		return locDesc;
	}

	public boolean isSendLocation() {
		return sendLocation;
	}

	public String getPoi() {
		return poi;
	}

	public static class Builder {
		private EventType type;
		private Conversation conversation;

		private double latitude;
		private double longitude;
		private String locDesc;
		private boolean sendLocation;
		private String poi;

		public LocationEvent.Builder setType(EventType type) {
			this.type = type;
			return this;
		}

		public LocationEvent.Builder setConversation(Conversation conv) {
			this.conversation = conv;
			return this;
		}

		public LocationEvent.Builder setLatitude(double latitude) {
			this.latitude = latitude;
			return this;
		}

		public LocationEvent.Builder setLongitude(double longitude) {
			this.longitude = longitude;
			return this;
		}

		public LocationEvent.Builder setLocDesc(String locDesc) {
			this.locDesc = locDesc;
			return this;
		}

		public LocationEvent.Builder setSendLocation(boolean sendLocation) {
			this.sendLocation = sendLocation;
			return this;
		}

		public LocationEvent.Builder setPoi(String poi) {
			this.poi = poi;
			return this;
		}

		public LocationEvent build() {
			return new LocationEvent(type, conversation, latitude, longitude, locDesc, sendLocation, poi);
		}

	}
}
