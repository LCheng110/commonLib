package cn.jpush.im.android.helpers.sync;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.helpers.EventProcessor;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.helpers.eventsync.EventNotificationWrapperBuilder;
import cn.jpush.im.android.helpers.eventsync.GroupEventsWrapper;
import cn.jpush.im.android.helpers.eventsync.Kind7EventsWrapper;
import cn.jpush.im.android.pushcommon.proto.Event;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMProtocol;
import cn.jpush.im.android.storage.EventIdListManager;
import cn.jpush.im.android.tasks.GetEventNotificationTaskMng;
import cn.jpush.im.android.tasks.GetGroupInfoTask;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/6/27.
 */

public class SyncEventRespHandler extends SyncRespBaseHandler {

    private static final String TAG = "SyncEventRespHandler";
    private static final int ERRORCODE_GROUP_NOT_EXISTS = 898006;


    private static SyncEventRespHandler instance;
    //缓存的SyncEventPageContent会话对象，用于多页之间相同会话的合并, key -- conv_id, Value -- SyncEventPageContent
    private Map<String, SyncEventPageContent> receivedEventPageContents = new LinkedHashMap<String, SyncEventPageContent>();

    private SyncEventRespHandler() {
        super(GetEventNotificationTaskMng.getInstance());
    }

    public static synchronized SyncEventRespHandler getInstance() {
        if (null == instance) {
            instance = new SyncEventRespHandler();
        }
        return instance;
    }

    @Override
    protected int getTotalPage(IMProtocol protocol) {
        Event.SyncEventResp resp = (Event.SyncEventResp) protocol.getEntity();
        return null == resp ? 0 : resp.getTotalPages();
    }

    @Override
    protected int getPageNo(IMProtocol protocol) {
        Event.SyncEventResp resp = (Event.SyncEventResp) protocol.getEntity();
        return null == resp.getEventPage() ? 0 : resp.getEventPage().getPageNo();
    }

    @Override
    protected long getSyncKey(IMProtocol protocol) {
        Event.SyncEventResp resp = (Event.SyncEventResp) protocol.getEntity();
        Logger.d(TAG, "[getSyncKey] , resp.getSyncKey() = " + resp.getSyncKey());
        if (0 != resp.getSyncKey()) {
            syncKey = resp.getSyncKey();
        }
        return syncKey;
    }

    @Override
    protected int getTotalEntityCount() {
        return receivedEventPageContents.size();
    }

    @Override
    protected void sendSyncACK(long syncKey) {
        if (uid != IMConfigs.getUserID()) {
            Logger.ww(TAG, "current uid not match uid in protocol. do not send sync event ack back.");
            return;
        }

        //将syncKey本地化，同时将syncKey通过ACK回给后台
        Logger.ii(TAG, "send sync event ack back == syncEventKey is " + syncKey);
        IMConfigs.setSyncEventKey(IMConfigs.getUserID(), syncKey);
        RequestProcessor.imSyncEventACK(JMessage.mContext, syncKey, CommonUtils.getSeqID());
    }

    @Override
    public void clearCache() {
        super.clearCache();
        receivedEventPageContents.clear();
    }

    @Override
    protected void startLocalize(BasicCallback callback) {
        super.startLocalize(callback);
        //开始本地化处理前，先清一下RequestPackager的缓存
        requestPackager.clearCache();
        //先清掉计数器。
        finishedCounter.set(0);
        //清空缓存的会话EventPageContents.防止出现两次SyncCheck挤到一起时，前后的EventPageContents重复计算。
        receivedEventPageContents.clear();
        //将所有同步页按照其中的会话重新聚合。
        for (Object value : receivedPages.values()) {
            Event.SyncEventResp resp = (Event.SyncEventResp) value;
            Logger.d(TAG, "[startLocalize], resp.getSyncKey() = " + resp.getSyncKey());
            if (0 != resp.getSyncKey()) {
                syncKey = resp.getSyncKey();
            }

            List<Event.EventPageContent> pageContentList = resp.getEventPage().getContentListList();
            for (Event.EventPageContent pageContent : pageContentList) {
                String id = pageContent.getId().toStringUtf8();
                List<Event.EventMeta> eventMetaList = pageContent.getEventListList();
                SyncEventPageContent contentEntity = receivedEventPageContents.get(id);
                if (null != contentEntity) {
                    contentEntity.addAllToEventMetaList(eventMetaList);
                } else {
                    contentEntity =
                            new SyncEventPageContent(id, pageContent.getEventKind(), pageContent.getFullUpdate(), pageContent.getEventListList(), operationWatcher);
                    receivedEventPageContents.put(id, contentEntity);
                }
            }
        }

        //通知BaseHandler更新entitiesToLocalize这个变量(必须在下面单个SyncEventPageContent startLocalize之前调用)
        onEntitiesToLocalizeCntUpdated(receivedEventPageContents.size());

        for (SyncEventPageContent content : receivedEventPageContents.values()) {
            content.startLocalize();
        }

    }

    private class SyncEventPageContent extends SyncRespEntity {
        private int kind;
        private boolean fullUpdate;
        private List<Event.EventMeta> eventMetaList;

        SyncEventPageContent(String id, int kind, boolean fullUpdate, List<Event.EventMeta> eventMetaList, SyncRespBaseHandler.Watcher operationWatcher) {
            super(id, operationWatcher);
            this.kind = kind;
            this.fullUpdate = fullUpdate;
            this.eventMetaList = eventMetaList;
        }

        void addAllToEventMetaList(List<Event.EventMeta> eventMetaList) {
            if (null != eventMetaList) {
                this.eventMetaList.addAll(eventMetaList);
            }
        }

        @Override
        protected void startLocalize() {
            Logger.ii(TAG, " id = " + convId + " kind = " + kind + " fullUpdate = " + fullUpdate +
                    " event meta list totalCount = " + eventMetaList.size());

            List<Long> eventIdList = new ArrayList<Long>();
            for (Event.EventMeta meta : eventMetaList) {
                long eventId = meta.getEid();
                Logger.d(TAG, "received event id " + eventId + " ctime = " + meta.getCtime());
                eventIdList.add(eventId);
            }

            long gid = 0L;
            if (GroupEventsWrapper.EVENT_KIND_3 == kind) {
                try {
                    gid = Long.parseLong(convId.split("_")[1]);
                } catch (NumberFormatException e) {
                    Logger.ee(TAG, "error occurs when parse gid from conv_id");
                }
            }
            //去重
            EventIdListManager.getInstance().removeDuplicatesWithEventIdList(gid, eventIdList);
            if (!eventIdList.isEmpty()) {
                //去重之后不为空，则开始批量获取eventIdList的完整内容
                requestPackager.prepareToRequest(uid, entitiesToLocalize.get(), convId,
                        new GetEventNotificationTaskMng.GetEventNotificationCallback() {
                            @Override
                            public void gotResult(int responseCode, String responseMessage, GetEventNotificationTaskMng.EventEntity entity) {
                                if (ErrorCode.NO_ERROR == responseCode) {
                                    //成功获取到eventNotification的内容之后，开始批量处理事件。
                                    EventProcessor.getInstance().enqueueEventList(convId, entity.getConEventResponse().getEventList(), kind, fullUpdate, new BasicCallback() {
                                        @Override
                                        public void gotResult(int responseCode, String responseMessage) {
                                            Logger.d(TAG, "SyncEventPageContent " + convId + " localize finished . code = " + responseCode + " msg = " + responseMessage);
                                            if (ERRORCODE_GROUP_NOT_EXISTS == responseCode) {
                                                //如果返回的是群组不存在的错误码，则忽略这个entity的处理
                                                operationWatcher.update(CODE_NO_NEED_TO_LOCALIZE);
                                            } else {
                                                operationWatcher.update(responseCode);
                                            }
                                        }
                                    });
                                } else {
                                    operationWatcher.update(responseCode);
                                }
                            }
                        }, eventIdList);
            } else if (fullUpdate && GroupEventsWrapper.EVENT_KIND_3 == kind) {
                //如果eventIdList是空，而且fullUpdate == true,而且kind == 3是群事件，则说明这个群中有事件过期了，这时需要重新获取一遍群成员。
                Logger.d(TAG, "event id list is empty while fullUpdate == true & kind == 3,refresh group member");
                new GetGroupInfoTask(gid, null, true, false).execute();
                operationWatcher.update(CODE_NO_NEED_TO_LOCALIZE);
            } else if (fullUpdate && Kind7EventsWrapper.EVENT_KIND_7 == kind) {
                //如果eventIdList是空，而且fullUpdate == true,而且kind == 7是用户相关列表更新事件，交给UserProfileEventsWrapper处理剩下的逻辑
                Logger.d(TAG, "event id list is empty while fullUpdate == true & kind == 7");
                EventNotificationWrapperBuilder.buildWrapper(kind, 0).onProcess(convId, null, kind, fullUpdate, null);
                operationWatcher.update(CODE_NO_NEED_TO_LOCALIZE);
            } else {
                //去重之后，如果是空的列表，直接通知上层watcher
                operationWatcher.update(CODE_NO_NEED_TO_LOCALIZE);
            }
        }
    }
}
