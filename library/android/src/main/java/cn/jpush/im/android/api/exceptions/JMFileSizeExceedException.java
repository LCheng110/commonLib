package cn.jpush.im.android.api.exceptions;

/**
 * Created by ${chenyn} on 16/7/18.
 *
 * @desc :文件大小异常
 */
public class JMFileSizeExceedException extends JMessageException {
    private static final long serialVersionUID = -3365275706523851139L;

    public JMFileSizeExceedException(String detailMessage) {
        super(detailMessage);
    }
}
