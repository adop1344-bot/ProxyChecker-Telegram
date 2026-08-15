package com.letovpn.proxychecker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.letovpn.proxychecker.model.Proxy
import com.letovpn.proxychecker.model.ProxyStatus
import com.letovpn.proxychecker.ui.viewmodel.ProxyViewModel
import com.letovpn.proxychecker.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ProxyViewModel,
    settingsViewModel: SettingsViewModel
) {
    val proxies by viewModel.proxies.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()
    val checkProgress by viewModel.checkProgress.collectAsState()
    val checkTotal by viewModel.checkTotal.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proxy Checker") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                actions = {
                    IconButton(onClick = { navController.navigate("sources") }) {
                        Icon(Icons.Default.Add, contentDescription = "Sources")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (proxies.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.checkAllProxies() },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    text = { Text(if (isChecking) "$checkProgress/$checkTotal" else "Check All") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isChecking) {
                LinearProgressIndicator(
                    progress = { if (checkTotal > 0) checkProgress.toFloat() / checkTotal else 0f },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (proxies.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("No proxies yet", style = MaterialTheme.typography.titleMedium)
                        Text("Add sources or paste proxies", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { viewModel.fetchFromSources() }) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Fetch from sources")
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(proxies, key = { it.id }) { proxy ->
                        ProxyCard(proxy = proxy, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ProxyCard(proxy: Proxy, viewModel: ProxyViewModel) {
    val statusColor by animateColorAsState(
        targetValue = when (proxy.status) {
            ProxyStatus.WORKING -> MaterialTheme.colorScheme.primary
            ProxyStatus.DEAD -> MaterialTheme.colorScheme.error
            ProxyStatus.TESTING -> MaterialTheme.colorScheme.tertiary
            ProxyStatus.UNKNOWN -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(300), label = "status"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { viewModel.openInTelegram(proxy) }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(proxy.toDisplayString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(proxy.type.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (proxy.country != null) {
                        Text(proxy.country, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (proxy.latency > 0) {
                        Text("${proxy.latency}ms", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Surface(shape = MaterialTheme.shapes.small, color = statusColor, modifier = Modifier.size(12.dp)) {}
        }
    }
}
