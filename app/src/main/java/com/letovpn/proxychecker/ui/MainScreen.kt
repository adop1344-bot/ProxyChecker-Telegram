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
import com.letovpn.proxychecker.util.SourceFetcher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var items by remember { mutableStateOf(listOf<ProxyItem>()) }
    var isChecking by remember { mutableStateOf(false) }
    var showSrc by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proxy Checker") },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primaryContainer),
                actions = {
                    IconButton(onClick = { showSrc = true }) { Icon(Icons.Default.Add, "Add") }
                    IconButton(onClick = { items = emptyList() }) { Icon(Icons.Default.Delete, "Clear") }
                    IconButton(onClick = { navController.navigate("settings") }) { Icon(Icons.Default.Settings, "Settings") }
                }
            )
        },
        floatingActionButton = {
            if (items.isNotEmpty()) {
                FloatingActionButton(onClick = {
                    isChecking = true
                    scope.launch {
                        items = items.map { it.copy(isTesting = true) }
                        items = ProxyChecker.checkAll(items)
                        isChecking = false
                    }
                }) {
                    if (isChecking) Icon(Icons.Default.HourglassEmpty, "...")
                    else Icon(Icons.Default.PlayArrow, "Check")
                }
            }
        }
    ) { p ->
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Public, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("No proxies", style = MaterialTheme.typography.titleMedium)
                    Text("Tap + to add sources", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    Text(proxy.type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            title = { Text("Add proxy source") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter URL with proxy list (ip:port per line):", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Or use quick add:", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { url = "https://api.proxyscrape.com/v2/?request=get&protocol=socks5&timeout=10000&country=all" }) { Text("SOCKS5") }
                        OutlinedButton(onClick = { url = "https://api.proxyscrape.com/v2/?request=get&protocol=socks4&timeout=10000&country=all" }) { Text("SOCKS4") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (url.isNotBlank()) {
                        showSrc = false
                        scope.launch {
                            val fetched = SourceFetcher.fetchFromUrl(url)
                            items = items + fetched
                        }
                    }
                }) { Text("Fetch") }
            },
            dismissButton = { TextButton(onClick = { showSrc = false }) { Text("Cancel") } }
        )
    }
}
