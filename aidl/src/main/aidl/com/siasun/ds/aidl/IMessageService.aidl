// IMessageService.aidl
package com.siasun.ds.aidl;

import com.example.messenger.Message;
import com.example.messenger.IMessageCallback;

interface IMessageService {
    /**
     * 客户端向服务端发送消息
     */
    void sendMessageToServer(in Message msg);

    /**
     * 注册回调，用于接收服务端推送
     */
    void registerCallback(IMessageCallback callback);

    /**
     * 注销回调
     */
    void unregisterCallback(IMessageCallback callback);
}