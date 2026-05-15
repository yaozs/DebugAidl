// IMessageCallback.aidl
package com.siasun.ds.aidl;

import com.example.messenger.Message;

interface IMessageCallback {
    /**
     * 服务端向客户端推送消息时回调
     */
    void onMessageFromServer(in Message msg);
}