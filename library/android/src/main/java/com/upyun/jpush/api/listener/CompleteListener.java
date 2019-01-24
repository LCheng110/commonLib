package com.upyun.jpush.api.listener;

public interface CompleteListener {
	void result(boolean isComplete, int statusCode, String reason);
}
