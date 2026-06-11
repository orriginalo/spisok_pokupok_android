package com.example.list

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.IOException

private val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shopping_settings")

data class ShoppingUiState(
    val items: List<String> = emptyList(),
    val inputText: String = "",
    val isDarkTheme: Boolean = false
)

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    private val itemsKey = stringPreferencesKey("shopping_items")
    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val inputText = MutableStateFlow("")
    private val dataStore = application.dataStore

    private val savedState = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }

    val uiState = combine(savedState, inputText) { preferences, currentInput ->
        ShoppingUiState(
            items = decodeItems(preferences[itemsKey]),
            inputText = currentInput,
            isDarkTheme = preferences[darkThemeKey] ?: false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShoppingUiState()
    )

    fun updateInput(value: String) {
        inputText.value = value
    }

    fun addItem() {
        val newItem = inputText.value.trim()
        if (newItem.isEmpty()) return

        viewModelScope.launch {
            dataStore.edit { preferences ->
                val currentItems = decodeItems(preferences[itemsKey]).toMutableList()
                currentItems.add(newItem)
                preferences[itemsKey] = encodeItems(currentItems)
            }
            inputText.value = ""
        }
    }

    fun removeItem(index: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val currentItems = decodeItems(preferences[itemsKey]).toMutableList()
                if (index in currentItems.indices) {
                    currentItems.removeAt(index)
                    preferences[itemsKey] = encodeItems(currentItems)
                }
            }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[darkThemeKey] = enabled
            }
        }
    }

    private fun decodeItems(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()

        val jsonArray = JSONArray(value)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                add(jsonArray.getString(index))
            }
        }
    }

    private fun encodeItems(items: List<String>): String {
        return JSONArray(items).toString()
    }
}
