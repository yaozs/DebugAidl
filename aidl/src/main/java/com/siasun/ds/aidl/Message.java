package com.siasun.ds.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {
    private String info;      // 消息信息（如类型）
    private String content;   // 消息内容
    private String extJson;   // 扩展 JSON

    public Message() {
    }

    public Message(String info, String content, String extJson) {
        this.info = info;
        this.content = content;
        this.extJson = extJson;
    }

    protected Message(Parcel in) {
        info = in.readString();
        content = in.readString();
        extJson = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExtJson() {
        return extJson;
    }

    public void setExtJson(String extJson) {
        this.extJson = extJson;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(info);
        dest.writeString(content);
        dest.writeString(extJson);
    }
}
