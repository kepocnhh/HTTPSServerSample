package test.kotlin.https

import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocket

fun main() {
    val password = "qwe123".toCharArray()
    val ks = KeyStore.getInstance("pkcs12")
    Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream("ca.pkcs12")!!.use { src ->
            ks.load(src, password)
        }
    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    kmf.init(ks, password)
    val sc = SSLContext.getInstance("tls")
    sc.init(kmf.keyManagers, null, SecureRandom.getInstanceStrong())
    sc.serverSocketFactory.createServerSocket(8080)!!.use { ss ->
        check(ss is SSLServerSocket)
        ss.needClientAuth = true
        ss.enabledCipherSuites = arrayOf("TLS_AES_128_GCM_SHA256")
        ss.enabledProtocols = arrayOf("TLSv1.3")
        println("start server ${ss.inetAddress.hostAddress}:${ss.localPort}")
        val version = "1.1"
        while (true) {
            println("waiting...")
            ss.accept().use { socket ->
                val lines = mutableListOf<String>()
                socket.getInputStream().bufferedReader().let { reader ->
                    while (true) {
                        val line = reader.readLine() ?: break
                        if (line.isEmpty()) break // todo
                        lines.add(line)
                    }
                }
                lines.forEachIndexed { index, line ->
                    println("$index] $line")
                }
                val writer = socket.getOutputStream().bufferedWriter()
                val code = 200
                val message = "OK"
                val builder = StringBuilder()
                    .append("HTTP/$version $code $message")
                    .append("\r\n")
                val body: String? = "foobarbaz"
                val headers = mutableMapOf("foo" to "bar")
                if (body != null) {
                    headers["Content-Length"] = body.length.toString()
                }
                headers.forEach { (key, value) ->
                    builder.append("$key: $value")
                        .append("\r\n")
                }
                builder.append("\r\n")
                if (body != null) {
                    builder.append(body)
                }
                writer.write(builder.toString())
                writer.flush()
            }
        }
    }
}
