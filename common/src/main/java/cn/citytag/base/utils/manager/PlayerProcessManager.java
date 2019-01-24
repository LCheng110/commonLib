package cn.citytag.base.utils.manager;

import java.util.HashSet;

import cn.citytag.base.config.BaseConfig;

/**
 * Author: Lusheast
 * E-mail：zhangxiaoyu@maopp.cn
 * Date: on 2019/1/15 22:26
 * Desc:
 */
public class PlayerProcessManager {
    public volatile static PlayerProcessManager playerProcessManager;

    HashSet<String> urlSet;

    private PlayerProcessManager() {
        VideoPreferencesUtil.getInstance().init(BaseConfig.getContext(), "player_process");
        urlSet = new HashSet<String>();
    }

    public static PlayerProcessManager getInstance() {
        if (playerProcessManager == null) {
            synchronized (PlayerProcessManager.class) {
                if (playerProcessManager == null) {
                    playerProcessManager = new PlayerProcessManager();
                }
            }
        }
        return playerProcessManager;
    }

    /**
     * 保存当前进度
     *
     * @param videoUrl
     * @param process
     */
    public void savePlayerProcess(String videoUrl, int process) {
        if (urlSet == null){
            urlSet = new HashSet<>();
        }
        urlSet.add(videoUrl);
        VideoPreferencesUtil.getInstance().saveParam(videoUrl, process);
    }

    /**
     * 获取当前进度
     *
     * @param videoUrl
     */
    public int getPlayerProcess(String videoUrl) {
        int currentProcess = (int) VideoPreferencesUtil.getInstance().getParam(videoUrl, 0);
        return currentProcess;
    }

    /**
     * 移除进度
     *
     * @param videoUrl
     */
    public void removePlayerProcess(String videoUrl) {
        if (urlSet == null){
            urlSet = new HashSet<>();
        }
        urlSet.remove(videoUrl);
        VideoPreferencesUtil.getInstance().remove(videoUrl);
    }

    /**
     * 移除所有记录
     */
    public void removeAllPlayerProcess() {
        if (urlSet == null || urlSet.size() == 0) {
            return;
        }
        for (String url : urlSet) {
            VideoPreferencesUtil.getInstance().remove(url);
        }
        urlSet = null;
    }

    public HashSet getAllList(){
        if (urlSet == null) {
            return null;
        }else{
            return urlSet;
        }
    }


}
