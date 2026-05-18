package com.siasun.ds.test

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.siasun.ds.aidl.DSMessage
import com.siasun.ds.aidl.IMessageCallback
import com.siasun.ds.aidl.IMessageService
import com.siasun.ds.aidl.MessageServiceClient

class TestActivity : AppCompatActivity() {

    lateinit var tvContent: TextView
    lateinit var btnSend: Button
    lateinit var btnBind: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_test)

        tvContent = findViewById(R.id.tv_content)
        btnSend = findViewById(R.id.btn_send)
        btnBind = findViewById(R.id.btn_bind)
        btnBind.setOnClickListener {
            MessageServiceClient.getInstance().bind(this)
            MessageServiceClient.getInstance().registerCallback(messageCallback)

        }
        btnSend.setOnClickListener {
            if (MessageServiceClient.getInstance().isConnected()) {

                MessageServiceClient.getInstance().sendMessage(
                    DSMessage
                        .Builder()
                        .putId("11111")
                        .putType("testType")
                        .putContent("testConten")
                        .build()
                )
            } else {
                tvContent.text = "服务绑定不成功"
            }

        }
    }

    private var messageService: IMessageService? = null


    // 定义回调（用于接收服务端主动推送的消息）
    private val messageCallback = object : IMessageCallback.Stub() {
        override fun onMessageFromServer(msg: DSMessage) {
            // ⚠️ 此方法运行在 Binder 线程池中，不可直接操作 UI
            val data = msg.data
            val type = data.getString("type", "")
            val content = data.getString("content", "")


            // 将结果抛到主线程更新 UI
            runOnUiThread {
                tvContent.text = "服务端发送信号：\n ${formatBundle(msg.data)}"
                Toast.makeText(this@TestActivity, "服务端已收到: $content", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()

        MessageServiceClient.getInstance().unregisterCallback(messageCallback)
        MessageServiceClient.getInstance().unbind(this)
    }


    /**
     * 将 Bundle 格式化为可读字符串
     * @param bundle 要格式化的 Bundle
     * @param indent 缩进前缀（内部递归使用，外部调用无需传入）
     * @return 格式化后的字符串
     */
    fun formatBundle(bundle: Bundle?, indent: String = ""): String {
        if (bundle == null) {
            return "${indent}Bundle is null"
        }
        if (bundle.isEmpty) {
            return "${indent}Bundle is empty"
        }
        val sb = StringBuilder()
        for (key in bundle.keySet()) {
            val value = bundle.get(key)
            when (value) {
                null -> sb.append("$indent$key = null\n")
                is Bundle -> {
                    sb.append("$indent$key = Bundle {\n")
                    sb.append(formatBundle(value, "$indent  "))
                    sb.append("$indent}\n")
                }

                is Array<*> -> {
                    val arrayStr = value.joinToString(prefix = "[", separator = ", ", postfix = "]")
                    sb.append("$indent$key = $arrayStr\n")
                }

                else -> sb.append("$indent$key = $value (${value.javaClass.simpleName})\n")
            }
        }
        return sb.toString()
    }
}