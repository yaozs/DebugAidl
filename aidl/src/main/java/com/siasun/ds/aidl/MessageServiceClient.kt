package com.siasun.ds.aidl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * AIDL 双向通信工具类（单例）
 * 使用示例：
 *   // 绑定服务（建议在 Application 或首个 Activity 中调用）
 *   MessageServiceClient.getInstance().bind(applicationContext)
 *
 *   // 发送消息到服务端
 *   val msg = DSMessage.Builder().put("action", "test").build()
 *   MessageServiceClient.getInstance().sendMessage(msg) { success ->
 *       if (success) Log.d("TAG", "发送成功")
 *   }
 *
 *   // 注册回调（接收服务端推送）
 *   val callback = object : IMessageCallback.Stub() {
 *       override fun onMessageFromServer(msg: DSMessage) {
 *           // 处理服务端发来的消息（运行在 Binder 线程池，如需更新 UI 请切换线程）
 *           val data = msg.data
 *           Log.d("TAG", "收到服务端消息: ${data.getString("content")}")
 *       }
 *   }
 *   MessageServiceClient.getInstance().registerCallback(callback)
 *
 *   // 解绑（在最后一个使用服务的组件 onDestroy 中调用）
 *   MessageServiceClient.getInstance().unbind(applicationContext)
 */
class MessageServiceClient private constructor() {
    companion object {
        private const val TAG = "MessageServiceClient"

        const val SERVICE_CLASS_NAME = "com.siasun.ds.debugging.service.MessageService"
        const val SERVICE_PACKAGE_NAME = "com.siasun.ds.debugging"
        @Volatile
        private var instance: MessageServiceClient? = null

        fun getInstance(): MessageServiceClient {
            return instance ?: synchronized(this) {
                instance ?: MessageServiceClient().also { instance = it }
            }
        }
    }

    // 在 MessageServiceClient 中添加
    interface OnServiceConnectedListener {
        fun onConnected()
        fun onDisconnected()
    }

    private var connectionListener: OnServiceConnectedListener? = null

    fun setOnServiceConnectedListener(listener: OnServiceConnectedListener?) {
        connectionListener = listener
    }

    private var messageService: IMessageService? = null
    private var bound = false
//    private var appContext: Context? = null
    private val bindCount = AtomicInteger(0)          // 绑定引用计数
    private val callbacks = CopyOnWriteArrayList<IMessageCallback>()  // 本地缓存回调

    // 服务连接监听
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            messageService = IMessageService.Stub.asInterface(service)
            connectionListener?.onConnected()
            bound = true
            Log.d(TAG, "服务已连接，当前绑定计数: ${bindCount.get()}")
            // 重新注册所有缓存的回调
            callbacks.forEach { callback ->
                try {
                    messageService?.registerCallback(callback)
                    Log.d(TAG, "成功重注册回调: ${callback.hashCode()}")
                } catch (e: RemoteException) {
                    Log.e(TAG, "重注册回调失败", e)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // 在 onServiceDisconnected 中调用
            connectionListener?.onDisconnected()
            messageService = null
            bound = false
            Log.w(TAG, "服务意外断开，等待重连")
            // 注意：系统会尝试自动重连，重连后会再次回调 onServiceConnected
        }
    }

    /**
     * 绑定服务（支持多组件同时绑定，内部引用计数）
     * @param context 建议传入 ApplicationContext
     * @return true 表示绑定请求已发出，false 表示参数错误
     */
    fun bind(context: Context): Boolean {
        if (context == null) return false
        synchronized(this) {
//            val ctx = context.applicationContext
//            this.appContext = ctx
            val newCount = bindCount.incrementAndGet()
            Log.d(TAG, "bind 调用，当前计数: $newCount")
            if (!bound) {
                val intent = Intent().apply {
                    // 使用 ComponentName 明确指定服务，避免依赖具体类
                    component = ComponentName(SERVICE_PACKAGE_NAME, SERVICE_CLASS_NAME)
                }
                val ret = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                if (!ret) {
                    Log.e(TAG, "bindService 失败")
                    bindCount.decrementAndGet()
                    return false
                }
                bound = true // 标记请求已发出，实际连接回调中会再次设置
            }
            return true
        }
    }

    /**
     * 解绑服务（与 bind 配对调用，内部引用计数减到 0 时真正解绑）
     * @param context 必须与 bind 时传入的 ApplicationContext 相同（或同进程任意 Context）
     */
    fun unbind(context: Context) {
        synchronized(this) {
            if (bindCount.get() <= 0) {
                Log.w(TAG, "unbind 调用时计数已为 0，忽略")
                return
            }
            val newCount = bindCount.decrementAndGet()
            Log.d(TAG, "unbind 调用，当前计数: $newCount")
            if (newCount == 0) {
                if (bound) {
                    context.applicationContext.unbindService(connection)
                    bound = false
                    messageService = null
                    // 注意：不清除 callbacks，以便下次绑定时自动重注册
                    Log.d(TAG, "服务已彻底解绑，回调缓存仍保留 ${callbacks.size} 个")
                } else {
                    Log.d(TAG, "服务未处于绑定状态，无需解绑")
                }
//                appContext = null
            }
        }
    }

    /**
     * 检查当前是否已连接服务
     */
    fun isConnected(): Boolean = bound && messageService != null

    /**
     * 发送消息到服务端（异步，无回调）
     * @return true 表示发送请求已发出（不保证对方收到，无异常即成功）
     */
    fun sendMessage(msg: DSMessage): Boolean {
        if (!isConnected()) {
            Log.w(TAG, "服务未连接，无法发送消息")
            return false
        }
        return try {
            messageService?.sendMessageToServer(msg)
            true
        } catch (e: RemoteException) {
            Log.e(TAG, "发送消息时发生 RemoteException", e)
            false
        }
    }

    /**
     * 发送消息到服务端，并可选的成功/失败回调（运行在调用线程）
     * @param msg 消息对象
     * @param callback 回调参数 success: Boolean
     */
    fun sendMessage(msg: DSMessage, callback: ((Boolean) -> Unit)? = null) {
        val success = sendMessage(msg)
        callback?.invoke(success)
    }

    /**
     * 注册回调（用于接收服务端推送的消息）
     * 注意：回调接口运行在 Binder 线程池，如需更新 UI 需自行切换到主线程
     * @param callback IMessageCallback.Stub 的实现对象
     */
    fun registerCallback(callback: IMessageCallback) {
        if (callback == null) return
        callbacks.add(callback)
        if (isConnected()) {
            try {
                messageService?.registerCallback(callback)
                Log.d(TAG, "立即注册回调成功: ${callback.hashCode()}")
            } catch (e: RemoteException) {
                Log.e(TAG, "注册回调时发生 RemoteException", e)
            }
        } else {
            Log.d(TAG, "服务未连接，回调已缓存: ${callback.hashCode()}")
        }
    }

    /**
     * 注销回调
     */
    fun unregisterCallback(callback: IMessageCallback) {
        callbacks.remove(callback)
        if (isConnected()) {
            try {
                messageService?.unregisterCallback(callback)
                Log.d(TAG, "注销回调成功: ${callback.hashCode()}")
            } catch (e: RemoteException) {
                Log.e(TAG, "注销回调时发生 RemoteException", e)
            }
        } else {
            Log.d(TAG, "服务未连接，已从缓存中移除回调: ${callback.hashCode()}")
        }
    }

    /**
     * 获取原始 AIDL 接口（用于特殊操作，注意可能为 null）
     */
    fun getRawService(): IMessageService? = messageService

    /**
     * 手动释放所有缓存回调（一般不需要调用，除非确定不再使用服务）
     */
    fun clearCallbacks() {
        callbacks.clear()
    }
}