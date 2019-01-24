package cn.jpush.im.android.common;

import android.os.Process;

import cn.jpush.im.android.tasks.AbstractTask;
import cn.jpush.im.android.utils.Logger;

/**
 * @author xiongtc
 */
public class TaskEngine implements Runnable {

    private static final String TAG = "TaskEngine";

    HttpTaskQueue m_qTask = new HttpTaskQueue();

    TaskItem m_curTask = null;

    private static Object locker = new Object();

    private static TaskEngine m_instance = null;

    TaskItem m_tiEndThreadFlag = new TaskItem();

    private TaskEngine() {
//        if (IMConfigs.getUserName() != null) {
//            List<LocalTask> list = LocalTask.queryAllInBackground();
//            List<TaskItem> itemList = LocalTask.LocalTaskToTaskItem(list);
//            for (TaskItem item : itemList) {
//                addTaskToTail(item.getM_oTask(), item.getM_nTypeId());
//            }
//            Thread t = new Thread(this);
//            t.start();
//        }
    }

    public static TaskEngine getInstance() {
        synchronized (locker) {
            if (null == m_instance) {
                m_instance = new TaskEngine();
            }
        }

        return m_instance;
    }

    public static void clearInstance() {
        synchronized (locker) {
            if (null != m_instance) {
                m_instance.endThread();
            }
            m_instance = null;
        }
    }

    private void endThread() {

        m_qTask.cancelAllTasksInQueue();
        m_tiEndThreadFlag.setM_nTypeId(-1);
        m_tiEndThreadFlag.setM_oTask(null);

        m_qTask.putTail(m_tiEndThreadFlag);
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        Logger.d(TAG, "TaskEngine thread started...");
        while (true) {
            try {
                setCurTaskItem(null);
                // System.out.println("[TaskEngine.run] Task get. free: "+Runtime.getRuntime().freeMemory()+", total: "+Runtime.getRuntime().totalMemory());
                TaskItem ti = (TaskItem) m_qTask.consume();
                if (m_tiEndThreadFlag == ti) {
                    break;
                }
//                else {
//                    LocalTask.deleteFirst();
//                }
                if (null != ti && null != ti.getM_oTask()) {

                    setCurTaskItem(ti);

                    if (ti.getM_oTask().isCanceled()) {
                        continue;
                    }

                    // execute task
                    try {
                        ti.getM_oTask().execute();
                    } catch (Throwable ex) {
                        Logger.e(TAG, "1", ex);
                    }

                    if (ti.getM_oTask().isCanceled()) {
                        continue;
                    }

//					try {
//						ti.getM_oTask().doPostExecute(result);
//					} catch (Throwable t) {
//
//						Logger.e(TAG, "doPostExecute ", t);
//					}
//
//					if (ti.getM_oTask().isCanceled()) {
//						continue;
//					}

//					ti = null;
                    setCurTaskItem(null);
                }
                // Thread.yield();
            } catch (Exception ex) {
                Logger.e(TAG, ex.toString());
            }
        }

        Logger.d(TAG, "TaskEngine thread ended...");
    }

    private synchronized void setCurTaskItem(TaskItem ti) {
        m_curTask = ti;
    }

    /**
     * 将任务添加到队列尾，等待执行
     *
     * @param tsk    task to add
     * @param typeId task ordinal id in {@linkplain BackgroundTasks}
     */
    public void addTaskToTail(AbstractTask tsk, int typeId) {

        TaskItem item = new TaskItem();
        item.setM_nTypeId(typeId);
        item.setM_oTask(tsk);
        m_qTask.putTail(item);

    }

//	/**
//	 * 立即执行任务
//	 *
//	 * @param tsk
//	 *            task to execute
//	 */
//	public void executeImmediately(final AbstractTask tsk) {
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				ResponseWrapper result = null;
//				try {
//					result = tsk.execute();
//					tsk.doPostExecute(result);
//				} catch (Throwable ex) {
//					Logger.e(TAG, "executeImmediately", ex);
//				}
//			}
//		}).start();
//	}

    /**
     * 判断队列中是否存在相同的任务
     *
     * @param typeId task position in {@linkplain BackgroundTasks}
     * @return true if exists
     */
    public synchronized boolean isTaskExists(int typeId) {
        return m_qTask.isTaskExists(typeId);
    }

    /**
     * 替换队列中指定类别的任务
     *
     * @param task   the new task
     * @param typeId task ordinal id in {@linkplain BackgroundTasks}
     */
    public synchronized void replaceTask(AbstractTask task, int typeId) {
        Logger.d(TAG, "before replace task!! queue = " + m_qTask.toString());
        m_qTask.replaceWithSpecificID(typeId, task);
        Logger.d(TAG, "after replace task!! queue = " + m_qTask.toString());
    }

    /**
     * 取消指定类别任务
     *
     * @param typeId task ordinal id in {@linkplain BackgroundTasks}
     */
    public synchronized void cancelTasks(int typeId) {
        m_qTask.cancelTaskInQueue(typeId);
        // check if current task need to cancel
        if (null != m_curTask && null != m_curTask.getM_oTask()
                && m_curTask.getM_nTypeId() == typeId) {
            m_curTask.getM_oTask().cancel();
        }
    }

    /**
     * 取消所有任务，包括正在运行和队列中等待执行的任务
     */
    public synchronized void cancelAllTasks() {
        m_qTask.cancelAllTasksInQueue();
        // cancel current task
        if (null != m_curTask && null != m_curTask.getM_oTask()) {
            m_curTask.getM_oTask().cancel();
        }
    }
}
