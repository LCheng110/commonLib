package cn.citytag.base.live;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import cn.citytag.base.model.VolumeModel;
import io.agora.rtc.IRtcEngineEventHandler;

/**
 * 作者：M. on 2018/11/16 15:17
 * <p>
 * 邮箱：qiuhuanming@maopp.cn
 */
public class MpRtcEngineEventHandler {

    public static final int DEFAULT_VOLUME = 30;

    private final Context mContext;

    private final ConcurrentHashMap<RtcEngineEventHandler, Integer> mEventHandlerList = new ConcurrentHashMap<>();

    final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        private final static String TAG = "IRtcEngineEventHandler";

        @Override
        public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
            Log.d(TAG, "onFirstRemoteVideoDecoded " + (uid & 0xFFFFFFFFL) + width + " " + height + " " + elapsed);

            Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                RtcEngineEventHandler handler = it.next();
                handler.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
            }
        }

        @Override
        public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
            Log.d(TAG, "onFirstLocalVideoFrame " + width + " " + height + " " + elapsed);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                RtcEngineEventHandler handler = it.next();
                handler.onUserJoined(uid, elapsed);
            }
            Log.d(TAG, "onUserJoined " + uid + " " + elapsed);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            // FIXME this callback may return times
            Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                RtcEngineEventHandler handler = it.next();
                handler.onUserOffline(uid, reason);
            }
            Log.d(TAG, ("onUserOffline " + reason));
        }

        @Override
        public void onUserMuteVideo(int uid, boolean muted) {
            Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                RtcEngineEventHandler handler = it.next();
                handler.onUserMuteVideo(uid, muted);
            }
        }

        @Override
        public void onRtcStats(RtcStats stats) {
        }


        @Override
        public void onLeaveChannel(RtcStats stats) {
            Log.d(TAG, ("RtcStats " + stats));
        }

        @Override
        public void onLastmileQuality(int quality) {
            Log.d(TAG, ("onLastmileQuality " + quality));
        }

        @Override
        public void onError(int err) {
            super.onError(err);
            Log.e(TAG, ("onError " + err));
        }

        @Override
        public void onTokenPrivilegeWillExpire(String token) {
            Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                RtcEngineEventHandler handler = it.next();
                handler.onTokenPrivilegeWillExpire(token);
            }
            Log.d(TAG, ("onTokenPrivilegeWillExpire " + token));
        }

        @Override
        public void onRequestToken() {
            Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                RtcEngineEventHandler handler = it.next();
                handler.onRequestToken();
            }
            Log.d(TAG, ("onRequestToken "));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.d(TAG, ("onJoinChannelSuccess " + channel + " " + uid + " " + (uid & 0xFFFFFFFFL) + " " + elapsed));

            Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                RtcEngineEventHandler handler = it.next();
                handler.onJoinChannelSuccess(channel, uid, elapsed);
            }
        }

        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
            if (speakers.length == 0) {
                return;
            }
            if (speakers.length == 1 && speakers[0].uid == 0) {
                Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
                while (it.hasNext()) {
                    RtcEngineEventHandler handler = it.next();
                    handler.onLocalVolume(totalVolume, totalVolume > 30);
                }
                return;
            } else {
                List<VolumeModel> volumeList = new ArrayList<>();
                for (IRtcEngineEventHandler.AudioVolumeInfo item : speakers) {
                    if (item.volume > 30) {
                        VolumeModel model = new VolumeModel(item.uid, item.volume);
                        volumeList.add(model);
                    }
                }
                if (!volumeList.isEmpty()) {
                    Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
                    while (it.hasNext()) {
                        RtcEngineEventHandler handler = it.next();
                        handler.onRemoteVolume(volumeList);
                    }
                }
            }
        }

        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.d(TAG, ("onRejoinChannelSuccess " + channel + " " + uid + " " + elapsed));
        }

        public void onWarning(int warn) {
            Log.w(TAG, ("onWarning " + warn));
        }

        @Override
        public void onConnectionLost() {
            Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                RtcEngineEventHandler handler = it.next();
                handler.onConnectionLost();
            }
        }

    };

    public MpRtcEngineEventHandler(Context ctx) {
        this.mContext = ctx;
    }

    public void addEventHandler(RtcEngineEventHandler handler) {
        this.mEventHandlerList.put(handler, 0);
    }

    public void removeEventHandler(RtcEngineEventHandler handler) {
        this.mEventHandlerList.remove(handler);
    }
}
