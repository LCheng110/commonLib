package cn.jpush.im.android.api.options;

import cn.jpush.im.api.BasicCallback;

/**
 * 针对消息发送动作的控制选项.
 *
 * @since 2.2.0
 */
public class MessageSendingOptions {

    private boolean retainOffline = true;

    private boolean showNotification = true;

    private boolean isCustomNotficationEnabled = false;

    private boolean needReadReceipt = false;

    private String notificationTitle;

    private String notificationAtPrefix;

    private String notificationText;

    /**
     * 是否让后台在对方不在线时保存这条离线消息，等到对方上线后再推送给对方。
     *
     * @return true - 保存离线消息，false - 不保存这条离线消息。默认为true
     */
    public boolean isRetainOffline() {
        return retainOffline;
    }

    /**
     * 设置是否让后台在对方不在线时保存这条离线消息，等到对方上线后再推送给对方。
     * 如果设置为false，则当发送方发送消息时，如果接收方不在线，则后台不会保存这条离线消息，
     * 这样对方之后上线了也不会收到这条消息。
     *
     * @param retainOffline true - 保存离线消息，false - 不保存这条离线消息。默认为true
     */
    public void setRetainOffline(boolean retainOffline) {
        this.retainOffline = retainOffline;
    }

    /**
     * 接收方是否针对此次消息发送展示通知栏通知
     *
     * @return true - 展示，false - 不展示，默认为true
     */
    public boolean isShowNotification() {
        return showNotification;
    }

    /**
     * 设置针对本次消息发送，是否需要在消息接收方的通知栏上展示通知
     *
     * @param showNotification true - 展示，false - 不展示，默认为true
     */
    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }

    /**
     * 获取当前设置的自定义此条消息在接收方通知栏所展示通知的title
     *
     * @return 自定义的title
     */
    public String getNotificationTitle() {
        return null == notificationTitle ? "" : notificationTitle;
    }

    /**
     * 设置此条消息在接收方通知栏所展示通知的title.
     * 设置之后，消息接收方原本通知栏所展示的通知的title会被替换。
     * 注意需要调用{@link MessageSendingOptions#setCustomNotificationEnabled(boolean)}接口
     * 启用自定义通知栏功能之后，该设置才会生效。
     *
     * @param notificationTitle 在接收方通知栏通知所展示的title
     */
    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    /**
     * 获取当前设置的自定义此条消息在接收方通知栏所展示通知的at信息前缀
     *
     * @return 自定义的at信息前缀
     */
    public String getNotificationAtPrefix() {
        return null == notificationAtPrefix ? "" : notificationAtPrefix;
    }

    /**
     * 设置此条消息在接收方通知栏所展示通知的at信息前缀.
     * 设置之后，如果发送的这条消息是at对方的消息，则会在对方通知栏的text前面加上当前设置的这个前缀。
     * 注意需要调用{@link MessageSendingOptions#setCustomNotificationEnabled(boolean)}接口
     * 启用自定义通知栏功能之后，该设置才会生效。
     *
     * @param notificationAtPrefix 在接收方通知栏通知所展示的text
     */
    public void setNotificationAtPrefix(String notificationAtPrefix) {
        this.notificationAtPrefix = notificationAtPrefix;
    }

    /**
     * 获取当前设置的自定义此条消息在接收方通知栏所展示通知的text
     *
     * @return 自定义的text
     */
    public String getNotificationText() {
        return null == notificationText ? "" : notificationText;
    }

    /**
     * 设置此条消息在接收方通知栏所展示通知的text.
     * 设置之后，消息接收方原本通知栏所展示的通知的text会被替换。
     * 注意需要调用{@link MessageSendingOptions#setCustomNotificationEnabled(boolean)}接口
     * 启用自定义通知栏功能之后，该设置才会生效。
     *
     * @param notificationText 在接收方通知栏通知所展示的text
     */
    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    /**
     * 是否开启了自定义接收方通知栏功能
     *
     * @return true - 开启，false - 关闭
     */
    public boolean isCustomNotficationEnabled() {
        return isCustomNotficationEnabled;
    }

    /**
     * 设置是否启用自定义这条消息在接收方通知栏所展示的文字.
     * 开启之后，可以通过{@link MessageSendingOptions#setNotificationTitle(String)}
     * {@link MessageSendingOptions#setNotificationText(String)}
     * 接口来自定义这条消息在接收方通知栏上所展示的title和text.
     *
     * @param customNotificationEnabled true - 开启 false - 关闭
     */
    public void setCustomNotificationEnabled(boolean customNotificationEnabled) {
        isCustomNotficationEnabled = customNotificationEnabled;
    }

    /**
     * 获取当前消息发送是否需要对方的已读回执，默认为false.
     *
     * @return 是否需要接收方发送已读回执.true - 是,false - 否.
     * @since 2.3.0
     */
    public boolean isNeedReadReceipt() {
        return needReadReceipt;
    }

    /**
     * 设置这条消息的发送是否需要对方发送已读回执
     * 开启之后，对方收到消息后，如果调用了{@link cn.jpush.im.android.api.model.Message#setHaveRead(BasicCallback)}接口，
     * 则作为消息发送方，会收到事件通知{@link cn.jpush.im.android.api.event.MessageReceiptStatusChangeEvent}
     *
     * @param needReadReceipt 是否需要接收方发送已读回执. true - 是,false - 否.
     * @since 2.3.0
     */
    public void setNeedReadReceipt(boolean needReadReceipt) {
        this.needReadReceipt = needReadReceipt;
    }
}
