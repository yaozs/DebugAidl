// IMessageService.aidl
package com.siasun.ds.aidl;

import com.siasun.ds.aidl.Message;
import com.siasun.ds.aidl.IMessageCallback;

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