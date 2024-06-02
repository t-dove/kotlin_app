package com.example.anonymous_chat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.anonymous_chat.databinding.ActivityProfileBinding
import com.example.cyberhousenotifications.AnonChatAPI
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest

class ProfileActivity : ComponentActivity() {
    private lateinit var messageReceiver: BroadcastReceiver
    private val userAuthViewModel: UserAuthViewModel by viewModels()
    private lateinit var binding: ActivityProfileBinding
    private var selfAge:String? = null
    private var selfSex: Int? = null
    private var searchAge:MutableList<String> = mutableListOf()
    private var searchSex:Int? = null
    private var auth_token: String = ""
    private var isSearching: Boolean = false;
    private var searchId: Int = 0;
    private val REQUEST_NOTIFICATION_PERMISSION = 1

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userAuthViewModel.getUserAuth().observe(this, Observer { userAuth ->
            userAuth?.let {
                auth_token = it.access_token
            }
        })


        requestNotificationPermission()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        binding.mIc.setOnClickListener(){
            binding.mIc.alpha = 0.55f
            binding.femIc.alpha = 1f
            selfSex = 2
        }
        binding.femIc.setOnClickListener(){
            binding.femIc.alpha = 0.55f
            binding.mIc.alpha = 1f
            selfSex = 1
        }
        binding.searchF.setOnClickListener(){
            binding.searchF.alpha = 0.55f
            binding.searchM.alpha = 1f
            binding.searchFm.alpha = 1f
            searchSex = 1
        }
        binding.searchM.setOnClickListener(){
            binding.searchM.alpha = 0.55f
            binding.searchF.alpha = 1f
            binding.searchFm.alpha = 1f
            searchSex = 2
        }
        binding.searchFm.setOnClickListener(){
            binding.searchFm.alpha = 0.55f
            binding.searchM.alpha = 1f
            binding.searchF.alpha = 1f
            searchSex = 3
        }
        binding.age1.setOnClickListener(){
            setAgeOpacity()
            binding.age1.alpha = 0.55f
            selfAge = "16-17"
        }
        binding.age2.setOnClickListener(){
            setAgeOpacity()
            binding.age2.alpha = 0.55f
            selfAge = "18-19"
        }
        binding.age3.setOnClickListener(){
            setAgeOpacity()
            binding.age3.alpha = 0.55f
            selfAge = "20-24"
        }
        binding.age4.setOnClickListener(){
            setAgeOpacity()
            binding.age4.alpha = 0.55f
            selfAge = "25-99"
        }

        binding.searchAge1.setOnClickListener{
            if(binding.searchAge1.alpha == 0.55f) binding.searchAge1.alpha = 1f else binding.searchAge1.alpha = 0.55f
            searchAgeToggle("16-17")
        }
        binding.searchAge2.setOnClickListener{
            if(binding.searchAge2.alpha == 0.55f) binding.searchAge2.alpha = 1f else binding.searchAge2.alpha = 0.55f
           searchAgeToggle("18-19")
        }
        binding.searchAge3.setOnClickListener{
            if(binding.searchAge3.alpha == 0.55f) binding.searchAge3.alpha = 1f else binding.searchAge3.alpha = 0.55f
            searchAgeToggle("20-24")
        }
        binding.searchAge4.setOnClickListener{
            if(binding.searchAge4.alpha == 0.55f) binding.searchAge4.alpha = 1f else binding.searchAge4.alpha = 0.55f
            searchAgeToggle("25-99")
        }

        binding.exitBtn.setOnClickListener {
            if(isSearching){
                val data = mapOf(
                    "auth_token" to auth_token,
                    "search_id" to searchId.toString(),
                )
                CoroutineScope(Dispatchers.Main).launch {
                    val response = withContext(Dispatchers.IO) {
                        val AnonChat = AnonChatAPI()
                        AnonChat.sendPostRequest("cancel_search", data)
                    }
                    val gson = Gson()
                    val responseObject = gson.fromJson(response, ApiResponse::class.java)
                    if (responseObject.response.success) {
                        isSearching = false
                        binding.searchBtn.text = "Поиск"
                        binding.exitBtn.text = "Выйти"
                    }
                }
            }else{
                userAuthViewModel.deleteAllUserAuths()
                val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                startActivity(intent)
                this.finish()
            }
        }
        binding.searchBtn.setOnClickListener {
            if (!isSearching) {
                if (searchAge.isNotEmpty() && searchSex != null && selfAge != null && selfSex != null && !binding.nameText.text.isNullOrEmpty()) {
                    val data = mapOf(
                        "auth_token" to auth_token,
                        "name" to binding.nameText.text,
                        "self_age" to selfAge.toString(),
                        "self_sex" to selfSex.toString(),
                        "search_age" to searchAge.joinToString(","),
                        "search_sex" to searchSex.toString()
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        val response = withContext(Dispatchers.IO) {
                            val AnonChat = AnonChatAPI()
                            AnonChat.sendPostRequest("search_user", data)
                        }
                        val gson = Gson()
                        val responseObject = gson.fromJson(response, ApiResponse::class.java)
                        if (responseObject.response.success) {
                            if(responseObject.response.chat_found){
                                val intentStart = Intent(this@ProfileActivity, ChatActivity::class.java).apply {
                                    putExtra("chat_id", responseObject.response.chat_id)
                                    putExtra("user_name", responseObject.response.user_name)
                                    putExtra("user_sex", responseObject.response.user_sex)
                                    putExtra("auth_token", auth_token)
                                }
                                startActivity(intentStart)
                                this@ProfileActivity.finish()
                            }else {
                                searchId = responseObject.response.search_id
                                isSearching = true
                                binding.searchBtn.text = "Идет поиск..."
                                binding.exitBtn.text = "Отмена"
                            }
                        }
                    }
                } else {
                    Snackbar.make(
                        it,
                        "Заполните все поля!",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

        messageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val type = intent?.getStringExtra("type")
                if (type == "chat_found") {
                    val intentStart = Intent(this@ProfileActivity, ChatActivity::class.java).apply {
                        putExtra("chat_id", intent.getStringExtra("chat_id"))
                        putExtra("user_name", intent.getStringExtra("user_name"))
                        putExtra("user_sex", intent.getStringExtra("user_sex"))
                        putExtra("auth_token", auth_token)
                    }
                    startActivity(intentStart)
                    this@ProfileActivity.finish()
                }
            }
        }
        val filter = IntentFilter("com.example.anonymous_chat")
        registerReceiver(messageReceiver, filter, RECEIVER_EXPORTED)
    }
    private fun setAgeOpacity(){
        binding.age1.alpha = 1f
        binding.age2.alpha = 1f
        binding.age3.alpha = 1f
        binding.age4.alpha = 1f
    }
    private fun searchAgeToggle(item: String) {
        if (searchAge.contains(item)) {
            searchAge.remove(item)
        } else {
            searchAge.add(item)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(messageReceiver)
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
            }
        }
    }

}
