package cn.citytag.base.widget.facelib.model;

/**
 * Created by liguangchun on 2017/12/7.
 */

/**
 * 表情对象模型
 */
public class ChatFaceModel {

    /**
     * 表情资源图片对应的ID
     */
    private int id;

    /**
     * 表情资源对应的文字描述
     */
    private String character;

    /**
     * 表情资源的文件名
     */
    private String faceName;

	private boolean isSelected;

    /**
     * 表情资源图片对应的ID
     */
    public int getId() {
        return id;
    }

    /**
     * 表情资源图片对应的ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 表情资源对应的文字描述
     */
    public String getCharacter() {
        return character;
    }

    /**
     * 表情资源对应的文字描述
     */
    public void setCharacter(String character) {
        this.character = character;
    }

    /**
     * 表情资源的文件名
     */
    public String getFaceName() {
        return faceName;
    }

    /**
     * 表情资源的文件名
     */
    public void setFaceName(String faceName) {
        this.faceName = faceName;
    }

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}
}

