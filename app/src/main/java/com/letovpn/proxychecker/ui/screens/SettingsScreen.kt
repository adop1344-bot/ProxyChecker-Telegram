package com.letovpn.proxychecker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.letovpn.proxychecker.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val telegramChannel by viewModel.telegramChannel.collectAsState()
    val autoCheckOnAdd by viewModel.autoCheckOnAdd.collectAsState()
    val showCountryFlags by viewModel.showCountryFlags.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            // Theme toggle
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dark theme", style = MaterialTheme.typography.bodyLarge)
                    Text("Switch between light and dark", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isDarkTheme, onCheckedChange = { viewModel.setDarkTheme(it) })
            }

            HorizontalDivider()
            Text("Behavior", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            // Auto check
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-check on add", style = MaterialTheme.typography.bodyLarge)
                    Text("Check proxy immediately after adding", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = autoCheckOnAdd, onCheckedChange = { viewModel.setAutoCheck(it) })
            }

            // Show country flags
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Show country flags", style = MaterialTheme.typography.bodyLarge)
                    Text("Display country flags for proxies", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = showCountryFlags, onCheckedChange = { viewModel.setShowFlags(it) })
            }

            HorizontalDivider()
            Text("Telegram", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            // Telegram channel
            OutlinedTextField(
                value = telegramChannel,
                onValueChange = { viewModel.setTelegramChannel(it) },
                label = { Text("Telegram channel") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Open channel button
            OutlinedButton(
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/letovpn_free"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open @letovpn_free")
            }

            HorizontalDivider()
            Text("About", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            Text("Proxy Checker v1.0.0", style = MaterialTheme.typography.bodyMedium)
            Text("Made with ❤️ for Telegram", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
