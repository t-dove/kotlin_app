package com.example.anonymous_chat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.anonymous_chat.databinding.ActivityChatBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cyberhousenotifications.AnonChatAPI
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class ChatActivity : AppCompatActivity(), MessageDeletedListener  {
    private lateinit var messageReceiver: BroadcastReceiver
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatId: String;
    private lateinit var auth_token: String;
    private var lastTypingTime = 0L
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)

//        val messages = listOf(
//            Message(1,"Привет!", "18:30", true),
//            Message(2,"Как дела?", "18:31", false),
//            Message(3,"Все хорошо, спасибо!", "18:32", true)
//        )
        val messages = mutableListOf<Message>()

        val adapter = MessagesAdapter(messages, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        chatId = intent.getStringExtra("chat_id").toString()
        auth_token = intent.getStringExtra("auth_token").toString()
        binding.searchAge1.text = intent.getStringExtra("user_name")
        val intValue = intent.getIntExtra("another_key", 0)

        messageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val type = intent?.getStringExtra("type")
                if (type == "new_message") {
                    val msg_id = intent.getStringExtra("msg_id")!!.toInt()
                    val msg_text = intent.getStringExtra("text").toString()
                    val currentTime = LocalTime.now()
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    messages.add(Message(msg_id,msg_text, currentTime.format(formatter), false))
                    binding.recyclerView.adapter = adapter
                }else if(type == "chat_closed"){
                    showChatEndedDialog()
                }else if(type == "typing"){
                    binding.textView12.alpha = 1f
                    val typingHandler = Handler(Looper.getMainLooper())
                    typingHandler.postDelayed({
                        binding.textView12.alpha = 0f
                    }, 3000)

                }else if(type == "del_message"){
                    adapter.deleteMessageById(intent.getStringExtra("msg_id")!!.toInt(), false)
                    binding.recyclerView.adapter = adapter
                }
            }
        }
        val filter = IntentFilter("com.example.anonymous_chat")
        registerReceiver(messageReceiver, filter, RECEIVER_EXPORTED)

        binding.imageView10.setOnClickListener {
            val data = mapOf(
                "auth_token" to auth_token,
                "chat_id" to chatId,
                "text" to binding.messageText.text
            )
            CoroutineScope(Dispatchers.Main).launch {
                val response = withContext(Dispatchers.IO) {
                    val AnonChat = AnonChatAPI()
                    AnonChat.sendPostRequest("send_message", data)
                }
                val gson = Gson()
                val responseObject = gson.fromJson(response, ApiResponse::class.java)
                if (responseObject.response.success) {
                    val currentTime = LocalTime.now()
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    messages.add(Message(responseObject.response.msg_id,binding.messageText.text.toString(), currentTime.format(formatter), true))
                    binding.recyclerView.adapter = adapter
                    binding.messageText.setText("")
                }
            }
        }
        binding.messageText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTypingTime >= 3000) {
                    val data = mapOf(
                        "auth_token" to auth_token,
                        "chat_id" to chatId
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        val response = withContext(Dispatchers.IO) {
                            val AnonChat = AnonChatAPI()
                            AnonChat.sendPostRequest("set_typing", data)
                        }
                        val gson = Gson()
                        val responseObject = gson.fromJson(response, ApiResponse::class.java)
                        if (responseObject.response.success) {
                            lastTypingTime = currentTime
                        }
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.imageView9.setOnClickListener {
            showCloseChatDialog()
        }
    }
    private fun showCloseChatDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Закончить чат")
        builder.setMessage("Вы действительно хотите закочнить чат?")
        builder.setPositiveButton("Да") { dialog, which ->
            closeChat()
        }
        builder.setNegativeButton("Нет") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun closeChat() {
        val data = mapOf(
            "auth_token" to auth_token,
            "chat_id" to chatId
        )
        CoroutineScope(Dispatchers.Main).launch {
            val response = withContext(Dispatchers.IO) {
                val AnonChat = AnonChatAPI()
                AnonChat.sendPostRequest("close_chat", data)
            }
            val gson = Gson()
            val responseObject = gson.fromJson(response, ApiResponse::class.java)
            if (responseObject.response.success) {
                val intent = Intent(this@ChatActivity, ProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun showChatEndedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Чат завершен")
        builder.setMessage("Собеседник завершил чат")
        builder.setPositiveButton("ОК") { dialog, which ->
            dialog.dismiss()
            val intentCloseChat = Intent(this@ChatActivity, ProfileActivity::class.java)
            startActivity(intentCloseChat)
            finish()
        }
        builder.setOnDismissListener {
            val intentCloseChat = Intent(this@ChatActivity, ProfileActivity::class.java)
            startActivity(intentCloseChat)
            finish()
        }
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(messageReceiver)
    }

    override fun onMessageDeleted(messageId: Int) {
        val data = mapOf(
            "auth_token" to auth_token,
            "chat_id" to chatId,
            "msg_id" to messageId.toString()
        )
        CoroutineScope(Dispatchers.Main).launch {
            val response = withContext(Dispatchers.IO) {
                val AnonChat = AnonChatAPI()
                AnonChat.sendPostRequest("del_message", data)
            }
            val gson = Gson()
            val responseObject = gson.fromJson(response, ApiResponse::class.java)
        }
    }

}