package cn.jiguang.im.encoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoiceRecorder {

    private static final String TAG = VoiceRecorder.class.getSimpleName();

    private static final int DEFAULT_SAMPLING_RATE = 8000;

    private static final int FRAME_COUNT = 160;

    /* Encoded bit rate. MP3 file will be encoded with bit rate 32kbps */
    private static final int BIT_RATE = 32;

    private static VoiceRecorder instance;

    private AudioRecord audioRecord = null;

    private double amplitudeInDb = internalGetAmplitudeDb(-1);

    private int bufferSize;

    private RingBuffer ringBuffer;

    private byte[] buffer;

    private FileOutputStream os = null;

    private DataProcessThread processThread;

    private int samplingRate;

    private int channelConfig;

    private int audioFormat;

    private int bytesPerFrame;

    private AtomicBoolean isRecording = new AtomicBoolean(false);
    private AtomicBoolean isPause = new AtomicBoolean(false);

    public synchronized static VoiceRecorder getInstance() {
        Log.d(TAG, "instance is " + instance);
        if (null == instance) {
            instance = new VoiceRecorder();
        }
        return instance;
    }

    public void updateConfig(int samplingRate, int channelConfig,
                             int audioFormat) {
        this.samplingRate = samplingRate;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
        switch (audioFormat) {
            case AudioFormat.ENCODING_PCM_8BIT:
                bytesPerFrame = 1;
                break;
            case AudioFormat.ENCODING_PCM_16BIT:
                bytesPerFrame = 2;
                break;
            default:
                bytesPerFrame = 2;
        }
    }

    private VoiceRecorder(int samplingRate, int channelConfig,
                          int audioFormat) {
        updateConfig(samplingRate, channelConfig, audioFormat);
    }

    /**
     * Default constructor. Setup recorder with default sampling rate 1 channel,
     * 16 bits pcm
     */
    private VoiceRecorder() {
        this(DEFAULT_SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    /**
     * Start recording. Create an encoding thread. Start record from this
     * thread.
     *
     * @throws IOException
     */
    public synchronized void startRecording(final String filePath, final String fileName, final boolean needEncode) throws IOException {
        if (isRecording.getAndSet(true)) {
            Log.d(TAG, "recording was started already,do nothing");
            return;
        }
        Log.d(TAG, "Start recording");
        Log.d(TAG, "BufferSize = " + bufferSize);
        // Initialize audioRecord if it's null.
        if (audioRecord == null) {
            initAudioRecorder(filePath, fileName, needEncode);
        }
        audioRecord.startRecording();
        new Thread() {

            @Override
            public void run() {
                try {
                    while (isRecording.get()) {
                        while (isPause.get()) ;
                        int bytes = audioRecord.read(buffer, 0, bufferSize);
                        if (bytes > 0) {
                            ringBuffer.write(buffer, bytes);

                            //关于分贝的算法参考资料：http://stackoverflow.com/questions/2917762/android-pcm-bytes
                            //使用16Bit编码前提下，每两个字节表示一次采样，所以需要将两个字节拼成一个integer来表示这次采样的振幅
                            int amplitude = (buffer[0] & 0xff) << 8 | buffer[1];
                            // Determine amplitude
                            amplitudeInDb = internalGetAmplitudeDb(amplitude);
                        }
                    }
                    // release and finalize audioRecord

                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;

                    // stop the encoding thread and try to wait
                    // until the thread finishes its job
                    Message msg = Message.obtain(processThread.getHandler(),
                            DataProcessThread.PROCESS_STOP);
                    msg.sendToTarget();

                    Log.d(TAG, "waiting for encoding thread");
                    processThread.join();
                    Log.d(TAG, "done encoding thread");
                } catch (InterruptedException e) {
                    Log.d(TAG, "Failed to join encode thread");
                } catch (IllegalStateException e) {
                    Log.d(TAG, "failed to manage audio record");
                    if (null != audioRecord) {
                        audioRecord.release();
                        audioRecord = null;
                    }
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }.start();
    }

    /**
     * finish recording
     */
    public synchronized void finishRecording() {
        Log.d(TAG, "finish recording");
        isRecording.set(false);
        isPause.set(false);
        amplitudeInDb = internalGetAmplitudeDb(-1);
    }

    /**
     * pause recording
     */
    public synchronized void pauseRecording() {
        Log.d(TAG, "pause recording");
        isPause.set(true);
    }

    /**
     * resume recording
     */
    public synchronized void resumeRecording() {
        Log.d(TAG, "resume recording");
        isPause.set(false);
    }

    public double getAmplitudeInDb() {
        return amplitudeInDb;
    }

    private double internalGetAmplitudeDb(int amplitude) {
        return 20 * Math
                .log10((double) Math.abs(amplitude) / 32768);
    }

    /**
     * Initialize audio recorder
     */
    private void initAudioRecorder(String filePath, String fileName, boolean needEncode) throws IOException {
//        int bytesPerFrame = audioFormat.getBytesPerFrame();
        /* Get number of samples. Calculate the buffer size (round up to the
           factor of given frame size) */
        int frameSize = AudioRecord.getMinBufferSize(samplingRate,
                channelConfig, audioFormat) / bytesPerFrame;
        if (frameSize % FRAME_COUNT != 0) {
            frameSize = frameSize + (FRAME_COUNT - frameSize % FRAME_COUNT);
            Log.d(TAG, "Frame size: " + frameSize);
        }

        bufferSize = frameSize * bytesPerFrame;

		/* Setup audio recorder */
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                samplingRate, channelConfig, audioFormat,
                bufferSize);

        // Setup RingBuffer. Currently is 10 times size of hardware buffer
        // Initialize buffer to hold data
        ringBuffer = new RingBuffer(10 * bufferSize);
        buffer = new byte[bufferSize];

        // Initialize lame buffer
        // mp3 sampling rate is the same as the recorded pcm sampling rate
        // The bit rate is 32kbps
        SimpleLame.init(samplingRate, 1, samplingRate, BIT_RATE);

        File fileOutPath = new File(filePath);
        if (!fileOutPath.exists()) {
            fileOutPath.mkdirs();
        }

        os = new FileOutputStream(new File(filePath, fileName));

        // Create and run thread used to encode data
        // The thread will
        processThread = new DataProcessThread(ringBuffer, os, bufferSize, needEncode);
        processThread.start();
        audioRecord.setRecordPositionUpdateListener(processThread, processThread.getHandler());
        audioRecord.setPositionNotificationPeriod(FRAME_COUNT);
    }
}