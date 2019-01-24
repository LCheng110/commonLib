package cn.citytag.base.dao;

import android.os.Parcel;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.Serializable;

/**
 * Created by yangfeng01 on 2017/12/14.
 *
 * 冒泡media实体类，包括图片，视频
 */
@Entity
public class MediaInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id(autoincrement = true)
	private Long id;
	private String tag;
	private String imageID; //用于网络图片的删除
	private int type = 0;    // 1:图片 2:视频,3加好
	private String mimeTye;
	@NotNull
	private String filePath;    // 文件路径
	private String compressPath;// 压缩文件路径
	private String urlPath;		//网络图片url；
	private int position;    // 排列位置
	private long duration;    // 音视频的长度
	private int width;    // 宽度
	private int height;    // 高度
	private boolean isAvater;
	@Generated(hash = 407402819)
	public MediaInfo(Long id, String tag, String imageID, int type, String mimeTye,
                     @NotNull String filePath, String compressPath, String urlPath, int position,
                     long duration, int width, int height, boolean isAvater) {
		this.id = id;
		this.tag = tag;
		this.imageID = imageID;
		this.type = type;
		this.mimeTye = mimeTye;
		this.filePath = filePath;
		this.compressPath = compressPath;
		this.urlPath = urlPath;
		this.position = position;
		this.duration = duration;
		this.width = width;
		this.height = height;
		this.isAvater = isAvater;
	}

	@Generated(hash = 899343088)
	public MediaInfo() {
	}

	protected MediaInfo(Parcel in) {
		imageID = in.readString();
		type = in.readInt();
		mimeTye = in.readString();
		filePath = in.readString();
		compressPath = in.readString();
		urlPath = in.readString();
		position = in.readInt();
		duration = in.readLong();
		width = in.readInt();
		height = in.readInt();
	}


	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getType() {
		return this.type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getMimeTye() {
		return this.mimeTye;
	}

	public void setMimeTye(String mimeTye) {
		this.mimeTye = mimeTye;
	}

	public String getFilePath() {
		return this.filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getCompressPath() {
		return this.compressPath;
	}

	public void setCompressPath(String compressPath) {
		this.compressPath = compressPath;
	}

	public String getUrlPath() {
		return urlPath == null ? "" : urlPath;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}

	public int getPosition() {
		return this.position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public long getDuration() {
		return this.duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getImageID() {
		return imageID;
	}

	public void setImageID(String imageID) {
		this.imageID = imageID;
	}

	public String getTag() {
		return this.tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean isAvater() {
		return isAvater;
	}

	public void setAvater(boolean avater) {
		isAvater = avater;
	}

	public boolean getIsAvater() {
		return this.isAvater;
	}

	public void setIsAvater(boolean isAvater) {
		this.isAvater = isAvater;
	}
}


