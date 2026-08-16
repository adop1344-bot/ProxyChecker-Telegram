package com.letovpn.proxychecker.util

import com.letovpn.proxychecker.model.ProxyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.URL

object SourceFetcher {

    suspend fun fetchFromUrl(url: String): List<ProxyItem> = withContext(Dispatchers.IO) {
        try {
            val text = URL(url).readText()
            text.lines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    try {
                        val parts = line.trim().split(":")
                        if (parts.size >= 2) {
                            ProxyItem(host = parts[0], port = parts[1].toInt())
                        } else null
                    } catch (e: Exception) { null }
                }
        } catch (e: Exception) { emptyList() }
    }
}
