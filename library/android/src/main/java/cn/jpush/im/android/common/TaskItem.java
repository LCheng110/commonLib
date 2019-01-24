package cn.jpush.im.android.common;

import cn.jpush.im.android.tasks.AbstractTask;

public class TaskItem {
	private int m_nTypeId;
	private AbstractTask m_oTask;

	public int getM_nTypeId() {
		return m_nTypeId;
	}

	public void setM_nTypeId(int m_nTypeId) {
		this.m_nTypeId = m_nTypeId;
	}

	public AbstractTask getM_oTask() {
		return m_oTask;
	}

	public void setM_oTask(AbstractTask m_oTask) {
		this.m_oTask = m_oTask;
	}

	@Override
	public String toString() {
		return "typeId is " + m_nTypeId + " task is " + m_oTask;
	}
}
