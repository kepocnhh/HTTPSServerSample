package test.kotlin.https

import java.net.ServerSocket

fun main() {
    ServerSocket(8080).use { ss ->
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
