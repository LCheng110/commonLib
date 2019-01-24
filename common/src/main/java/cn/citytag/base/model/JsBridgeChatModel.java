package cn.citytag.base.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhaoyuanchao on 2019/1/22.
 */
public class JsBridgeChatModel implements Serializable {
    private long userId;
    private String phone;
    private String nickName;
    private List<Message> messages;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public class Message implements Serializable{
        private int messageType;
        private String textContent;
        private String imgLink;
        private String voiceLink;
        private double voiceDuration;
        private double latitude;
        private double longitude;
        private double scale;
        private String name;
        private String address;

        public int getMessageType() {
            return messageType;
        }

        public void setMessageType(int messageType) {
            this.messageType = messageType;
        }

        public String getTextContent() {
            return textContent;
        }

        public void setTextContent(String textContent) {
            this.textContent = textContent;
        }

        public String getImgLink() {
            return imgLink;
        }

        public void setImgLink(String imgLink) {
            this.imgLink = imgLink;
        }

        public String getVoiceLink() {
            return voiceLink;
        }

        public void setVoiceLink(String voiceLink) {
            this.voiceLink = voiceLink;
        }

        public double getVoiceDuration() {
            return voiceDuration;
        }

        public void setVoiceDuration(double voiceDuration) {
            this.voiceDuration = voiceDuration;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getScale() {
            return scale;
        }

        public void setScale(double scale) {
            this.scale = scale;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}
