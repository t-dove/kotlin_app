package com.example.anonymous_chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.anonymous_chat.databinding.ActivityMainBinding
import com.example.cyberhousenotifications.AnonChatAPI
import com.google.android.material.snackbar.Snackbar

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private val userAuthViewModel: UserAuthViewModel by viewModels()
    private var auth_token: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        userAuthViewModel.getUserAuth().observe(this, Observer { userAuth ->
            userAuth?.let {
                val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                startActivity(intent)
                this.finish()
            }
            })
        binding.button?.setOnClickListener(){
            userAuthViewModel.getUserAuth().observe(this, Observer { userAuth ->
                userAuth?.let {
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    startActivity(intent)
                    this.finish()
                } ?: run {
                    if(auth_token.isEmpty() && binding.emailText != null && !loadFcmToken(this@MainActivity).isNullOrEmpty()){
                        //    binding.textView2.text = "Нет сохраненных токенов"
                        //    userAuthViewModel.insertUserAuth("GIGA_CHAD")
                        val data = mapOf(
                            "email" to binding.emailText.text,
                            "fcm_token" to loadFcmToken(this@MainActivity).toString()
                        )
                        CoroutineScope(Dispatchers.Main).launch {
                            val response = withContext(Dispatchers.IO) {
                                val AnonChat = AnonChatAPI()
                                AnonChat.sendPostRequest("auth", data)
                            }
                            val gson = Gson()
                            val responseObject = gson.fromJson(response, ApiResponse::class.java)
                            Log.i(
                                "server_api",
                                "Sended API AUTH to email: ${binding.emailText.text}"
                            )
                            if (responseObject.response.success) {
                                auth_token = responseObject.response.auth_token
                                binding.textView2.text = "Введите код, отправленный на почту"
                                binding.emailText.setText("")
                                binding.emailText.hint = "Введите код"
                                Snackbar.make(
                                    it,
                                    "Осталось ввести код с почты!",
                                    Snackbar.LENGTH_SHORT
                                )
                                    .show()
                            } else {
                                Log.i(
                                    "server_api",
                                    "API request failure ${responseObject.response.success}"
                                )
                            }
                        }
                    }else if(binding.emailText != null){

                        val data = mapOf(
                            "auth_token" to auth_token,
                            "code" to binding.emailText.text
                        )
                        CoroutineScope(Dispatchers.Main).launch {
                            val response = withContext(Dispatchers.IO) {
                                val AnonChat = AnonChatAPI()
                                AnonChat.sendPostRequest("confirm_code", data)
                            }
                            val gson = Gson()
                            val responseObject = gson.fromJson(response, ApiResponse::class.java)
                            if (responseObject.response.success) {
                                userAuthViewModel.insertUserAuth(auth_token)
                                val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                                startActivity(intent)
                            }else{
                                Snackbar.make(
                                    it,
                                    "Введен неверный код!",
                                    Snackbar.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }else{

                    }
                }
            })
          //  val intent = Intent(this, ChatActivity::class.java)
          //  startActivity(intent)
            }
        }
    private fun loadFcmToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences("ANON_CHAT_USERDATA_AUTH", Context.MODE_PRIVATE)
        return sharedPref.getString("fcm_token", null)
    }

    }
