package cn.citytag.base.live;

import java.util.List;

import cn.citytag.base.model.VolumeModel;

/**
 * 作者：M. on 2018/11/16 15:18
 * <p>
 * 邮箱：qiuhuanming@maopp.cn
 */
public interface RtcEngineEventHandler {
    void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed);

    void onJoinChannelSuccess(String channel, int uid, int elapsed);

    void onUserOffline(int uid, int reason);

    void onUserJoined(int uid, int elapsed);

    void onUserMuteVideo(int uid, boolean muted);

    void onTokenPrivilegeWillExpire(String token);

    void onRequestToken();

    //本地音量变化
    void onLocalVolume(int volume, boolean isSpeaking);

    //远端用户 volume > 30 用户
    void onRemoteVolume(List<VolumeModel> userIds);

    void onConnectionLost();
}
