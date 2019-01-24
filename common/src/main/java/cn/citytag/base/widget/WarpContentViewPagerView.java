package cn.citytag.base.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cn.citytag.base.R;
import cn.citytag.base.constants.ExtraName;
import cn.citytag.base.event.RefreshMomentDetailsPicSlideEvent;
import cn.citytag.base.image.ImageLoader;
import cn.citytag.base.model.DynamicPictureModel;
import cn.citytag.base.utils.L;
import cn.citytag.base.utils.ScreenUtil;
import cn.citytag.base.utils.UIUtils;
import cn.citytag.base.view.PreviewActivity;
import cn.citytag.base.vm.ListVM;

import static cn.citytag.base.constants.ExtraName.EXTRA_COMMON_VAR;
import static cn.citytag.base.constants.ExtraName.EXTRA_PREVIEW_IMAGES;
import static cn.citytag.base.constants.ExtraName.EXTRA_PREVIEW_SHOW_DELETE;
import static cn.citytag.base.constants.ExtraName.EXTRA_PREVIEW_SHOW_DOWNLOAD;
import static cn.citytag.base.constants.ExtraName.EXTRA_SELECT_POSITION;

/**
 * Created by liguangchun on 2018/3/15.
 */

public class WarpContentViewPagerView extends ViewPager {

    private String TAG = "WarpContentViewPagerView";

    //context
    private Context mContext;

    private Activity activity;

    private boolean isFirstCreate;

    //轮播
    private ViewPager mViewPager;

    //url 集合
    private static List<String> urls = new ArrayList<>();

    ArrayList<DynamicPictureModel> arrayList;

    //高度 array
    private int[] heightArray;

    //default first pic height
    private int defaultHeight;

    // bind item vm
    private ListVM mListVm;

    //all size
    private int size;

    //viewpager 横向滑动
    private ISlide mISlide;

    private boolean isLeftSlide = false;

    //点击图片是否跳转
    private boolean isCanLink = true;

    //current position
    int currentPosition = 0;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public ISlide getISlide() {
        return mISlide;
    }

    public void setISlide(ISlide iSlide) {
        this.mISlide = iSlide;
    }

    private RefreshMomentDetailsPicSlideEvent event = new RefreshMomentDetailsPicSlideEvent();

    public WarpContentViewPagerView(Context context) {
        super(context, null);
    }

    public WarpContentViewPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    /**
     * 布局加载器加载view
     *
     * @param context
     */
    private void initView(Context context) {
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.view_warp_content_pager, null);
        mViewPager = findViewById(R.id.view_pager);

    }

    //初始化pagerView 第一张图展示
    private void initParameter() {
        if (arrayList != null && arrayList.size() > 0) {
            if (arrayList.get(0).getHeight() == 0.0) {
                SimpleTarget<Bitmap> simpleTarget = new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        float scale = (float) resource.getHeight() / resource.getWidth();
                        setDefaultFirst(scale);
                    }
                };

                Glide.with(getContext()).asBitmap().load(urls.get(0))
                        .listener(new RequestListener<Bitmap>() {

                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        }).
                        into(simpleTarget);
            } else {
                float scale = (float) arrayList.get(0).getHeight() / (float) arrayList.get(0).getWidth();
                setDefaultFirst(scale);

            }
        }
    }

    //默认 first pic height
    private void setDefaultFirst(float scale) {
        defaultHeight = (int) (scale * ScreenUtil.getWidthScreen(mContext));
        mISlide.afterSetDate(defaultHeight, 1);
        if (defaultHeight >= UIUtils.dip2px(500)) {
            defaultHeight = UIUtils.dip2px(500);
        } else if (defaultHeight < UIUtils.dip2px(280)) {
            defaultHeight = UIUtils.dip2px(280);
        }
        ViewGroup.LayoutParams layoutParams = mViewPager.getLayoutParams();
        layoutParams.height = defaultHeight;
        mViewPager.setLayoutParams(layoutParams);
        initPager();
    }

    //init Viewpager
    private void initPager() {

        MPagerAdapter pagerAdapter = new MPagerAdapter();
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                L.d(TAG, position + "");
                //暂时用不到左右滑动，应该会用到的
               /* if (position > currentPosition) {
                    //右滑
                    isLeftSlide = false;
                    currentPosition = position;
                } else if (position < currentPosition) {
                    //左滑
                    isLeftSlide = true;
                    currentPosition = position;
                }*/

                //positionOffset 控制控件高度
                if (position == urls.size() - 1) return;
                int viewPagerHeight = heightArray[position] == 0 ? defaultHeight :
                        (int) (heightArray[position] * (1 - positionOffset) +
                                heightArray[position + 1] * positionOffset);
                ViewGroup.LayoutParams layoutParams = mViewPager.getLayoutParams();
                layoutParams.height = viewPagerHeight;
                mViewPager.setLayoutParams(layoutParams);
                if (mISlide != null) {
                    // 处理viewpager滑动时顶部分享布局的隐藏效果
                    mISlide.viewPagerHorizontalSlide(positionOffset, isLeftSlide);
                }
            }

            @Override
            public void onPageSelected(int position) {
                int pagerHeight = heightArray[position] == 0 ? defaultHeight : heightArray[position];
                mISlide.afterSetDate(pagerHeight, position + 1);
                //refresh 动态详情的图片集合下面角标
                event.setPosition(position + 1);
                EventBus.getDefault().post(event);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * set数据 refresh Adapter
     * <p>
     * mViewPager.setOffscreenPageLimit(size - 1);
     *
     * @param arrayList
     */
    public void setData(List<DynamicPictureModel> arrayList) {
        this.arrayList = (ArrayList<DynamicPictureModel>) arrayList;
        if (arrayList == null) return;
        int size = arrayList.size();
        urls.clear();
        for (int i = 0; i < size; i++) {
            urls.add(arrayList.get(i).getPictureUrl());
        }

        if (mViewPager != null) {
            initParameter();
        }

    }

    //适配器
    class MPagerAdapter extends android.support.v4.view.PagerAdapter {
        @Override
        public int getCount() {
            if (heightArray == null || heightArray.length != urls.size()) {
                heightArray = new int[urls.size()];
            }
            return urls.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            final ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            if (arrayList.get(position).getHeight() == 0.0) {
                SimpleTarget<Bitmap> simpleTarget = new SimpleTarget<Bitmap>() {


                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        //拿到resource 渲染画面
                        float scale = (float) resource.getHeight() / (float) resource.getWidth();
                        int heightPic = (int) (scale * ScreenUtil.getWidthScreen(mContext));
                        if (heightPic > ScreenUtil.getHeightScreen(mContext)) {
                            heightPic = ScreenUtil.getHeightScreen(mContext);
                        }
                        heightArray[position] = heightPic;
                        imageView.setImageBitmap(resource);
                        container.addView(imageView);
                    }
                };
                Glide.with(mContext)
                        .asBitmap()
                        .load(urls.get(position))
                        .into(simpleTarget);
            } else {
                float scale = (float) arrayList.get(position).getHeight() / (float) arrayList.get(position).getWidth();
                int heightPic = (int) (scale * ScreenUtil.getWidthScreen(mContext));
                if (heightPic > UIUtils.dip2px(500)) {
                    heightPic = UIUtils.dip2px(500);
                } else if (heightPic <= UIUtils.dip2px(270)) {
                    heightPic = UIUtils.dip2px(270);
                }
//                if (heightPic > ScreenUtil.getHeightScreen(mContext)) {
//                    heightPic = ScreenUtil.getHeightScreen(mContext);
//                }
                heightArray[position] = heightPic;
                ImageLoader.loadImage(imageView, arrayList.get(position).getPictureUrl());
                container.addView(imageView);
            }

            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isCanLink) {
                        Intent intent = new Intent(activity, PreviewActivity.class);
                        intent.putExtra(ExtraName.INDEX, position);
                        intent.putStringArrayListExtra(EXTRA_PREVIEW_IMAGES, (ArrayList<String>) urls);
                        intent.putExtra(EXTRA_SELECT_POSITION, position);
                        intent.putExtra(EXTRA_PREVIEW_SHOW_DELETE, false);
                        intent.putExtra(EXTRA_PREVIEW_SHOW_DOWNLOAD, true);
                        intent.putExtra(EXTRA_COMMON_VAR, EXTRA_COMMON_VAR);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                }
            });
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof ImageView) {
                container.removeView((ImageView) object);
            }
        }
    }

    public boolean isCanLink() {
        return isCanLink;
    }

    public void setCanLink(boolean canLink) {
        isCanLink = canLink;
    }

    //横向滑动接口
    public interface ISlide {
        void viewPagerHorizontalSlide(float positionOffset, boolean isLeftSlide);

        void afterSetDate(int picHeight, int position);
    }


}
