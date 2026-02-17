package test.kotlin.client

import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.X509TrustManager
import kotlin.time.Duration.Companion.seconds
import okhttp3.OkHttpClient
import okhttp3.Request

fun main() {
    val cf = CertificateFactory.getInstance("x509")
    val caCrt = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream("ca.crt")
        ?.use(cf::generateCertificate)
        ?: error("No CA crt!")
    check(caCrt is X509Certificate)
    val trustManager = object : X509TrustManager {
        override fun checkClientTrusted(
            chain: Array<out X509Certificate?>?,
            authType: String?,
        ) {
            // noop
        }

        override fun checkServerTrusted(
            chain: Array<out X509Certificate?>?,
            authType: String?,
        ) {
            // noop
        }

        override fun getAcceptedIssuers(): Array<out X509Certificate?> {
            return arrayOf(caCrt)
        }
    }
    val sc = SSLContext.getInstance("tls")
    sc.init(null, arrayOf(trustManager), SecureRandom.getInstanceStrong())
    val hostname = "0.0.0.0"
    val hv = HostnameVerifier { actual, _ -> hostname == actual }
    val client = OkHttpClient.Builder()
        .callTimeout(10.seconds)
        .readTimeout(5.seconds)
        .writeTimeout(5.seconds)
        .sslSocketFactory(sc.socketFactory, trustManager)
        .hostnameVerifier(hv)
        .build()
    val timestamp = System.currentTimeMillis()
    println("timestamp: $timestamp")
    val request = Request.Builder()
        .url("https://${hostname}:8080/timestamp/$timestamp")
        .build()
    client.newCall(request).execute().use { response ->
        when (val code = response.code) {
            200 -> println("body: ${response.body.string()}")
            else -> error("Unknow code: $code!")
        }
    }
}
