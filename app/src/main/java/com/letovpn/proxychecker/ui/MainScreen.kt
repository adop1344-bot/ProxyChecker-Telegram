package com.letovpn.proxychecker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.letovpn.proxychecker.model.ProxyItem
import com.letovpn.proxychecker.util.ProxyChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var items by remember { mutableStateOf(listOf<ProxyItem>()) }
    var isChecking by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSrc by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    val sources = listOf(
        "Proxy.org RU" to "https://raw.githubusercontent.com/kort0881/telegram-proxy-collector/main/proxy_ru.txt",
        "Proxy.org EU" to "https://raw.githubusercontent.com/kort0881/telegram-proxy-collector/main/proxy_eu.txt"
    )

    fun parseProxyText(text: String): List<ProxyItem> {
        return text.lines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .mapNotNull { line ->
                try {
                    val trimmed = line.trim()
                    when {
                        trimmed.startsWith("tg://proxy?") -> {
                            val params = trimmed.substringAfter("?").split("&")
                            var server = ""; var port = 0; var secret = ""
                            params.forEach { p ->
                                val kv = p.split("=", limit = 2)
                                if (kv.size == 2) {
                                    when (kv[0]) {
                                        "server" -> server = kv[1]
                                        "port" -> port = kv[1].toIntOrNull() ?: 0
                                        "secret" -> secret = kv[1]
                                    }
                                }
                            }
                            if (server.isNotBlank() && port > 0) ProxyItem(host = server, port = port, secret = secret) else null
                        }
                        trimmed.contains(":") && !trimmed.contains("/") -> {
                            val parts = trimmed.split(":")
                            if (parts.size >= 2) {
                                val host = parts[0]
                                val port = parts[1].toIntOrNull()
                                val secret = if (parts.size >= 3) parts[2] else ""
                                if (host.isNotBlank() && port != null && port > 0) ProxyItem(host = host, port = port, secret = secret) else null
                            } else null
                        }
                        else -> null
                    }
                } catch (e: Exception) { null }
            }
    }

    suspend fun loadFromUrl(url: String): List<ProxyItem> = withContext(Dispatchers.IO) {
        val text = java.net.URL(url).readText()
        parseProxyText(text)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MTProxy Checker") },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primaryContainer),
                actions = {
                    IconButton(onClick = { showSrc = true }) { Icon(Icons.Default.Add, "Add") }
                    IconButton(onClick = { items = emptyList(); errorMsg = "" }) { Icon(Icons.Default.Delete, "Clear") }
                    IconButton(onClick = { navController.navigate("settings") }) { Icon(Icons.Default.Settings, "Settings") }
                }
            )
        },
        floatingActionButton = {
            if (items.isNotEmpty() && !isChecking) {
                FloatingActionButton(onClick = {
                    isChecking = true
                    scope.launch(Dispatchers.IO) {
                        items = items.map { it.copy(isTesting = true) }
                        items = ProxyChecker.checkAll(items)
                        isChecking = false
                    }
                }) {
                    Icon(Icons.Default.PlayArrow, "Check")
                }
            }
        }
    ) { p ->
        if (isLoading || isChecking) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(p))
        }

        if (errorMsg.isNotBlank()) {
            Text(errorMsg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(p).padding(16.dp))
        }

        if (items.isEmpty() && !isLoading) {
            Box(Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Public, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("No MTProxy", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Choose a source below", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(24.dp))

                    sources.forEach { (name, url) ->
                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                errorMsg = ""
                                scope.launch {
                                    try {
                                        val parsed = loadFromUrl(url)
                                        items = parsed
                                        if (parsed.isEmpty()) errorMsg = "No proxies found in source"
                                    } catch (e: Exception) {
                                        errorMsg = "Failed to load: ${e.message}"
                                    }
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 4.dp),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.CloudDownload, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Load from $name")
                        }
                    }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.id }) { proxy ->
                    Card(
                        onClick = {
                            val i = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(proxy.toTelegramUrl()))
                            i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            ctx.startActivity(i)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(proxy.display(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("MTProto", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (proxy.country.isNotBlank()) Text(proxy.country, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (proxy.latency > 0) Text("${proxy.latency}ms", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (proxy.isTesting) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = if (proxy.isWorking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(12.dp)
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSrc) {
        var url by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSrc = false },
            title = { Text("Custom source URL") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter URL with proxy list:", style = MaterialTheme.typography.bodySmall)
                    Text("Supports: tg://proxy?server=...&port=... OR ip:port", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (url.isNotBlank()) {
                        showSrc = false
                        isLoading = true
                        errorMsg = ""
                        scope.launch {
                            try {
                                val parsed = loadFromUrl(url)
                                items = items + parsed
                                if (parsed.isEmpty()) errorMsg = "No proxies found"
                            } catch (e: Exception) {
                                errorMsg = "Error: ${e.message}"
                            }
                            isLoading = false
                        }
                    }
                }) { Text("Load") }
            },
            dismissButton = { TextButton(onClick = { showSrc = false }) { Text("Cancel") } }
        )
    }
}
