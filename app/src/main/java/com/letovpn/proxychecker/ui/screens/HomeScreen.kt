package com.letovpn.proxychecker.ui.screens

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
                FloatingActionButton(onClick = { viewModel.checkAllProxies() }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Check")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isChecking) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (proxies.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Public, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("No proxies", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("Add sources to get started", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(proxies, key = { it.id }) { proxy ->
                        ProxyCard(proxy)
                    }
                }
            }
        }
    }
}

@Composable
fun ProxyCard(proxy: Proxy) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(proxy.toDisplayString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(proxy.type.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (proxy.country != null) Text(proxy.country!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (proxy.latency > 0) Text("${proxy.latency}ms", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Surface(shape = MaterialTheme.shapes.small, color = if (proxy.status == ProxyStatus.WORKING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(12.dp)) {}
        }
    }
}
