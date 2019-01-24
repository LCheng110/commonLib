package cn.jpush.im.android.pushcommon.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import java.nio.ByteBuffer;

import cn.jiguang.ald.api.JAction;
import cn.jiguang.ald.api.JProtocol;
import cn.jpush.im.android.BuildConfig;
import cn.jpush.im.android.pushcommon.proto.JMessageCommands;
import cn.jpush.im.android.utils.AndroidUtil;
import cn.jpush.im.android.utils.Logger;

/***
 * //TODO::PushService 可以启动成功此类方法则跑在该service对应的进程
 * 否则：目前可以支持让它跑在local 进程
 */
public class JMessageAction implements JAction {
    private static final String TAG = "JMessageAction";

    /**
     * 各个sdk通过此方法告诉core此cmd是否支持
     * 只有支持的情况下才会回调dispatchMessage方法
     */
    public boolean isSupportedCMD(int command) {
        if (command == JMessageCommands.IM.CMD) {
            return true;
        }
        return false;
    }

    /**
     * 接收到消息时JCore会根据isSupportedCMD返回的值来判断
     *
     * @param connection 长连接句柄
     * @param command    message 对应的cmd
     * @param head       message的head
     * @param body       message body
     * @return 返回msg的rid
     */
    public long dispatchMessage(Context context, long connection, int command, Object head, ByteBuffer body) {
        //TODO:: 该方法是用来解析socket收到的消息，针对什么类型的消息会分发到此方法依赖isSupportedCMD这个方法
        if (head == null) {
            Logger.ww(TAG, "#unexcepted - head of message was null");
            return -1;
        }
        if (body == null) {
            Logger.ww(TAG, "#unexcepted - body of message was null");
            return -1;
        }
        try {
            //step 1: 获取body的byte 数组
            byte[] bodyArray = body.array();
            //TODO:: 因为socket 使用aes加密，由于后台根据aes加密规则会增加数据，这里需要将这些附加数据去掉，避免IM PB解析失败
            int realBodyBufferLen = AndroidUtil.getRealBodyLen(bodyArray);
            if (realBodyBufferLen <= 0) {
                Logger.w(TAG, "#unexcepted - invalide body length:" + realBodyBufferLen);
                return -1;
            }
            byte[] realBodyArray = new byte[realBodyBufferLen];
            System.arraycopy(bodyArray, 0, realBodyArray, 0, realBodyBufferLen);

            //step 2: 获取head的byte 数组
            byte[] JheadArray = JProtocol.parseHead(head);

            Bundle bundle = new Bundle();
            bundle.putSerializable(IMResponseHelper.DATA_MSG_CMD, command);
            bundle.putSerializable(IMResponseHelper.DATA_MSG_HEAD, JheadArray);
            bundle.putSerializable(IMResponseHelper.DATA_MSG_BODY, realBodyArray);
            ByteBuffer buffer = ByteBuffer.wrap(JheadArray);
            Long rid = buffer.getLong(4);//从byte数组的第4个字节起，是rid。详细见JHeader定义
            AndroidUtil.sendBroadcast(context,
                    IMResponseHelper.ACTION_IM_RESPONSE,
                    bundle);
            return rid;
        } catch (Exception e) {
            Logger.e(TAG, "#unexcepted - cause by:" + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 之前pushservice中handle的Action 通过回调的方式给回到对应的sdk让对应的sdk来处理
     *
     * @param connection 长连接句柄
     * @param bundle     service解析到的bundle
     * @param object     PushService中的handler
     */
    public void onActionRun(Context context, long connection, Bundle bundle, Object object) {
        //TODO:: 此方法是提供给各个sdk通过startservice方式让PushService帮忙后台处理一些任务
    }

    /**
     * 登录成功，心跳成功等event的回调
     *
     * @param connection 长连接句柄
     * @param eventType  cmd
     */
    //由JiguangTcpManager（从PushService中启动）回调
    public void onEvent(Context context, long connection, int eventType) {
        //step 1: 看IM是否需要判断IM是否初始化之类的动作

        //step 2:根据具体的eventTyp做相关的操作
        switch (eventType) {
            case 0: //socket 注册成功
                break;
            case 1://socket 登录成功
                Bundle bundlelogin = new Bundle();
                //TODO:: 移入源码时需要将注释掉的代码替换当前的代码
                bundlelogin.putString(IMResponseHelper.EXTRA_PUSH_TYPE, IMResponseHelper.EXTRA_PUSH_TYPE_LOGIN);
                AndroidUtil.sendBroadcast(context.getApplicationContext(), IMResponseHelper.ACTION_IM_RESPONSE, bundlelogin);
//                bundlelogin.putString("push_type", "push_login");
//                AndroidUtil.sendBroadcast(context.getApplicationContext(), "cn.jpush.im.android.action.IM_RESPONSE", bundlelogin);
                break;
            case -1://socket 断开链接
                Bundle bundlelogout = new Bundle();
                //TODO:: 移入源码时需要将注释掉的代码替换当前的代码
                bundlelogout.putString(IMResponseHelper.EXTRA_PUSH_TYPE, IMResponseHelper.EXTRA_PUSH_TYPE_LOGOUT);
                AndroidUtil.sendBroadcast(context.getApplicationContext(), IMResponseHelper.ACTION_IM_RESPONSE, bundlelogout);
//                bundlelogout.putString("push_type", "push_logout");
//                AndroidUtil.sendBroadcast(context.getApplicationContext(), "cn.jpush.im.android.action.IM_RESPONSE", bundlelogout);
                break;
            case 19://socket 心跳成功
                break;
        }
    }

    /**
     * 提供超时handler，拿到service的handler在send的时候msg需要添加sdktype的值以便可以正确回调
     * example:
     * Message msg = handler.obtainMessage(0X01);
     * Bundle bundle = new Bundle();
     * bundle.putString("111","11");
     * bundle.putInt("sdktype",1); //此标志
     * msg.setData(bundle);
     * msg.sendToTarget();
     *
     * @param connection 长连接句柄
     * @param object     异步超时时回传的Bundle
     */
    //这个方法从JCore的JPushReceiver中回调
    public void handleMessage(Context context, long connection, Object object) {
        if (object != null) {
            if (object instanceof Bundle) {
            } else if (object instanceof Intent) {
                PluginJCoreHelper.onJCoreIntentNotify(context, (Intent) object);
            } else {
                Logger.dd(TAG, "handleMessage unknown object ");
            }
        }
    }

    /**
     * 提供跨进程访问使用,如果需要使用aidl可以使用
     *
     * @param index sdk client自己定义的参数可用来区分不同的aidl
     *              Ext: 如下是使用示例
     *              //TODO::(getBinderByType第一个参数表明对应的sdk，第二个参数则为sdk对应的JAction中的getBinderByType方法的参数)
     *              IBinder binder = JCoreInterface.getBinderByType(SdkType.JMESSAGE.name(),"");
     *              if(binder!=null){
     *              iPush = IPush.Stub.asInterface(binder);
     *              }
     */
    public IBinder getBinderByType(String index) {
        return null;
    }

    /**
     * 获取sdk的版本信息
     *
     * @return
     */
    public String getSdkVersion() {
        return BuildConfig.SDK_VERSION;
    }

    /**
     * 分发超时信息
     *
     * @param connection
     * @param rid
     * @param command
     */
    public void dispatchTimeOutMessage(Context context, long connection, long rid, int command) {
        //TODO:: 当前IM有其自定义的消息超时的机制，后续可以考虑让IM的消息超时机制交给JCore统一管理，当前不做任何改动
    }
}
