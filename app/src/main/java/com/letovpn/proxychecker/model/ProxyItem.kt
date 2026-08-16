package com.letovpn.proxychecker.model

data class ProxyItem(
    val id: String = java.util.UUID.randomUUID().toString().take(8),
    val host: String,
    val port: Int,
    val type: String = "SOCKS5",
    val country: String = "",
    val latency: Long = -1,
    val isWorking: Boolean = false,
    val isTesting: Boolean = false
) {
    fun toTelegramUrl(): String {
        return when (type) {
            "MTProto" -> "tg://proxy?server=$host&port=$port"
            "HTTP" -> "tg://proxy?server=$host&port=$port"
            else -> "tg://socks?server=$host&port=$port"
        }
    }
    fun display(): String = "$host:$port"
}
