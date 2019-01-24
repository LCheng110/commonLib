package cn.jpush.im.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;

import org.junit.BeforeClass;

import java.io.File;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseTest {
    private static final String TAG = "BaseTest";

    private static final long USERID = 10000l;

    @BeforeClass
    public static void setUp() {
//        TestLogger.setTestLogger();
        Context context = mock(Context.class);
        Resources resources = mock(Resources.class);
        DisplayMetrics displayMetrics = mock(DisplayMetrics.class);
        ConnectivityManager connectivityManager = mock(ConnectivityManager.class);
        NetworkInfo networkInfo = mock(NetworkInfo.class);
        SharedPreferences sharedPreferences = mock(SharedPreferences.class);
        SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);

        when(context.getApplicationContext()).thenReturn(context);

        when(context.getFilesDir()).thenReturn(new File("foo"));

        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);

        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);

        when(context.getResources()).thenReturn(resources);
        when(resources.getDisplayMetrics()).thenReturn(displayMetrics);

        when(connectivityManager.getActiveNetworkInfo()).thenReturn(networkInfo);
        when(networkInfo.isAvailable()).thenReturn(true);

        when(sharedPreferences.edit()).thenReturn(editor);
        //fake userid.
        when(sharedPreferences.getLong(eq("im_user_id"), anyLong())).thenReturn(USERID);
        //fake network connect state.
        when(sharedPreferences.getBoolean(eq("push_network_connected"), anyBoolean())).thenReturn(true);

        JMessage.init(context, false);
    }
}
