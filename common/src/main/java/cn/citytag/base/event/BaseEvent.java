package cn.citytag.base.event;

/**
 * Created by yangfeng01 on 2017/12/19.
 */

public class BaseEvent {

	private int type;
	private Object object;

	public BaseEvent() {

	}

	public BaseEvent(int type) {
		this.type = type;
	}

	public BaseEvent(int eventType, Object object) {
		this.type = eventType;
		this.object = object;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
}
