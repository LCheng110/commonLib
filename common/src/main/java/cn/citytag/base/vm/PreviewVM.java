package cn.citytag.base.vm;

import android.app.Activity;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.michaelflisar.rxbus2.rx.RxDisposableManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.citytag.base.adapter.PreviewAdapter;
import cn.citytag.base.dao.MediaInfo;
import cn.citytag.base.databinding.ActivityPreviewBinding;
import cn.citytag.base.event.ImageEvent;
import cn.citytag.base.event.ModifyMyPhotoEvent;
import cn.citytag.base.event.OpinionEvent;
import cn.citytag.base.event.PublishEvent;
import cn.citytag.base.image.ImageLoader;
import cn.citytag.base.utils.FileUtils;
import cn.citytag.base.utils.UIUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static cn.citytag.base.constants.ExtraName.EXTRA_PREVIEW_IMAGES;
import static cn.citytag.base.constants.ExtraName.EXTRA_PREVIEW_SHOW_DELETE;
import static cn.citytag.base.constants.ExtraName.EXTRA_PREVIEW_SHOW_DOWNLOAD;
import static cn.citytag.base.constants.ExtraName.EXTRA_PREVIEW_THUMB_IMAGES;
import static cn.citytag.base.constants.ExtraName.EXTRA_SELECT_POSITION;


/**
 * Created by yangfeng01 on 2017/12/18.
 */
public class PreviewVM extends BaseVM {

    public final ObservableBoolean isShowDelete = new ObservableBoolean(true);    // 是否显示删除
    public final ObservableBoolean isShowDownload = new ObservableBoolean(false);    // 是否显示下载
    public final ObservableField<String> sizeField = new ObservableField<>();    // 1/5

    private Activity activity;
    private ActivityPreviewBinding cvb;

    private int index;

    private PreviewAdapter adapter;
    private List<String> images = new ArrayList<>();
    private List<String> imagesThumb = new ArrayList<>(); //缩略图图片集合

    private int curPosition;
    private String path;

    private boolean isShowAlpha;

    public PreviewVM(Activity activity, ActivityPreviewBinding cvb) {
        this.activity = activity;
        this.cvb = cvb;
        init();
    }

    public PreviewVM(Activity activity, ActivityPreviewBinding cvb, int index, boolean isShowAlpha) {
        this.activity = activity;
        this.cvb = cvb;
        this.index = index;
        this.isShowAlpha = isShowAlpha;
        init();
    }


    private void init() {
        for (String s : images = activity.getIntent().getStringArrayListExtra(EXTRA_PREVIEW_IMAGES)) {
            
        }

        imagesThumb = activity.getIntent().getStringArrayListExtra(EXTRA_PREVIEW_THUMB_IMAGES);
                curPosition = activity.getIntent().getIntExtra(EXTRA_SELECT_POSITION, 0);
        sizeField.set((curPosition + 1) + "/" + images.size());
        boolean showDelete = activity.getIntent().getBooleanExtra(EXTRA_PREVIEW_SHOW_DELETE, true);
        boolean showDownload = activity.getIntent().getBooleanExtra(EXTRA_PREVIEW_SHOW_DOWNLOAD, false);
        isShowDelete.set(showDelete);
        if (showDelete) {
            cvb.circleIndicator.setVisibility(View.VISIBLE);
            cvb.rlBottom.setVisibility(View.GONE);
        }
        if (showDownload) {
            isShowDownload.set(showDownload);
            cvb.circleIndicator.setVisibility(View.GONE);
            cvb.rlTitle.setVisibility(View.GONE);
            cvb.rlBottom.setVisibility(View.VISIBLE);
        }
        adapter = new PreviewAdapter(images,imagesThumb, activity, isShowAlpha);
        cvb.viewPager.setAdapter(adapter);
        cvb.viewPager.setCurrentItem(curPosition, false);
        cvb.circleIndicator.setViewPager(cvb.viewPager, curPosition);
        cvb.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                curPosition = position;
                sizeField.set((curPosition + 1) + "/" + images.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //        cvb.viewPager.setCurrentItem(index);
    }


    /**
     * 点击右上角删除
     *
     * @param view
     */
    public void deleteMedia(View view) {
        path = refeshData();
        PublishEvent publishEvent = new PublishEvent();
        publishEvent.setOperation(PublishEvent.DELETE);
        MediaInfo info = new MediaInfo();
        info.setCompressPath(path);
        publishEvent.setMediaInfo(info);
        EventBus.getDefault().post(publishEvent);

        ModifyMyPhotoEvent event = new ModifyMyPhotoEvent();
        event.setOperation(ModifyMyPhotoEvent.DELETE);
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.setCompressPath(path);
        event.setMediaInfo(mediaInfo);
        EventBus.getDefault().post(event);

        OpinionEvent opinionEvent = new OpinionEvent();
        opinionEvent.setOperation(OpinionEvent.DELETE);
        MediaInfo mediaInfo1 = new MediaInfo();
        mediaInfo1.setCompressPath(path);
        opinionEvent.setMediaInfo(mediaInfo1);
        EventBus.getDefault().post(opinionEvent);

        ImageEvent imageEvent = new ImageEvent();
        imageEvent.setType(ImageEvent.TYPE_DELETE);
        imageEvent.setObject(path);
        EventBus.getDefault().post(imageEvent);
        if (images.size() == 0) {
            activity.finish();
        }
    }

    /**
     * 下载并保存图片到本地
     *
     * @param view
     */
    public void clickDownload(View view) {
        String imageUrl = images.get(curPosition);
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) {
                String cacheFilePath = ImageLoader.download(activity, imageUrl);
                e.onNext(cacheFilePath);
                e.onComplete();

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String cacheFilePath) {
                        try {

                            String suffix = ".jpg";
                            if (imageUrl != null && imageUrl.length() > 0) {
                                String[] suffixS = imageUrl.split("\\.");
                                if (suffixS.length > 0) {
                                    suffix = "." + suffixS[suffixS.length - 1];
                                }
                            }

                            String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + System.currentTimeMillis() + suffix;
                            FileUtils.copyFile(cacheFilePath, imagePath);
                            Intent intentBroadcast = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            File file = new File(imagePath);
                            intentBroadcast.setData(Uri.fromFile(file));
                            activity.sendBroadcast(intentBroadcast);
                            UIUtils.toastMessage("图片保存成功");
                        } catch (Exception e) {
                            e.printStackTrace();
                            UIUtils.toastMessage("图片保存失败");
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        UIUtils.toastMessage("图片保存失败");
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    private String refeshData() {
        String imagetPath = images.get(curPosition);
        images.remove(imagetPath);
        adapter.notifyDataSetChanged();
        return imagetPath;
    }

    @Override
    public void detach() {
        RxDisposableManager.unsubscribe(this);
    }
}
