package cn.jiguang.im.encoder;

import android.media.AudioRecord;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.CountDownLatch;

public class DataProcessThread extends Thread implements AudioRecord.OnRecordPositionUpdateListener {

    private static final String TAG = DataProcessThread.class.getSimpleName();

    public static final int PROCESS_STOP = 1;

    private StopHandler handler;

    private byte[] buffer;

    private byte[] mp3Buffer;

    private RingBuffer ringBuffer;

    private FileOutputStream os;

    private int bufferSize;

    private boolean needEncode;

    private CountDownLatch handlerInitLatch = new CountDownLatch(1);

    static class StopHandler extends Handler {

        WeakReference<DataProcessThread> encodeThread;

        public StopHandler(DataProcessThread encodeThread) {
            this.encodeThread = new WeakReference<DataProcessThread>(encodeThread);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PROCESS_STOP) {
                DataProcessThread threadRef = encodeThread.get();
                // Process all data in ring buffer and flush
                // left data to file
                while (threadRef.processData(threadRef.needEncode) > 0) ;
                // Cancel any event left in the queue
                removeCallbacksAndMessages(null);
                if (threadRef.needEncode) {
                    threadRef.flushAndRelease();
                }
                getLooper().quit();
            }
            super.handleMessage(msg);
        }
    }

    ;

    /**
     * Constructor
     *
     * @param ringBuffer
     * @param os
     * @param bufferSize
     */
    public DataProcessThread(RingBuffer ringBuffer, FileOutputStream os,
                             int bufferSize, boolean needEncode) {
        this.os = os;
        this.ringBuffer = ringBuffer;
        this.bufferSize = bufferSize;
        this.needEncode = needEncode;
        buffer = new byte[bufferSize];
        mp3Buffer = new byte[(int) (7200 + (buffer.length * 2 * 1.25))];
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new StopHandler(this);
        handlerInitLatch.countDown();
        Looper.loop();
    }

    /**
     * Return the handler attach to this thread
     *
     * @return
     */
    public Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "Error when waiting handle to init");
        }
        return handler;
    }

    @Override
    public void onMarkerReached(AudioRecord recorder) {
        // Do nothing
    }

    @Override
    public void onPeriodicNotification(AudioRecord recorder) {
        processData(needEncode);
    }

    /**
     * Get data from ring buffer
     * Encode it to mp3 frames using lame encoder
     *
     * @return Number of bytes read from ring buffer
     * 0 in case there is no data left
     */
    private int processData(boolean needEncode) {
        int bytes = ringBuffer.read(buffer, bufferSize);
//        Log.d(TAG, "Read size: " + bytes);
        if (bytes > 0) {
            try {
                if (needEncode) {
                    short[] innerBuf = new short[bytes / 2];
                    ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(innerBuf);
                    int encodedSize = SimpleLame.encode(innerBuf, innerBuf, bytes / 2, mp3Buffer);

                    if (encodedSize < 0) {
                        Log.e(TAG, "Lame encoded size: " + encodedSize);
                    }
                    os.write(mp3Buffer, 0, encodedSize);
                } else {
                    //不转码，直接将buffer中原数据写入os.
                    Log.d(TAG, "no need to encode to mp3 , just write to os");
                    os.write(buffer);
                }
                return bytes;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * Flush all data left in lame buffer to file
     */
    private void flushAndRelease() {
        final int flushResult = SimpleLame.flush(mp3Buffer);

        if (flushResult > 0) {
            try {
                os.write(mp3Buffer, 0, flushResult);
            } catch (final IOException e) {
                // TODO: Handle error when flush
                Log.e(TAG, "Lame flush error");
            }
        }
    }
}
