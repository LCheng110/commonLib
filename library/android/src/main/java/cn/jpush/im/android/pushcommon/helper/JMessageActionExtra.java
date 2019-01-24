package cn.jpush.im.android.pushcommon.helper;


public class JMessageActionExtra {
    /**
     * JCore询问各个sdkclinet 对应action是否允许
     *
     * @param action 需要确认的action{0:是否允许stopPush, ......}
     * @return boolean true为允许,false为不允许
     */
    public boolean checkAction(int action) {
        if (0 == action) { //action == 0 means stopPush
            //上层调用stopPush之后，jmessage依然需要不受影响继续运行，所以这里返回false.
            return false;
        }
        return true;
    }
}
