/**
 * BasicCallback这个类是对外的，所以需要保证它的包名不变。
 */
package cn.jpush.im.api;

/**
 * 默认 JPush IM 的所有 callback 都是主线程调用回去。一般也不会有问题。
 * <p>
 * 但如果callback 回去后 App 侧有比较重的任务，可选让 JPush IM SDK Callback 回去时在子线程。
 */
public abstract class BasicCallback {

    private boolean isRunInUIThread = true;

    /**
     * Default is running in UI thread.
     */
    public BasicCallback() {
    }

    public BasicCallback(boolean isRunInUIThread) {
        this.isRunInUIThread = isRunInUIThread;
    }

    public boolean isRunInUIThread() {
        return this.isRunInUIThread;
    }

    public void gotResult(int responseCode, String responseMessage, Object... result) {
        gotResult(responseCode, responseMessage);
    }

    /**
     * 异步调用返回结果。
     *
     * @param responseCode    0 表示正常。大于 0 表示异常，responseMessage 会有进一步的异常信息。
     * @param responseMessage 一般异常时会有进一步的信息提示。
     */
    public abstract void gotResult(int responseCode, String responseMessage);

}
