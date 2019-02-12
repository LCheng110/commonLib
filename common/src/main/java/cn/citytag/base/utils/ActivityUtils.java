package cn.citytag.base.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Stack;

import cn.citytag.base.config.BaseConfig;

/**
 * Created by yangfeng01 on 2017/11/17.
 */
public final class ActivityUtils {
    private static final String TAG = "ActivityUtils";
    // 堆栈管理对象
    private static final ActivityStack STACK = new ActivityStack();

    /**
     * push this activity to stack
     */
    public static void push(Activity activity) {
        //Logger.i(TAG, "push = " + activity);
        STACK.pushToStack(activity);
    }


    /**
     * pop top activity from stack
     */
    public static void pop() {
        Activity activity = STACK.popFromStack();
        //Logger.i(TAG, "pop = " + activity);
        if (activity != null) {
            activity.finish();
        }
    }

    /**
     * remove this activity from stack, maybe is null
     */
    public static void remove(Activity activity) {
        //Logger.i(TAG, "remove = " + activity);
        STACK.removeFromStack(activity);
    }

    /**
     * peek top activity from stack, maybe is null
     */
    public static Activity peek() {
        Activity activity = BaseConfig.getCurrentActivity();
        //Logger.i(TAG, "peek = " + activity);
        return activity;
    }

    /**
     * pop activities until this Activity
     */
    @SuppressWarnings("unchecked")
    public static <T extends Activity> T popUntil(final Class<T> clazz) {
        if (clazz != null) {
            while (!STACK.isEmpty()) {
                final Activity activity = STACK.popFromStack();
                if (activity != null) {
                    if (clazz.isAssignableFrom(activity.getClass())) {
                        return (T) activity;
                    }
                    activity.finish();
                }
            }
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(BaseConfig.getContext(), clazz);
            BaseConfig.getContext().startActivity(intent);
        }
        return null;
    }


    /**
     * 最后一次尝试退出的时间戳
     */
    private static long lastExitPressedMills = 0;
    /**
     * 距上次尝试退出允许的最大时间差
     */
    private static final long MAX_DOUBLE_EXIT_MILLS = 800;

    /**
     * 当APP退出的时候，结束所有Activity
     */
    public static void finishAll() {
        //Logger.i(TAG, "********** Exit **********");
        while (!STACK.isEmpty()) {
            final Activity activity = STACK.popFromStack();
            if (activity != null) {
                //Logger.i(TAG, "Exit = " + activity);
                activity.finish();
            }
        }
        STACK.clear();
    }

    /**
     * exist Activity from Stack
     */
    public static boolean existActivity(Class<?> cls) {
        if (STACK.activityStack == null) {
            return false;
        }
        for (WeakReference<Activity> weakReference : STACK.activityStack) {
            if (weakReference.get().getClass().equals(cls)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 结束Activity
     */
    public static void finish(final Class<? extends Activity> clazz) {
        //Logger.i(TAG, "********** finish activity **********");
        if (clazz != null) {
            Enumeration<WeakReference<Activity>> elements = STACK.activityStack.elements();
            while (elements.hasMoreElements()) {
                Activity activity = elements.nextElement().get();
                if (activity != null) {
                    if (clazz.equals(activity.getClass())) {
                        STACK.removeFromStack(activity);
                        activity.finish();
                        return;
                    }
                }
            }
        }
    }

    /**
     * activity堆栈，用以管理APP中的所有activity
     */
    private static class ActivityStack {
        // activity堆对象
        private final Stack<WeakReference<Activity>> activityStack = new Stack<>();

        /**
         * @return 堆是否为空
         */
        public boolean isEmpty() {
            return activityStack.isEmpty();
        }


        /**
         * 清空Activity栈
         */
        public void clear() {
            activityStack.clear();
        }

        /**
         * 向堆中push此activity
         */
        public void pushToStack(Activity activity) {
            activityStack.push(new WeakReference<>(activity));
        }

        /**
         * @return 从堆栈中pop出一个activity对象
         */
        public Activity popFromStack() {
            while (!activityStack.isEmpty()) {
                final WeakReference<Activity> weak = activityStack.pop();
                final Activity activity = weak.get();
                if (activity != null) {
                    return activity;
                }
            }
            return null;
        }

        /**
         * @return 从堆栈中查看一个对象，且不会pop
         */
        public Activity peekFromStack() {
            while (!activityStack.isEmpty()) {
                final WeakReference<Activity> weak = activityStack.peek();
                final Activity activity = weak.get();
                if (activity != null) {
                    return activity;
                } else {
                    activityStack.pop();
                }
            }
            return null;
        }

        /**
         * @return 从堆栈中删除指定对象
         */
        public boolean removeFromStack(Activity activity) {
            for (WeakReference<Activity> weak : activityStack) {
                final Activity act = weak.get();
                if (act == activity) {
                    return activityStack.remove(weak);
                }
            }
            return false;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // 启动activity
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 启动新的Activity
     */
    public static void push(Activity a, Class<? extends Activity> clazz, Intent intent, int requestCode) {
        if (a == null) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.setClass(BaseConfig.getContext(), clazz);
            if (requestCode >= 0) {
                if (BaseConfig.getCurrentActivity() != null) {
                    BaseConfig.getCurrentActivity().startActivityForResult(intent, requestCode);
                } else {
                    L.d(TAG, "activity is null");
                }
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                BaseConfig.getContext().startActivity(intent);
            }
        } else {
            //Logger.w(TAG, a.getClass().getSimpleName() + " -> " + clazz.getSimpleName());
            intent = getIntent(a, clazz, intent);
            if (requestCode >= 0) {
                a.startActivityForResult(intent, requestCode);
            } else {
                a.startActivity(intent);
            }
        }
    }

    public static void push(Class<? extends Activity> clazz, Intent intent) {
        push(peek(), clazz, intent, -1);
    }

    public static void push(Activity a, Class<? extends Activity> clazz, Intent intent) {
        push(a, clazz, intent, -1);
    }

    public static void push(Class<? extends Activity> clazz, Intent intent, int code) {
        push(peek(), clazz, intent, code);
    }

    public static void push(Class<? extends Activity> clazz) {

        push(peek(), clazz, null, -1);
    }

    public static void push(Activity a, Class<? extends Activity> clazz) {
        push(a, clazz, null, -1);
    }

    public static void push(Activity a, Class<? extends Activity> clazz, int code) {
        push(a, clazz, null, code);
    }

    public static void pushTransition(Class<? extends Activity> clazz, Intent intent,int requestCode, Bundle bundle) {

        if (peek() == null) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.setClass(BaseConfig.getContext(), clazz);
            if (requestCode >= 0) {
                if (BaseConfig.getCurrentActivity() != null) {
                    BaseConfig.getCurrentActivity().startActivityForResult(intent, requestCode,bundle);
                } else {
                    L.d(TAG, "activity is null");
                }
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                BaseConfig.getContext().startActivity(intent,bundle);
            }
        } else {
            //Logger.w(TAG, a.getClass().getSimpleName() + " -> " + clazz.getSimpleName());
            intent = getIntent(peek(), clazz, intent);
            if (requestCode >= 0) {
                peek().startActivityForResult(intent, requestCode,bundle);
            } else {
                peek().startActivity(intent,bundle);
            }
        }
    }

    /**
     * 根据入参，获得intent
     */
    private static Intent getIntent(Activity a, Class<? extends Activity> clazz, Intent intent) {
        if (intent == null) {
            intent = new Intent();
        }
        intent.setClass(a, clazz);
        return intent;
    }

    public static void pop(final Activity a) {
        pop(a, null);
    }

    public static void pop(final Activity a, int code) {
        pop(a, code, null);
    }

    public static void pop(final Activity a, Intent intent) {
        pop(a, Activity.RESULT_OK, intent);
    }

    /**
     * 关闭Activity
     */
    public static void pop(final Activity a, int code, Intent intent) {
        if (intent != null) {
            a.setResult(code, intent);
        }
        a.finish();
    }

    /**
     * 退出应用
     */
    public void exit() {
        try {
            finishAll();
            //杀死该应用程序
            Process.killProcess(Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
