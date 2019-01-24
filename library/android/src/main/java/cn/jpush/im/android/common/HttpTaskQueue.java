package cn.jpush.im.android.common;

import cn.jpush.im.android.tasks.AbstractTask;

public class HttpTaskQueue extends ObjectQueue {

	public void cancelAllTasksInQueue() {
		m_list.clear();
		m_nItemCnt = 0;
	}

	public void cancelTaskInQueue(int typeId) {
		int i = -1;
		// iterator is not stable,so we use loop to iterate the Victor
		for (i = m_nItemCnt - 1; i >= 0; i--) {
			TaskItem ti = (TaskItem) m_list.elementAt(i);
			if (null == ti || ti.getM_nTypeId() == typeId) {
				m_list.removeElementAt(i);
				m_nItemCnt--;
			}
		}
	}

	public boolean isTaskExists(int typeId) {
		int i = -1;
		// iterator is not stable,so we use loop to iterate the Victor
		for (i = m_nItemCnt - 1; i >= 0; i--) {
			TaskItem ti = (TaskItem) m_list.elementAt(i);
			if (null != ti && ti.getM_nTypeId() == typeId)
				return true;
		}
		return false;
	}

	public synchronized void replaceWithSpecificID(int typeId, AbstractTask task) {
		int i = -1;
		// iterator is not stable,so we use loop to iterate the Victor
		for (i = m_nItemCnt - 1; i >= 0; i--) {
			TaskItem ti = (TaskItem) m_list.elementAt(i);
			if (null != ti && ti.getM_nTypeId() == typeId) {
				ti.setM_oTask(task);
				super.replace(i, ti);
			}
		}
	}

	@Override
	public String toString() {
		int i = -1;
		StringBuffer sb = new StringBuffer();
		// iterator is not stable,so we use loop to iterate the Victor
		for (i = m_nItemCnt - 1; i >= 0; i--) {
			TaskItem ti = (TaskItem) m_list.elementAt(i);
			sb.append(ti.toString());
		}

		return sb.toString();
	}

}
