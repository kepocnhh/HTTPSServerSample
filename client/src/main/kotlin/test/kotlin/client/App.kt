package test.kotlin.client

import kotlin.time.Duration.Companion.seconds
import okhttp3.OkHttpClient
import okhttp3.Request

fun main() {
    val client = OkHttpClient.Builder()
        .callTimeout(10.seconds)
        .readTimeout(5.seconds)
        .writeTimeout(5.seconds)
        .build()
    val request = Request.Builder()
        .url("https://0.0.0.0:8080/foo/bar/baz")
        .build()
    client.newCall(request = request).execute().use { response ->
        when (val code = response.code) {
            200 -> {
                println("body: ${response.body.string()}")
            }
            else -> error("Unknow code: $code!")
        }
    }
}
