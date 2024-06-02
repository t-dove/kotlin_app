package com.example.anonymous_chat

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log;
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
class FirebaseMessagingService:  FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Обработка входящего сообщения от FCM
        if (remoteMessage.data.isNotEmpty()) {
            val type: String = remoteMessage.data["type"]!!
            Log.i("test","looool")
            if(type == "chat_found") {
                val chat_id: String = remoteMessage.data["chat_id"]!!
                val user_name: String = remoteMessage.data["user_name"]!!
                val user_sex: String = remoteMessage.data["user_sex"]!!
                val intent = Intent("com.example.anonymous_chat")
                intent.putExtra("type", type)
                intent.putExtra("chat_id", chat_id)
                intent.putExtra("user_name", user_name)
                intent.putExtra("user_sex", user_sex)
                sendBroadcast(intent)
            }else if(type == "new_message") {
                val msg_id: String = remoteMessage.data["msg_id"]!!
                val text: String = remoteMessage.data["text"]!!
                val intent = Intent("com.example.anonymous_chat")
                intent.putExtra("type", type)
                intent.putExtra("msg_id", msg_id)
                intent.putExtra("text", text)
                sendBroadcast(intent)
                if (isAppInBackground()) {
                    showNotification("Новое сообщение", text)
                }
            }else if(type == "chat_closed"){
                val intent = Intent("com.example.anonymous_chat")
                intent.putExtra("type", type)
                sendBroadcast(intent)
            }else if(type == "typing"){
                val intent = Intent("com.example.anonymous_chat")
                intent.putExtra("type", type)
                sendBroadcast(intent)
            }else if(type == "del_message"){
                val msg_id: String = remoteMessage.data["msg_id"]!!
                val intent = Intent("com.example.anonymous_chat")
                intent.putExtra("type", type)
                intent.putExtra("msg_id", msg_id)
                sendBroadcast(intent)
            }
        }
        }

    private fun isAppInBackground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses
        val myProcess = runningAppProcesses.firstOrNull { it.processName == packageName }
        return myProcess?.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    private fun showNotification(title: String?, body: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "default_channel_id"

        val channel = NotificationChannel(
            channelId,
            "Default Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.message_inter_style)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(0, notificationBuilder.build())
    }
    override fun onNewToken(token: String) {
        // new token
        saveFcmToken(applicationContext,token)
    }
    private fun saveFcmToken(context: Context, token: String?) {
        val sharedPref = context.getSharedPreferences("ANON_CHAT_USERDATA_AUTH", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString("fcm_token", token)
            apply()
        }
    }
}