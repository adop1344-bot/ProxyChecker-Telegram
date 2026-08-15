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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var proxies by remember { mutableStateOf(listOf<ProxyItem>()) }
    var isChecking by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proxy Checker") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                actions = {
                    IconButton(onClick = {
                        val p = listOf(
                            ProxyItem(host = "185.199.108.154", port = 1080, country = "USA"),
                            ProxyItem(host = "45.67.89.10", port = 1080, country = "Netherlands"),
                            ProxyItem(host = "103.45.67.89", port = 4153, country = "Singapore"),
                            ProxyItem(host = "192.168.1.1", port = 3128, type = "HTTP", country = "Germany")
                        )
                        proxies = proxies + p
                    }) { Icon(Icons.Default.Add, "Add") }
                    IconButton(onClick = { proxies = emptyList() }) { Icon(Icons.Default.Delete, "Clear") }
                    IconButton(onClick = { navController.navigate("settings") }) { Icon(Icons.Default.Settings, "Settings") }
                }
            )
        },
        floatingActionButton = {
            if (proxies.isNotEmpty()) {
                FloatingActionButton(onClick = {
                    isChecking = true
                    scope.launch {
                        proxies = proxies.map { it.copy(isTesting = true) }
                        proxies = ProxyChecker.checkMany(proxies)
                        isChecking = false
                    }
                }) {
                    if (isChecking) Icon(Icons.Default.HourglassEmpty, "...")
                    else Icon(Icons.Default.PlayArrow, "Check")
                }
            }
        }
    ) { padding ->
        if (proxies.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Public, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("No proxies", style = MaterialTheme.typography.titleMedium)
                    Text("Tap + to add test proxies", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(proxies, key = { it.id }) { p ->
                    Card(Modifier.fillMaxWidth(), onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(p.toTelegramUrl()))
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(p.display(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(p.type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (p.country.isNotBlank()) Text(p.country, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (p.latency > 0) Text("${p.latency}ms", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (p.isTesting) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Surface(shape = MaterialTheme.shapes.small, color = if (p.isWorking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(12.dp)) {}
                            }
                        }
                    }
                }
            }
        }
    }
}
