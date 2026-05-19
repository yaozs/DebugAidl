package com.siasun.ds.aidl;

import static com.siasun.ds.aidl.AidlConstantsKt.DS_AIDL_CONNECT_VERSION;
import static com.siasun.ds.aidl.AidlConstantsKt.DS_KEY_CONTENT;
import static com.siasun.ds.aidl.AidlConstantsKt.DS_KEY_ID;
import static com.siasun.ds.aidl.AidlConstantsKt.DS_KEY_SEND_TYPE;
import static com.siasun.ds.aidl.AidlConstantsKt.DS_KEY_TYPE;
import static com.siasun.ds.aidl.AidlConstantsKt.DS_KEY_VERSION;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class DSMessage implements Parcelable {

    private Bundle data;

    public DSMessage() {
        this.data = new Bundle();
    }

    public DSMessage(Bundle data) {
        this.data = data != null ? data : new Bundle();
    }

    protected DSMessage(Parcel in) {
        this.data = in.readBundle(getClass().getClassLoader());
        if (this.data == null) {
            this.data = new Bundle();
        }
    }

    public Bundle getData() {
        return data;
    }

    public void setData(Bundle data) {
        this.data = data != null ? data : new Bundle();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(data);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DSMessage> CREATOR = new Creator<DSMessage>() {
        @Override
        public DSMessage createFromParcel(Parcel in) {
            return new DSMessage(in);
        }

        @Override
        public DSMessage[] newArray(int size) {
            return new DSMessage[size];
        }
    };

    // ==================== Builder ====================
    public static class Builder {
        private final Bundle bundle;

        public Builder() {
            bundle = new Bundle();
            // 默认加入版本信息
            bundle.putString(DS_KEY_VERSION, DS_AIDL_CONNECT_VERSION);
        }

        public Builder putSendType(SendEnum value) {
            bundle.putInt(DS_KEY_SEND_TYPE, value.getValue());
            return this;
        }

        public Builder putType(String value) {
            bundle.putString(DS_KEY_TYPE, value);
            return this;
        }

        public Builder putId(String value) {
            bundle.putString(DS_KEY_ID, value);
            return this;
        }

        public Builder putContent(String value) {
            bundle.putString(DS_KEY_CONTENT, value);
            return this;
        }

        /**
         * 修改版本号（如果不调用则保持默认）
         */
        public Builder version(String version) {
            bundle.putString(DS_KEY_VERSION, version);
            return this;
        }


        public DSMessage buildClient() {
            bundle.putInt(DS_KEY_SEND_TYPE, SendEnum.CLIENT.getValue());
            return new DSMessage(bundle);
        }

        public DSMessage buildServer() {
            bundle.putInt(DS_KEY_SEND_TYPE, SendEnum.SERVER.getValue());
            return new DSMessage(bundle);
        }
    }
}