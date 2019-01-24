package cn.citytag.base.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

import cn.citytag.base.R;
import cn.citytag.base.image.ImageLoader;
import cn.citytag.base.widget.ProgressHUD;
import cn.citytag.base.widget.photoview.OnPhotoTapListener;
import cn.citytag.base.widget.photoview.PhotoView;

/**
 * Created by yangfeng01 on 2017/12/18.
 */

public class PreviewAdapter extends PagerAdapter {

    private List<String> images;
    private List<String> imagesThumb; //缩略图集合
    private Activity activity;

    private boolean isAlphaFinish;

    public PreviewAdapter(List<String> images, Activity activity, boolean isAlphaFinish) {
        this.images = images;
        this.activity = activity;
        this.isAlphaFinish = isAlphaFinish;
    }

    public PreviewAdapter(List<String> images, List<String> imagesThumb, Activity activity, boolean isAlphaFinish) {
        this.images = images;
        this.imagesThumb = imagesThumb;
        this.activity = activity;
        this.isAlphaFinish = isAlphaFinish;
    }

    public PreviewAdapter(List<String> images, Activity activity) {
        this.images = images;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return images == null ? 0 : images.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.layout_preview_image, container, false);
        final PhotoView photoView = view.findViewById(R.id.photo_view);

        RequestOptions option = new RequestOptions()
                .error(activity.getResources().getDrawable(R.drawable.bg_shape_d8d8d8_place_holder))
                .skipMemoryCache(false);
        if (imagesThumb != null && position < imagesThumb.size()) {
            ProgressHUD.show(activity, "", true, null);
            ImageLoader.loadAddImage(photoView, imagesThumb.get(position), option);
            SimpleTarget<Drawable> simpleTarget = new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    //拿到bitmap，隐藏加载中的图片
                    if (images.get(position).endsWith("gif")) {
                        ImageLoader.loadAddImage(photoView, images.get(position), option);
                    } else {
                        //photoView.setImageDrawable(resource);
                        Glide.with(activity).load(images.get(position))
                                .into(photoView);
                    }

                    ProgressHUD.dismissHUD();
                }
            };

            Glide.with(activity).load(images.get(position))
                    .into(simpleTarget);
        } else {
            ImageLoader.loadAddImage(photoView, images.get(position), option);
        }

        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {

            }
        });
        container.addView(view, 0);
        setPhotoListener(photoView);
        return view;
    }

    /**
     * 设置监听事件
     *
     * @param photoView
     */
    private void setPhotoListener(PhotoView photoView) {
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.finish();
                if (isAlphaFinish) {
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
