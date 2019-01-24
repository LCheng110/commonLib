package cn.jpush.im.android.api;


import android.media.AudioFormat;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.jpush.im.android.utils.Logger;

/*仅供私有云使用，公有云打包时需要将此类剔除出jar*/

/**
 * 私有云环境下，使用fastdfs作为文件服务器，由于服务器没有将语音文件转码为mp3的接口，
 * 应用层发送语音时，必须使用此类中的接口可以直接生成mp3格式的语音文件。否则生成的语音文件无法
 * 和iOS端互通。
 */
public class JMVoiceRecorder {
    private static final String TAG = JMVoiceRecorder.class.getSimpleName();
    private static final int DEFAULT_SAMPLING_RATE = 8000;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int DEFAULT_AUDIO_FORMAT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private Object recorderInstance;
    private Method startRecording;
    private Method finishRecording;
    private Method pauseRecording;
    private Method resumeRecording;
    private Method getAmplitudeInDb;

    /**
     * 创建JMVoiceRecorder实例，一般用户直接调用无参的构造函数创建JMVoiceRecorder实例即可。
     * 当遇到个别设备使用默认参数无法正常录音时，可以通过此方法指定采样率等配置。
     *
     * @param samplingRate  采样率
     * @param channelConfig 音频声道配置,常用配置为{@link AudioFormat#CHANNEL_IN_MONO}或
     *                      {@link AudioFormat#CHANNEL_IN_STEREO}等
     * @param encodingBit   音频数据的编码格式,目前只支持{@link AudioFormat#ENCODING_PCM_8BIT}和
     *                      {@link AudioFormat#ENCODING_PCM_16BIT}两种
     */
    private JMVoiceRecorder(int samplingRate, int channelConfig, int encodingBit) {
        try {
            Class recorderCls = Class.forName("cn.jiguang.im.encoder.VoiceRecorder");
            recorderInstance = recorderCls.getDeclaredMethod("getInstance").invoke(null);
            startRecording = recorderCls.getDeclaredMethod("startRecording", String.class, String.class, boolean.class);
            finishRecording = recorderCls.getDeclaredMethod("finishRecording");
            pauseRecording = recorderCls.getDeclaredMethod("pauseRecording");
            resumeRecording = recorderCls.getDeclaredMethod("resumeRecording");
            getAmplitudeInDb = recorderCls.getDeclaredMethod("getAmplitudeInDb");
            recorderCls.getDeclaredMethod("updateConfig", int.class, int.class, int.class).invoke(recorderInstance, samplingRate, channelConfig, encodingBit);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建JMVoiceRecorder实例，使用默认参数构造Recorder.
     * <p>
     * 默认samplingRate={@link JMVoiceRecorder#DEFAULT_SAMPLING_RATE}
     * channelConfig={@link AudioFormat#CHANNEL_IN_MONO}
     * encodingBit={@link AudioFormat#ENCODING_PCM_16BIT}
     */
    public JMVoiceRecorder() {
        this(DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT_ENCODING);
    }

    /**
     * 开始录音，输出语音格式为mp3.
     * 录音完成后需要调用{@link JMVoiceRecorder#finishRecord()},通知底层结束录音。
     * <p>
     * 注意在一次录音结束之前，重复调用startRecord是无效的。录音线程全局唯一。
     *
     * @param filePath 语音文件输出路径
     * @param fileName 语音文件输出文件名
     */
    public void startRecord(String filePath, String fileName) throws IllegalStateException {
        try {
            if (TextUtils.isEmpty(filePath)) {
                Logger.ee(TAG, "file output path should not be empty");
                return;
            }
            if (null != startRecording) {
                startRecording.invoke(recorderInstance, filePath, fileName, true);
            } else {
                Logger.ee(TAG, "JMVoiceRecorder create failed. unable to start record");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof IllegalStateException) {
                throw (IllegalStateException) e.getTargetException();
            }
            e.printStackTrace();
        }
    }

    /**
     * 完成并结束录音，输出录音文件到指定路径。
     */
    public void finishRecord() {
        try {
            if (null != finishRecording) {
                finishRecording.invoke(recorderInstance);
            } else {
                Logger.ee(TAG, "JMVoiceRecorder create failed. unable to finish record");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 录音暂停，调用{@link JMVoiceRecorder#resumeRecord()}后可以恢复继续录音
     */
    public void pauseRecord() {
        try {
            if (null != pauseRecording) {
                pauseRecording.invoke(recorderInstance);
            } else {
                Logger.ee(TAG, "JMVoiceRecorder create failed. unable to pause record");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 恢复之前暂停的录音。
     */
    public void resumeRecord() {
        try {
            if (null != resumeRecording) {
                resumeRecording.invoke(recorderInstance);
            } else {
                Logger.ee(TAG, "JMVoiceRecorder create failed. unable to resume record");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分贝计算公式：20 * Math.log10((double) Math.abs(x) / 32768)。其中x表示采样振幅值。
     * <p>
     * 理论最小分贝为 20 * Math.log10((double) Math.abs(1) / 32768) = -90.30899869919436
     *
     * @return 实时麦克风采集到声音的分贝数
     */
    public double getAmplitudeInDb() {
        try {
            if (null != getAmplitudeInDb) {
                return (Double) getAmplitudeInDb.invoke(recorderInstance);
            } else {
                Logger.ee(TAG, "JMVoiceRecorder create failed. unable to get amplitudeInDb");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return -90.30899869919436;
    }
}
