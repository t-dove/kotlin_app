package com.example.cyberhousenotifications

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AnonChatAPI {

    fun sendPostRequest(methodName: String, data: Map<String, CharSequence>): String {
        val url = URL("https://dev.cr-house.ru/main.php")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true

        val postData = StringBuilder()
        postData.append("method=").append(methodName)
        for ((key, value) in data) {
            postData.append("&").append(key).append("=").append(value)
        }

        val wr = OutputStreamWriter(connection.outputStream)
        wr.write(postData.toString())
        wr.flush()

        val response = StringBuilder()
        connection.inputStream.bufferedReader().useLines { lines ->
            lines.forEach {
                response.append(it)
            }
        }

        return response.toString()
    }
}
