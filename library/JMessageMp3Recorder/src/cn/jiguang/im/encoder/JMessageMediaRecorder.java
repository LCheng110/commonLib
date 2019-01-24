package cn.jiguang.im.encoder;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 * MediaRecorder包装类，仅用于录制mp3格式音频文件
 */

public class JMessageMediaRecorder extends MediaRecorder {
    private static final String TAG = JMessageMediaRecorder.class.getSimpleName();

    private VoiceRecorder recorder;

    private String outPutFilePath;

    public JMessageMediaRecorder() {
        super();
        recorder = VoiceRecorder.getInstance();
        super.setAudioSource(MediaRecorder.AudioSource.MIC);
    }

    @Override
    public Surface getSurface() {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
        return null;
    }

    @Override
    public void setInputSurface(Surface surface) {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setPreviewDisplay(Surface sv) {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setAudioSource(int audio_source) throws IllegalStateException {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setVideoSource(int video_source) throws IllegalStateException {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setProfile(CamcorderProfile profile) {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setCaptureRate(double fps) {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setOrientationHint(int degrees) {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setLocation(float latitude, float longitude) {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setOutputFormat(int output_format) throws IllegalStateException {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setVideoSize(int width, int height) throws IllegalStateException {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setVideoFrameRate(int rate) throws IllegalStateException {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setMaxDuration(int max_duration_ms) throws IllegalArgumentException {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setMaxFileSize(long max_filesize_bytes) throws IllegalArgumentException {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setAudioEncoder(int audio_encoder) throws IllegalStateException {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setVideoEncoder(int video_encoder) throws IllegalStateException {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setAudioSamplingRate(int samplingRate) {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setAudioChannels(int numChannels) {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setAudioEncodingBitRate(int bitRate) {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setVideoEncodingBitRate(int bitRate) {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setOutputFile(FileDescriptor fd) throws IllegalStateException {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.use setOutputFile(String path) instead.");
    }

    @Override
    public void setOutputFile(String path) throws IllegalStateException {
        outPutFilePath = path;
    }

    @Override
    public void prepare() throws IllegalStateException, IOException {
        Log.w(TAG, "do nothing in prepare.");
    }

    @Override
    public void start() throws IllegalStateException {
        if (null == recorder) {
            throw new IllegalStateException("recorder is released.");
        }
        if (TextUtils.isEmpty(outPutFilePath)) {
            throw new IllegalStateException("should set outPutFilePath First");
        }
        File outPutFile = new File(outPutFilePath);
        try {
            recorder.startRecording(outPutFile.getParent(), outPutFile.getName(), true);
        } catch (IOException e) {
            throw new IllegalStateException("IOException when process outPutFile.", e);
        }
    }

    @Override
    public void stop() throws IllegalStateException {
        if (null == recorder) {
            throw new IllegalStateException("recorder is released.");
        }
        recorder.finishRecording();
    }

    @Override
    public void reset() {
        if (null != recorder) {
            recorder.finishRecording();
        }
        if (!TextUtils.isEmpty(outPutFilePath)) {
            File outPutFile = new File(outPutFilePath);
            outPutFile.delete();
        }
        outPutFilePath = null;
    }

    @Override
    public int getMaxAmplitude() throws IllegalStateException {
        return super.getMaxAmplitude();
    }

    @Override
    public void setOnErrorListener(OnErrorListener l) {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        Log.w(TAG, "operation unsupported in JMessageMediaRecorder.");
    }

    @Override
    public void release() {
        stop();
        recorder = null;
    }
}
