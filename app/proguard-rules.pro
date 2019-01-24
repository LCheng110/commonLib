# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\adt-bundle-windows-x86_64-20140702\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:W
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#Proguard:
#    1.shrinker(压缩):检测并移除没有用到的类，属性，方法，变量
#    2.optimizer(优化):优化代码，非入口节点类会加上private/static/final，没有用到的参数会被删除，一些方法可能会变成内联代码
#    3.obfuscator(混淆):使用短又没有语义的名字重命名非入口类的类名，变量名，方法名。入口类的名字保持不变
#    4.preverifier(预校验):预校验代码是否符合Java1.6或者更高的规范（唯一一个与入口类不相关的步骤）

#Keep配置：
#-keep class xxx                    指定类和类的成员变量是入口节点，保护他们不被移除和混淆。保留xxx该类及其所有子类不混淆，类中成员名不保证
#-keepclassmembers xxx              保护指定的成员变量不被移除、优化、混淆  ep:保护所有序列化的类的成员变量
#-keepclasseswithmembers class xxx  保护拥有指定成员的类，根据类成员确定一些将要被保护的类及其成员

#-keepnames xxx                  保护类名，前提是在shrink这一阶段没有被去掉，也就是说没有被入口节点直接或间接引用的类还是会被删除，仅在obfuscate阶段有效，是-keep,allowshrinking class_pecification的简写
#-keepclassmembernames xxx       保护指定的类成员，前提是在shrink阶段没有被删除
#-keepclasseswithmembernames xxx 保护拥有指定成员的类及其成员，前提是在shrink阶段没有被删除

#-printseeds [filename] 指定通过-keep配置匹配的类或者类成员的详细列表。列表可以打印到标准输出流或者文件里面。这个列表可以看到我们想要保护的类或者成员有没有被真正的保护到，尤其是那些使用通配符匹配的类。

#---------------------------------基本指令区----------------------------------
-optimizationpasses 5   #指定执行几次优化。代码混淆的压缩比例，值在0-7之间
#-dontusemixedcaseclassnames #混淆后类名都为小写
#-skipnonpubliclibraryclasses           #指定忽略类库中的非public类
#-dontskipnonpubliclibraryclasses       #指定不忽略类库中的非public类，版本4.5以上，这个是默认的选项
-dontskipnonpubliclibraryclassmembers   #指定不忽略类库中的非public成员（成员变量和方法）
#-dontpreverify  #不做预校验的操作
#-verbose   #声明在处理过程中输出更多信息。添加这项配置之后，如果处理过程中出现异常，会输出整个StackTrace而不是一条简单的异常说明。
-printmapping proguardMapping.txt   #生成原类名和混淆后的类名的映射文件
-optimizations !code/simplification/cast,!field/*,!class/merging/*  #指定混淆时采用的算法
-keepattributes *Annotation*,InnerClasses   #不混淆Annotation
-keepattributes Signature   #不混淆泛型
-keepattributes SourceFile,LineNumberTable #抛出异常时保留代码行号
#忽略警告
-ignorewarnings

#---------------------------------默认----------------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.vending.licensing.ILicensingService
-keep class android.support.** {*;}

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class **.R$* {
 *;
}
-keepclassmembers class * {
    void *(**On*Event);
}
#webview
-keepclassmembers class fqcn.of.javascript.interface.for.Webview {
   public *;
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, jav.lang.String);
}

#--------------------------------------app--------------------------------------
-keep class cn.citytag.mapgo.component.**{*;}
-keep class cn.citytag.base.utils.SysUtils {* ; }
-keep class cn.citytag.mapgo.BuildConfig {* ; }
-keep class cn.citytag.mapgo.dao.** {*;}
-keep class cn.citytag.mapgo.component.videoplayer.** { *; }

#---------------------------------1.实体类---------------------------------

-keep class cn.citytag.mapgo.model.** { *; }
-keep class jiguang.chat.model.** {*; }
-keep class cn.citytag.mapgo.event.** { *; }
-keep class cn.citytag.base.model.** { *; }
-keep class cn.citytag.live.model.** { *; }
-keep class cn.citytag.live.event.** { *; }
-keep class cn.citytag.live.dao.** {*;}
-keep class cn.citytag.live.constants.** {*;}
-keep class cn.citytag.live.api.** {*;}
#---------------------------------短视频模块model--------------------------
-keep class cn.citytag.video.model.** { *; }
-keep class cn.citytag.video.widgets.dialog.tab.model.** { *; }

#---------------------------------社交模块model----------------------------
-keep class com.example.social.model.** {*;}
-keep class com.example.social.event.** { *; }
-keep class cn.citytag.base.widget.video.** { *; }
#---------------------------------2.第三方包-------------------------------

#腾讯IM
-keep class com.tencent.**{*;}
-dontwarn com.tencent.**

-keep class tencent.**{*;}
-dontwarn tencent.**

-keep class qalsdk.**{*;}
-dontwarn qalsdk.**

#GreenDao
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties
# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**
# If you do not use Rx:
-dontwarn rx.**

#eventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#fastjson
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }
-keepattributes Signature

#gson
-keep class com.google.gson.** {*;}
-keep class com.google.**{*;}
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }

#retrofit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn okio.**
# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

#uCrop
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }
-keepattributes Exceptions,InnerClasses
-keepattributes Signature

# RongCloud SDK  蛋蛋 融云混淆稍后需改  木有时间
-keep class cn.citytag.mapgo.component.**{*;}
-keep class io.rong.** {*;}
-keep class * implements io.rong.imlib.model.MessageContent {*;}
-dontwarn io.rong.push.**
-dontnote com.xiaomi.**
-dontnote com.google.android.gms.gcm.**
-dontnote io.rong.**

# VoIP
-keep class io.agora.rtc.** { *; }

# 高德
-keep class com.amap.api.**{ *; }
-keep class com.amap.api.services.**{ *; }

# IM 广播接收者
-keep class cn.citytag.mapgo.component.im.SealNotificationReceiver {*;}

#2D地图
-keep class com.amap.api.maps2d.**{*;}
-keep class com.amap.api.mapcore2d.**{*;}

#ijkplayer
-keep class tv.danmaku.ijk.** { *; }

#pictureselector
-keep class com.luck.picture.lib.** { *; }
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

#okhttp2
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

 #rxjava
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
 long producerIndex;
 long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

#rxandroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

#极光推送
-dontoptimize
-dontpreverify
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.helpers.JPushMessageReceiver { *; }
-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

-keep class com.huawei.hms.**{*;}
-dontwarn com.xiaomi.push.**
-keep class com.xiaomi.push.** { *; }

#极光IM
-dontoptimize
-dontpreverify
-keepattributes  EnclosingMethod,Signature
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }

-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

 -keepclassmembers class ** {
     public void onEvent*(**);
 }

#========================protobuf================================
-keep class com.google.protobuf.** {*;}

#========================support=================================
-dontwarn cn.jmessage.support.**
-keep class cn.jmessage.support.**{*;}


#阿里云趣拍
-keep class com.qu.preview.** { *; }
-keep class com.qu.mp4saver.** { *; }
-keep class com.duanqu.transcode.** { *; }
-keep class com.duanqu.qupai.render.** { *; }
-keep class com.duanqu.qupai.player.** { *; }
-keep class com.duanqu.qupai.audio.** { *; }
-keep class com.aliyun.qupai.encoder.** { *; }
-keep class com.sensetime.stmobile.** { *; }
-keep class com.duanqu.qupai.yunos.** { *; }
-keep class com.aliyun.common.** { *; }
-keep class com.aliyun.jasonparse.** { *; }
-keep class com.aliyun.struct.** { *; }
-keep class com.aliyun.recorder.AliyunRecorderCreator { *; }
-keep class com.aliyun.recorder.supply.** { *; }
-keep class com.aliyun.querrorcode.** { *; }
-keep class com.qu.preview.callback.** { *; }
-keep class com.aliyun.qupaiokhttp.** { *; }
-keep class com.aliyun.crop.AliyunCropCreator { *; }
-keep class com.aliyun.crop.struct.CropParam { *; }
-keep class com.aliyun.crop.supply.** { *; }
-keep class com.aliyun.qupai.editor.pplayer.AnimPlayerView { *; }
-keep class com.aliyun.qupai.editor.impl.AliyunEditorFactory { *; }
-keep interface com.aliyun.qupai.editor.** { *; }
-keep interface com.aliyun.qupai.import_core.AliyunIImport { *; }
-keep class com.aliyun.qupai.import_core.AliyunImportCreator { *; }
-keep class com.aliyun.qupai.encoder.** { *; }
-keep class com.aliyun.leaktracer.** { *;}
-keep class com.duanqu.qupai.adaptive.** { *; }
-keep class com.aliyun.thumbnail.** { *;}
-keep class com.aliyun.demo.importer.media.MediaCache { *;}
-keep class com.aliyun.demo.importer.media.MediaDir { *;}
-keep class com.aliyun.demo.importer.media.MediaInfo { *;}
-keep class com.alivc.component.encoder.**{ *;}
-keep class com.aliyun.log.core.AliyunLogCommon { *;}
-keep class com.aliyun.log.core.AliyunLogger { *;}
-keep class com.aliyun.log.core.AliyunLogParam { *;}
-keep class com.aliyun.log.core.LogService { *;}
-keep class com.aliyun.log.struct.** { *;}
-keep class com.aliyun.demo.publish.SecurityTokenInfo { *; }
-keep class com.aliyun.vod.common.** { *; }
-keep class com.aliyun.vod.jasonparse.** { *; }
-keep class com.aliyun.vod.qupaiokhttp.** { *; }
-keep class com.aliyun.vod.log.core.AliyunLogCommon { *;}
-keep class com.aliyun.vod.log.core.AliyunLogger { *;}
-keep class com.aliyun.vod.log.core.AliyunLogParam { *;}
-keep class com.aliyun.vod.log.core.LogService { *;}
-keep class com.aliyun.vod.log.struct.** { *;}
-keep class com.aliyun.auth.core.**{*;}
-keep class com.aliyun.auth.common.AliyunVodHttpCommon{*;}
-keep class com.alibaba.sdk.android.vod.upload.exception.**{*;}
-keep class com.alibaba.sdk.android.vod.upload.auth.**{*;}
-keep class com.aliyun.auth.model.**{*;}
-keep class component.alivc.com.facearengine.** {*;}
-keep class **.R$* { *; }

#activeandroid
-keep class com.activeandroid.** { *; }
-dontwarn com.ikoding.app.biz.dataobject.**
-keep public class com.ikoding.app.biz.dataobject.** { *;}
-keepattributes *Annotation*

#---------------------------------3.与js互相调用的类------------------------

#---------------------------------4.反射相关的类和方法-----------------------


#友盟统计
-keepclassmembers class * {
    public <init> (org.json.JSONObject);
}
-keep public class cn.citytag.mapgo.R$*{
    public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#友盟分享
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**
-keep public class javax.**
-keep public class android.webkit.**
-dontwarn android.support.v4.**
-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**

-keep public class com.umeng.socialize.* {*;}
-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*
-keep class com.umeng.weixin.handler.**
-keep class com.umeng.weixin.handler.*
-keep class com.umeng.qq.handler.**
-keep class com.umeng.qq.handler.*
-keep class UMMoreHandler{*;}
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}
-keep class com.tencent.mm.sdk.** {
   *;
}
-keep class com.tencent.mm.opensdk.** {
   *;
}
-keep class com.tencent.wxop.** {
   *;
}
-keep class com.tencent.mm.sdk.** {
   *;
}
-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
-keep class com.kakao.** {*;}
-dontwarn com.kakao.**
-keep public class com.umeng.com.umeng.soexample.R$*{
    public static final int *;
}
-keep public class com.linkedin.android.mobilesdk.R$*{
    public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}
-keep class com.umeng.socialize.impl.ImageImpl {*;}
-keep class com.sina.** {*;}
-dontwarn com.sina.**
-keep class  com.alipay.share.sdk.** {
   *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keep class com.linkedin.** { *; }
-keep class com.android.dingtalk.share.ddsharemodule.** { *; }
-keepattributes Signature

#友盟第三方登录
-dontusemixedcaseclassnames
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**
-keep public class javax.**
-keep public class android.webkit.**
-dontwarn android.support.v4.**
-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**
-keep class com.android.dingtalk.share.ddsharemodule.** { *; }
-keep public class com.umeng.socialize.* {*;}

-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*
-keep class com.umeng.weixin.handler.**
-keep class com.umeng.weixin.handler.*
-keep class com.umeng.qq.handler.**
-keep class com.umeng.qq.handler.*
-keep class UMMoreHandler{*;}
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements   com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}
-keep class com.tencent.mm.sdk.** {
 *;
}
-keep class com.tencent.mm.opensdk.** {
*;
}
-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
-keep public class com.umeng.com.umeng.soexample.R$*{
public static final int *;
}
-keep public class com.linkedin.android.mobilesdk.R$*{
public static final int *;
    }
-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}

-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}

-keep class com.sina.** {*;}
-dontwarn com.sina.**
-keep class  com.alipay.share.sdk.** {
   *;
}
-keepnames class * implements android.os.Parcelable {
public static final ** CREATOR;
}

-keep class com.linkedin.** { *; }
-keepattributes Signature

# banner 的混淆代码
-keep class com.youth.banner.** {
    *;
 }

#3D 地图 V5.0.0之前：
-keep   class com.amap.api.maps.**{*;}
-keep   class com.autonavi.amap.mapcore.*{*;}
-keep   class com.amap.api.trace.**{*;}

#3D 地图 V5.0.0之后：
-keep   class com.amap.api.maps.**{*;}
-keep   class com.autonavi.**{*;}
-keep   class com.amap.api.trace.**{*;}

#定位
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}

#搜索
-keep   class com.amap.api.services.**{*;}

#2D地图
-keep class com.amap.api.maps2d.**{*;}
-keep class com.amap.api.mapcore2d.**{*;}

#导航
-keep class com.amap.api.navi.**{*;}
-keep class com.autonavi.**{*;}

#MigrationHelper
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static void dropTable(org.greenrobot.greendao.database.Database, boolean);
    public static void createTable(org.greenrobot.greendao.database.Database, boolean);
}

# alipay支付宝支付
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
-keep class com.alipay.sdk.app.H5PayCallback {
    <fields>;
    <methods>;
}
-keep class com.alipay.android.phone.mrpc.core.** { *; }
-keep class com.alipay.apmobilesecuritysdk.** { *; }
-keep class com.alipay.mobile.framework.service.annotation.** { *; }
-keep class com.alipay.mobilesecuritysdk.face.** { *; }
-keep class com.alipay.tscenter.biz.rpc.** { *; }
-keep class org.json.alipay.** { *; }
-keep class com.alipay.tscenter.** { *; }
-keep class com.ta.utdid2.** { *;}
-keep class com.ut.device.** { *;}



#神策埋点
-dontwarn com.sensorsdata.analytics.android.**
-keep class com.sensorsdata.analytics.android.** {
*;
}
-keep class **.R$* {
    <fields>;
}
-keep public class * extends android.content.ContentProvider
-keepnames class * extends android.view.View

-keep class * extends android.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}
-keep class android.support.v4.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}
-keep class * extends android.support.v4.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}

# 如果使用了 DataBinding
#-dontwarn android.databinding.**
#-keep class android.databinding.** { *; }
#-keep class 您项目的包名.databinding.** {
#    <fields>;
#    <methods>;
#}

#腾讯bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}



#西瓜信用
# 所有native的方法不混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# 继承自View的构造方法不混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
    public *** get*();
}


# AIDL 文件不能去混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# 保护 谷歌第三方 jar 包，界面特效
-keep class android.support.v4.**
-dontwarn android.support.v4.**
-keepattributes *Annotation*
-keepattributes *JavascriptInterface*

-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

# 保持任意包名.R类的类成员属性。即保护R文件中的属性名不变
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 保护所有实体中的字段名称
-keepclassmembers class * implements java.io.Serializable {
    <fields>;
    <methods>;
}

# 实体类 混淆keep规则
-keep class com.google.common.base.**{*;}

# EcaSDK混淆规则开始
# EcaConfig
-keepclassmembers class com.ald.razor.config.EcaConfig{
  public *;
}
-keepclassmembers class com.ald.razor.config.EcaInitListener{
  void *();
}
-keepclassmembers class com.ald.razor.activity.EcaBaseHtml5WebView {
  public *;
}
-keepattributes *Annotation*
-keepattributes *JavascriptInterface*


-keep class com.ald.razor.ui.EcaWebView$AlaWebViewData{
    public *;
}
-keep class com.ald.razor.ui.EcaWebView{
    public *;
}
-keep class com.ald.razor.network.entity.ApiResponse{*;}
-keep class com.ald.razor.network.entity.ApiResponseEca{*;}
# EcaSDK混淆规则结束

# mx
-keepclasseswithmembers class * {
    ... *JNI*(...);
}
-keepclasseswithmembernames class * {
	... *JRI*(...);
}
-keep class **JNI* {*;}

# keep curllib
-keep class com.moxie.mxcurllib.** { *; }

# keep annotated by NotProguard
-keep @com.proguard.annotation.NotProguard class * {*;}
-keep class * {
    @com.proguard.annotation <fields>;
    @android.webkit.JavascriptInterface <fields>;
}
-keepclassmembers class * {
    @com.proguard.annotation <fields>;
    @android.webkit.JavascriptInterface <fields>;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# 保护注解
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation {*;}
# 避免混淆泛型
-keepattributes Signature
# 避免混淆反射
-keepattributes EnclosingMethod
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
# 不混淆内部类
-keepattributes InnerClasses

# 不混淆枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 防⽌⼴告 SDK 请求参数被混淆
-keep class com.alddin.adsdk.model.** { *; }

#---------------------------------阿里云短视频SDK 混淆-------------------------------
-ignorewarnings
#crashreporter
-keep class com.alibaba.motu.crashreporter.MotuCrashReporter{*;}
-keep class com.alibaba.motu.crashreporter.ReporterConfigure{*;}
-keep class com.alibaba.motu.crashreporter.IUTCrashCaughtListener{*;}
-keep class com.ut.mini.crashhandler.IUTCrashCaughtListener{*;}
-keep class com.alibaba.motu.crashreporter.utrestapi.UTRestReq{*;}
-keep class com.alibaba.motu.crashreporter.handler.nativeCrashHandler.NativeCrashHandler{*;}
-keep class com.alibaba.motu.crashreporter.handler.nativeCrashHandler.NativeExceptionHandler{*;}
-keep interface com.alibaba.motu.crashreporter.handler.nativeCrashHandler.NativeExceptionHandler{*;}
#crashreporter3.0以后 一定要加这个
-keep class com.uc.crashsdk.JNIBridge{*;}

-ignorewarnings
-dontwarn okio.**
-dontwarn com.google.common.cache.**
-dontwarn java.nio.file.**
-dontwarn sun.misc.**
-keep class android.support.v4.** { *; }
-keep class android.support.v7.** { *; }
-keep class okhttp3.** { *; }
-keep class com.bumptech.glide.integration.okhttp3.** { *; }
-keep class com.liulishuo.filedownloader.** { *; }
-keep class java.nio.file.** { *; }
-keep class sun.misc.** { *; }

-keep class com.qu.preview.** { *; }
-keep class com.qu.mp4saver.** { *; }
-keep class com.duanqu.transcode.** { *; }
-keep class com.duanqu.qupai.render.** { *; }
-keep class com.duanqu.qupai.player.** { *; }
-keep class com.duanqu.qupai.audio.** { *; }
-keep class com.aliyun.qupai.encoder.** { *; }
-keep class com.sensetime.stmobile.** { *; }
-keep class com.duanqu.qupai.yunos.** { *; }
-keep class com.aliyun.common.** { *; }
-keep class com.aliyun.jasonparse.** { *; }
-keep class com.aliyun.struct.** { *; }
-keep class com.aliyun.recorder.AliyunRecorderCreator { *; }
-keep class com.aliyun.recorder.supply.** { *; }
-keep class com.aliyun.querrorcode.** { *; }
-keep class com.qu.preview.callback.** { *; }
-keep class com.aliyun.qupaiokhttp.** { *; }
-keep class com.aliyun.crop.AliyunCropCreator { *; }
-keep class com.aliyun.crop.struct.CropParam { *; }
-keep class com.aliyun.crop.supply.** { *; }
-keep class com.aliyun.qupai.editor.pplayer.AnimPlayerView { *; }
-keep class com.aliyun.qupai.editor.impl.AliyunEditorFactory { *; }
-keep interface com.aliyun.qupai.editor.** { *; }
-keep interface com.aliyun.qupai.import_core.AliyunIImport { *; }
-keep class com.aliyun.qupai.import_core.AliyunImportCreator { *; }
-keep class com.aliyun.qupai.encoder.** { *; }
-keep class com.aliyun.leaktracer.** { *;}
-keep class com.duanqu.qupai.adaptive.** { *; }
-keep class com.aliyun.thumbnail.** { *;}
-keep class com.aliyun.demo.importer.media.MediaCache { *;}
-keep class com.aliyun.demo.importer.media.MediaDir { *;}
-keep class com.aliyun.demo.importer.media.MediaInfo { *;}
-keep class com.alivc.component.encoder.**{ *;}
-keep class com.aliyun.log.core.AliyunLogCommon { *;}
-keep class com.aliyun.log.core.AliyunLogger { *;}
-keep class com.aliyun.log.core.AliyunLogParam { *;}
-keep class com.aliyun.log.core.LogService { *;}
-keep class com.aliyun.log.struct.** { *;}
-keep class com.aliyun.demo.publish.SecurityTokenInfo { *; }

-keep class com.aliyun.vod.common.** { *; }
-keep class com.aliyun.vod.jasonparse.** { *; }
-keep class com.aliyun.vod.qupaiokhttp.** { *; }
-keep class com.aliyun.vod.log.core.AliyunLogCommon { *;}
-keep class com.aliyun.vod.log.core.AliyunLogger { *;}
-keep class com.aliyun.vod.log.core.AliyunLogParam { *;}
-keep class com.aliyun.vod.log.core.LogService { *;}
-keep class com.aliyun.vod.log.struct.** { *;}
-keep class com.aliyun.auth.core.**{*;}
-keep class com.aliyun.auth.common.AliyunVodHttpCommon{*;}
-keep class com.alibaba.sdk.android.vod.upload.exception.**{*;}
-keep class com.alibaba.sdk.android.vod.upload.auth.**{*;}
-keep class com.aliyun.auth.model.**{*;}
-keep class component.alivc.com.facearengine.** {*;}
-keep class com.aliyun.editor.NativeEditor{*;}
-keep class com.aliyun.nativerender.BitmapGenerator{*;}
-keep class com.aliyun.editor.EditorCallBack{*;}
-keep enum com.aliyun.editor.TimeEffectType{*;}
-keep enum com.aliyun.editor.EffectType{*;}
-keep class **.R$* { *; }
-keep class com.aliyun.svideo.sdk.internal.common.project.**{*;}
-keep class com.aliyun.svideo.sdk.external.struct.**{*;}
-keep class com.aliyun.sys.AlivcSdkCore{*;}
## Event Bus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#glide配置
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

############################### glide配置  ###################
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
###################################### Event Bus  ###########################

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

##---------------End: proguard configuration for Gson  ----------

-keepclasseswithmembers class com.aliyun.demo.editor.EditorActivity{ *; }

-keep class com.alivc.player.** { *; }