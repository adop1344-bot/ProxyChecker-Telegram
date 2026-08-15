package com.letovpn.proxychecker.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    private val _telegramChannel = MutableStateFlow("@letovpn_free")
    val telegramChannel: StateFlow<String> = _telegramChannel.asStateFlow()
    private val _autoCheckOnAdd = MutableStateFlow(true)
    val autoCheckOnAdd: StateFlow<Boolean> = _autoCheckOnAdd.asStateFlow()
    private val _showCountryFlags = MutableStateFlow(true)
    val showCountryFlags: StateFlow<Boolean> = _showCountryFlags.asStateFlow()

    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        val TELEGRAM_CHANNEL_KEY = stringPreferencesKey("telegram_channel")
        val AUTO_CHECK_KEY = booleanPreferencesKey("auto_check")
        val SHOW_FLAGS_KEY = booleanPreferencesKey("show_flags")
    }

    init {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _isDarkTheme.value = preferences[DARK_THEME_KEY] ?: false
                _telegramChannel.value = preferences[TELEGRAM_CHANNEL_KEY] ?: "@letovpn_free"
                _autoCheckOnAdd.value = preferences[AUTO_CHECK_KEY] ?: true
                _showCountryFlags.value = preferences[SHOW_FLAGS_KEY] ?: true
            }
        }
    }

    fun setDarkTheme(enabled: Boolean) { viewModelScope.launch { dataStore.edit { it[DARK_THEME_KEY] = enabled } } }
    fun setTelegramChannel(channel: String) { viewModelScope.launch { dataStore.edit { it[TELEGRAM_CHANNEL_KEY] = channel } } }
    fun setAutoCheck(enabled: Boolean) { viewModelScope.launch { dataStore.edit { it[AUTO_CHECK_KEY] = enabled } } }
    fun setShowFlags(enabled: Boolean) { viewModelScope.launch { dataStore.edit { it[SHOW_FLAGS_KEY] = enabled } } }
    fun toggleTheme() { setDarkTheme(!_isDarkTheme.value) }
}
