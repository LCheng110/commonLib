package jiguang.chat.utils.imagepicker;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

public class GlideImageLoader implements ImageLoader {

    @Override
    public void displayImages(Activity activity, String path, ImageView imageView, int width, int height) {

        Glide.with(activity)                             //配置上下文
                .load(Uri.fromFile(new File(path)))      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                .into(imageView);
    }



    @Override
    public void clearMemoryCache() {
    }
}
