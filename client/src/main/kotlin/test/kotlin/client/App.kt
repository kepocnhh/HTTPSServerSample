package test.kotlin.client

import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.X509TrustManager
import kotlin.time.Duration.Companion.seconds
import okhttp3.OkHttpClient
import okhttp3.Request

fun main() {
    val password = "qwe123".toCharArray()
    val ks = KeyStore.getInstance("pkcs12")
    val issuer = "foo"
//    val issuer = "bar"
    Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream("$issuer.pkcs12")!!.use { src ->
            ks.load(src, password)
        }
    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    kmf.init(ks, password)
    val cf = CertificateFactory.getInstance("x509")
    val caCrt = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream("ca.crt")
        ?.use(cf::generateCertificate)
        as X509Certificate
    val key = ks.getKey(issuer, password) as PrivateKey
    val crt = ks.getCertificate(issuer) as X509Certificate
//    crt.verify(caCrt.publicKey)
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
    sc.init(kmf.keyManagers, arrayOf(trustManager), SecureRandom.getInstanceStrong())
    val hostname = "0.0.0.0"
    val hv = HostnameVerifier { actual, _ -> hostname == actual }
    val client = OkHttpClient.Builder()
        .callTimeout(8.seconds)
        .readTimeout(4.seconds)
        .writeTimeout(4.seconds)
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
