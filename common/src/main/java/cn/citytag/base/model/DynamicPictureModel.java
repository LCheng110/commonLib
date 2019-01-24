package cn.citytag.base.model;

/**
 * Created by yangfeng01 on 2017/12/28.
 *
 * 动态详情里的图片数组
 * 我的动态
 */

public class DynamicPictureModel {

	private long pictureId;			// 图片id
	private String pictureUrl;		// 图片url
	private double width;			// 图片宽度
	private double height;			// 	图片高度
	private long uormId;			//

	public long getPictureId() {
		return pictureId;
	}

	public void setPictureId(long pictureId) {
		this.pictureId = pictureId;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public long getUormId() {
		return uormId;
	}

	public void setUormId(long uormId) {
		this.uormId = uormId;
	}
}
