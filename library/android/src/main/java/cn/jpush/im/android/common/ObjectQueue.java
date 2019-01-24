package cn.jpush.im.android.common;

import java.util.Vector;

public class ObjectQueue {
    /**
     * the item cnt
     */
    protected int m_nItemCnt = 0;
    /**
     * the vector that hold the objects
     */
    protected Vector<Object> m_list = null;

    /**
     * Constructor of object queue, create member variables.
     */
    public ObjectQueue() {
        m_nItemCnt = 0;
        m_list = new Vector<Object>();
    }

    /**
     * Put a object into the object queue tail, and notify the waiting thread.
     *
     * @param obj object
     * @return position in this queue
     */
    public synchronized int putTail(Object obj) {
        // add the object
        m_list.addElement(obj);

        // increase the item count
        m_nItemCnt++;

        wakeUpThread();

        return m_list.indexOf(obj);
    }

    /**
     * Put a object into the object queue tail, and notify the waiting thread.
     *
     * @param obj object
     * @return position in this queue
     */
    public synchronized int putHead(Object obj) {
        // add the object
        m_list.insertElementAt(obj, 0);

        // increase the item count
        m_nItemCnt++;

        wakeUpThread();

        return m_list.indexOf(obj);
    }

    /**
     * Replace the Object in a specific position
     *
     * @param position the position of a replaced object
     * @param obj      the new object
     */
    public synchronized void replace(int position, Object obj) {

        m_list.set(position, obj);

    }

    /**
     * get the first object in this queue, don't remove it, if there're no
     * object it will not wait.
     *
     * @return first object in this queue
     */
    public synchronized Object get() {
        // wait for object
        if (m_nItemCnt < 1) {
            return null;
        }

        // get one object from head
        return m_list.firstElement();
    }

    /**
     * consume the first object in this queue.object will be removed from the
     * queue. if there're no object,it will wait until any object is available.
     *
     * @return first object in this queue
     */
    public synchronized Object consume() {
        // wait for object
        while (m_nItemCnt < 1) {
            try {
                wait();
                // Thread.sleep(200);
            } catch (Exception e) {
                // #debug
                e.printStackTrace();
                return null;
            }
        }

        // remove one object from head
        Object obj = m_list.firstElement();
        m_list.removeElementAt(0);

        // decrease item count
        if (m_nItemCnt > 0) {
            m_nItemCnt--;
        }
        return obj;
    }

    public synchronized Object consumeWithoutWait() {
        // wait for object
        if (m_nItemCnt < 1) {
            return null;
        }

        // remove one object from head
        Object obj = m_list.firstElement();
        m_list.removeElementAt(0);

        // decrease item count
        if (m_nItemCnt > 0) {
            m_nItemCnt--;
        }

        return obj;
    }

    /**
     * Check if we have items in the queue
     *
     * @return boolean
     */
    public synchronized boolean hasItem() {
        return m_nItemCnt > 0;
    }

    protected void wakeUpThread() {
        // wake up one waiting thread
        try {
            notify();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void clear() {
        m_list.clear();
        m_nItemCnt = 0;
    }
}
