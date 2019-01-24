package cn.jpush.im.android.api.exceptions;

/**
 * Created by ${chenyn} on 16/7/12.
 *
 * @desc :自定义异常
 */
public class JMessageException extends Exception {

    private static final long serialVersionUID = 4776011594198799311L;

    public JMessageException() {
    }

    public JMessageException(String detailMessage) {
        super(detailMessage);
    }

    public JMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public JMessageException(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }

}
