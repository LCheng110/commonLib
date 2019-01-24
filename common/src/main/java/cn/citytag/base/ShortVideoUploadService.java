package cn.citytag.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ShortVideoUploadService extends Service {
    public ShortVideoUploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }



    private void checkMsg(){


    }

    private void uploadImage(){

    }

    private void uploadVideo(){


    }

}
