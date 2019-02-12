package cn.citytag.base.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.util.concurrent.ExecutionException;

import cn.citytag.base.callback.OnImageLoadFinishListener;
import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.image.transform.GlideCircleTransform;
import cn.citytag.base.image.transform.GlideRoundTransform;

import static android.R.attr.radius;

/**
 * Created by yangfeng01 on 2017/11/17.
 */
public class ImageLoader {

    public static void loadImage(ImageView imageView, @DrawableRes int resId, boolean isGif) {

        RequestOptions options = new RequestOptions();
        if (!isGif) {
            options.placeholder(resId);
        } else {
            options.override(84, 76);
        }

        Glide.with(BaseConfig.getContext())
                .load(resId)
                .apply(options)
                .into(imageView);
    }

    public static void loadImage(ImageView imageView, @DrawableRes int resId) {


        Glide.with(BaseConfig.getContext())
                .load(resId)
                .into(imageView);
    }

    public static void loadImage(ImageView imageView, String url) {
       /* if (ImageUtil.urlIsGif(url)) {
            Glide.with(BaseConfig.getContext())
                    .asGif()
                    .load(url)
                    .into(imageView);
        } else {*/
        Glide.with(BaseConfig.getContext())
                .load(url)
                .into(imageView);
        //}

    }

    public static void loadAdImage(ImageView imageView, int url, RequestOptions options) {
        Glide.with(BaseConfig.getContext())
                .load(url)
                .apply(options)
                .into(imageView);
    }

    public static void loadAddImage(ImageView imageView, String url, RequestOptions options) {

        Glide.with(BaseConfig.getContext())
                .load(url)
                .apply(options)
                .into(imageView);
    }


    public static void loadImage(ImageView imageView, String url, Drawable placeholder) {
        RequestOptions options = new RequestOptions()
                .dontAnimate()
                .centerCrop()
                .placeholder(placeholder)
                .error(placeholder);
        Glide.with(BaseConfig.getContext())
                .load(url)
                .apply(options)
                .into(imageView);
    }


    public static void loadImage(ImageView imageView, String url, Drawable placeholder, Drawable error) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(placeholder)
                .error(error);
        // if (ImageUtil.urlIsGif(url)) {
        //  Glide.with(BaseConfig.getContext())
        //  .asGif()
        // .load(url)
        //  .apply(options)
        // .into(imageView);
        //} else {
        Glide.with(BaseConfig.getContext())
                .load(url)
                .apply(options)
                .into(imageView);
        //  }

    }

    public static void loadImage(Context context, String url, final OnImageLoadFinishListener loadFinishListener) {
        RequestOptions options = new RequestOptions()
                .centerCrop();
        if (context == null) {
            context = BaseConfig.getContext();
        }
//        else if (context instanceof Activity) {
//            Activity activity = (Activity) context;
//            if (activity.isDestroyed() || activity.isFinishing()) {
//                return;
//            }
//        }
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(options)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        loadFinishListener.onFinish(resource);
                        Log.i("sss", "onResourceReady: " + resource);
                    }
                });
    }

    public static void loadCircleImage(Context context, ImageView imageView, String url, Drawable placeholder) {
        if (context == null) {
            context = BaseConfig.getContext();
        }
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .transform(new GlideCircleTransform(context));

        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);

    }

    public static void loadCircleImage(ImageView imageView, String url, Drawable placeholder) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .transform(new GlideCircleTransform(BaseConfig.getContext()));

        Glide.with(BaseConfig.getContext())
                .load(url)
                .apply(options)
                .into(imageView);

    }

    public static void loadCircleImage(Context context, ImageView imageView, String url) {
        if (context == null) {
            context = BaseConfig.getContext();
        }
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .transform(new GlideCircleTransform(context));

        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);

    }

    public static void loadCircleImage(Context context, String url, final OnImageLoadFinishListener loadFinishListener) {
        if (context == null) {
            context = BaseConfig.getContext();
        }
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .transform(new GlideCircleTransform(context));
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(options)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        loadFinishListener.onFinish(resource);
                    }
                });
    }

    public static void loadCircleImage(Context context, ImageView imageView, String url, int placeholderId, int
            errorId) {
        if (context == null) {
            context = BaseConfig.getContext();
        }

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(placeholderId)
                .error(errorId)
                .transform(new GlideCircleTransform(context));

        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);

    }

    public static void loadCircleImage(Context context, ImageView imageView, int resId, int placeholderId, int
            errorId) {
        if (context == null) {
            context = BaseConfig.getContext();
        }
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(placeholderId)
                .error(errorId)
                .transform(new GlideCircleTransform(context));

        Glide.with(context)
                .load(resId)
                .apply(options)
                .into(imageView);
    }

    public static void loadRoundImage(ImageView imageView, String url, Drawable placeholder, int radius) {
        loadRoundImage(imageView, url, placeholder, placeholder, radius);
    }

    public static void loadRoundImage(ImageView imageView, String url, int radius) {
        loadRoundImage(imageView, url, null, null, radius);
    }

    public static void loadRoundImage(ImageView imageView, String url, Drawable placeholder, Drawable error, int
            radius) {

        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .error(error)
                .skipMemoryCache(false)
                .transform(new GlideRoundTransform(imageView.getContext(), radius));

        // if (ImageUtil.urlIsGif(url)) {
        //  Glide.with(imageView.getContext())
        //  .asGif()
        //   .load(url)
        //   .apply(options)
        //    .into(imageView);
        // } else {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(options)
                .into(imageView);
        //   }

    }

    public static void loadRoundImage(ImageView imageView, Drawable drawable, Drawable placeholder, int radius) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .skipMemoryCache(false)
                .transform(new GlideRoundTransform(imageView.getContext(), radius));

        Glide.with(imageView.getContext())
                .load(drawable)
                .apply(options)
                .into(imageView);
    }

    public static void loadCircleImage(Context context, ImageView imageView, String url, Drawable placeholder, final RequestListener listener) {
        if (context == null) {
            context = BaseConfig.getContext();
        }
        final RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .transform(new GlideCircleTransform(context));

        Glide.with(context)
                .load(url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
                                                boolean isFirstResource) {
                        listener.onLoadFailed(e, model, target, isFirstResource);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        listener.onResourceReady(resource, model, target, dataSource, isFirstResource);
                        return false;
                    }
                })
                .apply(options)
                .into(imageView);

    }

    public static void loadRoundImage(ImageView imageView, @DrawableRes int drawableId, Drawable placeholder, int
            radius) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .transform(new GlideRoundTransform(imageView.getContext(), radius));

        Glide.with(imageView.getContext())
                .asBitmap()
                .load(drawableId)
                .apply(options)
                .into(imageView);
    }

    public static void loadRoundImage(ImageView imageView, File file, Drawable placeholder, int radius) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .transform(new GlideRoundTransform(imageView.getContext(), radius));

        Glide.with(imageView.getContext())
                .asBitmap()
                .load(file)
                .apply(options)
                .into(imageView);
    }

    public static void loadCircleImage(Context context, ImageView imageView, File file, int placeholder) {
        if (context == null) {
            context = BaseConfig.getContext();
        }
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .transform(new GlideCircleTransform(context));

        Glide.with(context)
                .asBitmap()
                .load(file)
                .apply(options)
                .into(imageView);
    }

    /**
     * 同步下载
     *
     * @param context
     * @param url
     * @return
     */
    public static String download(Context context, String url) {
        if (context == null) {
            context = BaseConfig.getContext();
        }
        FutureTarget<File> target = Glide.with(context).load(url).downloadOnly(Target.SIZE_ORIGINAL, Target
                .SIZE_ORIGINAL);
        File cacheFile = null;
        String filePath = null;
        try {
            cacheFile = target.get();
            filePath = cacheFile.getAbsolutePath();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public static void loadLongImage(String url, ImageView imageView, Drawable placeholder, boolean isTop) {
        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .skipMemoryCache(false)
                .transform(new GlideRoundTransform(imageView.getContext(), radius));

        Glide.with(imageView.getContext()).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                if (resource != null) {
                    Bitmap bitmap = cutBitmap(resource, isTop); //调用裁剪图片工具类进行裁剪
                    if (bitmap != null)
                        imageView.setImageBitmap(bitmap); //设置Bitmap到图片上
                }

            }
        });

    }

    public static Bitmap cutBitmap(Bitmap bm, boolean isTop) {
        Bitmap bitmap = null;
        if (bm != null) {
            if (isTop) {
                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight() / 2); //对图片的高度的一半进行裁剪
            } else {
                bitmap = Bitmap.createBitmap(bm, 0, bm.getHeight() / 2, bm.getWidth(), bm.getHeight() / 2);
            }
        }
        return bitmap;
    }


}
