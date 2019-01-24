package cn.citytag.base.bindingadapter.recyclerview;

import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.citytag.base.R;
import cn.citytag.base.command.ReplyCommand;
import cn.citytag.base.image.ImageLoader;
import cn.citytag.base.utils.ImageUtil;
import cn.citytag.base.utils.L;
import cn.citytag.base.utils.UIUtils;
import cn.citytag.base.widget.SquareImageView;
import cn.citytag.base.widget.SupCircleImageView;
import cn.citytag.base.widget.UcAvatarView;
import cn.citytag.base.widget.facelib.model.ChatFaceModel;
import cn.citytag.base.widget.facelib.util.FaceConversionUtil;
import cn.citytag.base.widget.ptr.DefaultFooter;
import cn.citytag.base.widget.ptr.DefaultHeader;
import cn.citytag.base.widget.ptr.SpringView;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by yangfeng01 on 2017/11/27.
 */

public class ViewBindingAdapter {

	@BindingAdapter({"onRefreshCommand"})
	public static void onRefreshCommand(SwipeRefreshLayout swipeRefreshLayout, final ReplyCommand onRefreshCommand) {
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (onRefreshCommand != null) {
					try {
						onRefreshCommand.execute();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * RecyclerView上拉加载更多
	 *
	 * @param recyclerView
	 * @param onLoadMoreCommand
	 */
	// TODO: 2017/11/27 上拉加载的过程中防止多次加载
	@BindingAdapter("onLoadMoreCommand")
	public static void onLoadMoreCommand(RecyclerView recyclerView, ReplyCommand<Integer> onLoadMoreCommand) {
		RecyclerView.OnScrollListener onScrollListener = new LoadMoreScrollListener(onLoadMoreCommand);
		recyclerView.addOnScrollListener(onScrollListener);
	}

	public static class LoadMoreScrollListener extends RecyclerView.OnScrollListener {

		private ReplyCommand<Integer> onLoadMoreCommand;
		private PublishSubject<Integer> methodInvoke = PublishSubject.create();

		public LoadMoreScrollListener(final ReplyCommand<Integer> onLoadMoreCommand) {
			this.onLoadMoreCommand = onLoadMoreCommand;
			methodInvoke.throttleFirst(1, TimeUnit.SECONDS)
					.subscribe(new Consumer<Integer>() {
						@Override
						public void accept(@NonNull Integer itemCount) throws Exception {
							int page = itemCount / 20 + 1;
							onLoadMoreCommand.execute(page);
						}
					});
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);
			L.d("Scroll", "onScrolled dx == " + dx + " , dy == " + dy);
		}

		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			super.onScrollStateChanged(recyclerView, newState);
			L.i("Scroll", "onScrollStateChanged newState == " + newState);
			if (newState == RecyclerView.SCROLL_STATE_IDLE) {
				RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
				if (layoutManager instanceof LinearLayoutManager) {
					LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
					int totalItemCount = linearLayoutManager.getItemCount();    // 全部item数量
					int visibleItemCount = linearLayoutManager.getChildCount();    // 当前屏幕可见的item数量
					int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
					if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
						if (onLoadMoreCommand != null) {
							methodInvoke.onNext(recyclerView.getAdapter().getItemCount());
						}
					}
					//int visibleItemCount = layoutManager.getChildCount();
					//int totalItemCount = layoutManager.getItemCount();
					//int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
					//if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
					//	if (onLoadMoreCommand != null) {
					//		methodInvoke.onNext(recyclerView.getAdapter().getItemCount());
					//	}
					//}
				} else if (layoutManager instanceof StaggeredGridLayoutManager) {
					StaggeredGridLayoutManager staggeredGridLayoutManager =
							(StaggeredGridLayoutManager) layoutManager;
					int[] last = new int[staggeredGridLayoutManager.getSpanCount()];
					staggeredGridLayoutManager.findLastVisibleItemPositions(last);
					//if (!ViewCompat.canScrollVertically(recyclerView, 1)) {
					//	if (onLoadMoreCommand != null) {
					//		methodInvoke.onNext(recyclerView.getAdapter().getItemCount());
					//	}
					//}
					for (int aLast : last) {
						//Log.i(TAG, aLast + "    " + recyclerView.getAdapter().getItemCount());
						if (aLast >= recyclerView.getAdapter().getItemCount() - 1) {
							if (onLoadMoreCommand != null) {
								methodInvoke.onNext(recyclerView.getAdapter().getItemCount());
							}
						}
					}
				}

			}
		}

	}

	@BindingAdapter(value = {"viewHeight"}, requireAll = false)
	public static void setHeight(View view, int viewHeight) {
		final int heightPx = UIUtils.dip2px(view.getContext(), viewHeight);
		ViewGroup.LayoutParams lp = view.getLayoutParams();
		lp.height = heightPx;
		view.setLayoutParams(lp);
	}

	/**
	 * 设置ImageView的url地址加载和占位图
	 *
	 * @param imageView
	 * @param url
	 * @param placeholder
	 */
	@BindingAdapter(value = {"url", "placeholder", "error"}, requireAll = false)
	public static void setImage(ImageView imageView, String url, Drawable placeholder, Drawable error) {
		ImageLoader.loadImage(imageView, url, placeholder, error);
	}

	/**
	 * 设置ImageView的resId图
	 *
	 * @param imageView
	 *
	 */
	@BindingAdapter(value = {"resId","resIsGif"}, requireAll = false)
	public static void setImage(ImageView imageView, @DrawableRes int resId ,boolean resIsGif) {
		ImageLoader.loadImage(imageView, resId ,resIsGif);
	}

	/**
	 * 设置圆角ImageView
	 *
	 * @param imageView
	 * @param url
	 * @param placeholder
	 */
	@BindingAdapter(value = {"circleImageUrl", "placeholder"}, requireAll = false)
	public static void setCircleImage(ImageView imageView, String url, Drawable placeholder) {
		if (placeholder == null) {
			placeholder = imageView.getContext().getResources().getDrawable(R.drawable.shape_color_placeholder_circle);
		}
		//解决加载闪烁问题
		ImageLoader.loadCircleImage(imageView.getContext(), imageView, url, placeholder);
	}

	@BindingAdapter(value = {"roundImageDrawable", "placeholder", "radius"}, requireAll = false)
	public static void setRoundImage(ImageView imageView, Drawable drawable, Drawable placeholder, int radius) {
		if (placeholder == null) {
			placeholder = imageView.getResources().getDrawable(R.drawable.shape_color_placeholder_r4);
		}
		ImageLoader.loadRoundImage(imageView, drawable, placeholder, radius);
	}

	@BindingAdapter(value = {"roundImageFile", "placeholder", "radius"}, requireAll = false)
	public static void setRoundImage(ImageView imageView, File file, Drawable placeholder, int radius) {
		if (placeholder == null) {
			placeholder = imageView.getResources().getDrawable(R.drawable.shape_color_placeholder_r4);
		}
		ImageLoader.loadRoundImage(imageView, file, placeholder, radius);
	}

	@BindingAdapter(value = {"roundImageId", "placeholder", "radius"}, requireAll = false)
	public static void setRoundImage(ImageView imageView, int drawableId, Drawable placeholder, int radius) {
		if (placeholder == null) {
			placeholder = imageView.getResources().getDrawable(R.drawable.shape_color_placeholder_r4);
		}
		ImageLoader.loadRoundImage(imageView, drawableId, placeholder, radius);
	}

	@BindingAdapter(value = {"isColorFilter", "filterColor"}, requireAll = false)
	public static void setColorFilter(ImageView imageView, boolean isColorFilter, @ColorInt int filterColor) {
		if (isColorFilter) {
			if (filterColor == 0) {
				filterColor = Color.parseColor("#20000000");
			}
			imageView.setColorFilter(filterColor, PorterDuff.Mode.SRC_ATOP);
		}
	}


	/**
	 * View是否可见
	 *
	 * @param view
	 * @param isVisible
	 */
	@BindingAdapter("visibility")
	public static void setVisibility(View view, boolean isVisible) {
		view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}

	@BindingAdapter("src")
	public static void setSrc(View view, int resId) {
		((ImageView) view).setImageResource(resId);
	}

	@BindingAdapter("background")
	public static void setBackground(View view, int resId) {
		view.setBackgroundResource(resId);
	}

	@BindingAdapter("isSelected")
	public static void setViewSelected(View view, boolean isSelected) {
		view.setSelected(isSelected);
	}

	@BindingAdapter("isEnabled")
	public static void setViewEnabled(View view, boolean isEnabled) {
		view.setEnabled(isEnabled);
	}

	@BindingAdapter(value = {"fontSize", "fontColor"}, requireAll = false)
	public static void setFontStyle(TextView textView, int fontSize, int fontColor) {
		if (fontSize != 0) {
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
		}
		if (fontColor != 0) {
			textView.setTextColor(fontColor);
		}
	}

	@BindingAdapter(value = {"textChangedCommand", "editList"}, requireAll = false)
	public static void setOnTextWatcher(EditText editText, final ReplyCommand<String> textChangedCommand, List<EditText> editList) {
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (textChangedCommand != null) {
					try {
						textChangedCommand.execute(s.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		if (editList != null) {
			editList.add(editText);
		}
	}

	@BindingAdapter(value = {"scrollCommand"}, requireAll = false)
	public static void addOnScrollListener(RecyclerView recyclerView, final ReplyCommand<Integer> replyCommand) {
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				try {
					replyCommand.execute(dy);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@BindingAdapter(value = {"roundImageUrl", "placeholder", "radius"}, requireAll = false)
	public static void setRoundImage(SquareImageView imageView, String url, Drawable placeholder, int radius) {
		if(placeholder == null) {
			placeholder = imageView.getContext().getResources().getDrawable(R.drawable.shape_color_placeholder_r4);
		}
		if (radius == 0) {
			radius = 4;
		}
		ImageLoader.loadRoundImage(imageView, url, placeholder, radius);
	}

	@BindingAdapter({"title"})
	public static void setToolbar(Toolbar toolbar, String title) {
		if (toolbar != null) {
			toolbar.setTitle(title);
		}
	}


	@BindingAdapter(value = {"appDrawableLeft", "appDrawableTop", "appDrawableRight", "appDrawableBottom"}, requireAll = false)
	public static void setCompoundDrawablesWithIntrinsicBounds(TextView textView, int left, int top, int right, int bottom) {
		textView.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
	}



	/**
	 * TextView是否选中状态
	 *
	 * @param textView
	 * @param isSelected 是否选中状态
	 */
	@BindingAdapter({"isSelected"})
	public static void setIsSelected(View textView, boolean isSelected) {
		textView.setSelected(isSelected);
	}

	@BindingAdapter(value = {"isShow", "picSize", "supPicSize","isGF"}, requireAll = false)
	public static void setIsShow(SupCircleImageView supCircleImageView, boolean isShow, int picSize, int supPicsize, boolean isGF) {
		supCircleImageView.setShow(isShow);
		supCircleImageView.setPicSize(picSize);
		supCircleImageView.setSupPicSize(supPicsize);
		supCircleImageView.setGF(isGF);


	}

	@BindingAdapter(value = {"onRefresh", "onLoadMore"}, requireAll = false)
	public static void setPtrListener(SpringView springView, final cn.citytag.base.widget.ptr.listener.OnRefreshListener onRefreshListener, final cn.citytag.base.widget.ptr.listener.OnLoadMoreListener onLoadMoreListener) {
		springView.setType(SpringView.Type.FOLLOW);
		springView.setHeader(new DefaultHeader(springView.getContext()));
		springView.setFooter(new DefaultFooter(springView.getContext()));
		springView.setListener(new SpringView.OnFreshListener() {
			@Override
			public void onRefresh() {
				if (onRefreshListener != null) {
					onRefreshListener.onRefresh();
				}
			}

			@Override
			public void onLoadmore() {
				if (onLoadMoreListener != null) {
					onLoadMoreListener.onLoadMore();
				}
			}
		});
	}

	@BindingAdapter(value = {"circleAvatarUrl", "conner", "isShowConner"}, requireAll = false)
	public static void setCircleAvatar(UcAvatarView avatar, String avatarUrl, int conner, boolean isShowConner) {
		avatar.setCircleAvatarUrl(avatarUrl);
		if (conner != 0) {
			Drawable connerDrawable = avatar.getContext().getResources().getDrawable(conner);
			avatar.setCornerBackground(connerDrawable);
		}
		if (!isShowConner) {
			avatar.setIsShowConner(true);
		}
	}

	@BindingAdapter(value = {"circleAvatarUrl", "conner", "isShowConner", "width", "height"}, requireAll = false)
	public static void setCircleAvatar(UcAvatarView avatar, String avatarUrl, int conner, boolean isShowConner
			, int width, int height
	) {
		if (height != 0 && width != 0) {
			avatarUrl = ImageUtil.getSpecificUrl(avatarUrl, width, height);
		}

		avatar.setCircleAvatarUrl(avatarUrl);
		if (conner != 0) {
			Drawable connerDrawable = avatar.getContext().getResources().getDrawable(conner);
			avatar.setCornerBackground(connerDrawable);
		}
		if (!isShowConner) {
			avatar.setIsShowConner(true);
		}
	}

	@BindingAdapter(value = {"roundImageUrl", "placeholder", "radius",
			"width", "height"}, requireAll = false)
	public static void setRoundImage(ImageView imageView, String url, Drawable placeholder, int radius
			, int width, int height) {
		if (height != 0 && width != 0) {
			url = ImageUtil.getSpecificUrl(url, width, height);
		}
		if (radius == 0) {
			radius = 4;
		}
		if(placeholder == null) {
			placeholder = imageView.getContext().getResources().getDrawable(R.drawable.shape_color_placeholder_r4);
		}
		ImageLoader.loadRoundImage(imageView, url, placeholder, radius);
	}

	@BindingAdapter(value = {"circleAvatarUrlAddTag", "conner", "isShowConner"}, requireAll = false)
	public static void setCircleAvatarAddTag(UcAvatarView avatar, String avatarUrl, int conner, boolean isShowConner) {
		avatar.setCircleAvatarUrlAddTag(avatarUrl);
		if (conner != 0) {
			Drawable connerDrawable = avatar.getContext().getResources().getDrawable(conner);
			avatar.setCornerBackground(connerDrawable);
		}
		if (!isShowConner) {
			avatar.setIsShowConner(true);
		}
	}


	@BindingAdapter(value = {"drawableLeft"}, requireAll = false)
	public static void setDrawableLeft(TextView textView, int left) {
		textView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
	}


}
