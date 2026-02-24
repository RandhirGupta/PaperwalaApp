/*
 * Copyright 2026 Randhir Gupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paperwala.presentation.screens.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.paperwala.data.repository.UserRepository
import com.paperwala.data.sync.BackgroundSyncScheduler
import com.paperwala.domain.ai.AiStatus
import com.paperwala.domain.ai.LlmModel
import com.paperwala.domain.ai.ModelManager
import com.paperwala.domain.model.ThemeMode
import com.paperwala.util.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val enableLocalLlm: Boolean = false,
    val selectedModel: LlmModel = LlmModel.PHI_3_MINI,
    val isModelDownloaded: Boolean = false,
    val modelSizeMb: Long = 0,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadError: String? = null,
    val aiStatus: AiStatus = AiStatus.RULE_BASED,
    val readingTimeMinutes: Int = 10,
    val deliveryTimeHour: Int = 7,
    val enableNotifications: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontScale: Float = 1.0f
)

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val modelManager: ModelManager,
    private val notificationManager: NotificationManager,
    private val syncScheduler: BackgroundSyncScheduler
) : ScreenModel {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val prefs = userRepository.getPreferences()
        val selectedModel = prefs.selectedLlmModel
        val modelDownloaded = modelManager.isModelDownloaded(selectedModel)
        val modelSizeBytes = modelManager.getModelSizeBytes(selectedModel)

        _state.value = SettingsState(
            enableLocalLlm = prefs.enableLocalLlm,
            selectedModel = selectedModel,
            isModelDownloaded = modelDownloaded,
            modelSizeMb = modelSizeBytes / (1024 * 1024),
            aiStatus = when {
                prefs.enableLocalLlm && modelDownloaded -> AiStatus.LOCAL_LLM
                com.paperwala.util.Constants.GEMINI_API_KEY.isNotBlank() -> AiStatus.CLOUD
                else -> AiStatus.RULE_BASED
            },
            readingTimeMinutes = prefs.readingTimeMinutes,
            deliveryTimeHour = prefs.deliveryTimeHour,
            enableNotifications = prefs.enableNotifications,
            themeMode = prefs.themeMode,
            fontScale = prefs.fontScale
        )
    }

    fun toggleLocalLlm(enabled: Boolean) {
        val prefs = userRepository.getPreferences()
        userRepository.savePreferences(prefs.copy(enableLocalLlm = enabled))
        loadSettings()
    }

    fun selectModel(model: LlmModel) {
        val prefs = userRepository.getPreferences()
        userRepository.savePreferences(prefs.copy(selectedLlmModel = model))
        loadSettings()
    }

    fun toggleNotifications(enabled: Boolean) {
        if (enabled) {
            notificationManager.requestPermission()
        }
        val prefs = userRepository.getPreferences()
        userRepository.savePreferences(prefs.copy(enableNotifications = enabled))
        loadSettings()
    }

    fun updateDeliveryTime(hour: Int) {
        val prefs = userRepository.getPreferences()
        userRepository.savePreferences(prefs.copy(deliveryTimeHour = hour))
        syncScheduler.scheduleEditionSync(hour)
        loadSettings()
    }

    fun downloadModel() {
        val model = _state.value.selectedModel
        screenModelScope.launch {
            _state.value = _state.value.copy(
                isDownloading = true,
                downloadProgress = 0f,
                downloadError = null
            )
            try {
                modelManager.downloadModel(model) { progress ->
                    _state.value = _state.value.copy(downloadProgress = progress)
                }
                _state.value = _state.value.copy(
                    isDownloading = false,
                    isModelDownloaded = true,
                    modelSizeMb = modelManager.getModelSizeBytes(model) / (1024 * 1024)
                )
                loadSettings()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isDownloading = false,
                    downloadError = e.message ?: "Download failed"
                )
            }
        }
    }

    fun deleteModel() {
        modelManager.deleteModel(_state.value.selectedModel)
        loadSettings()
    }

    fun setThemeMode(mode: ThemeMode) {
        val prefs = userRepository.getPreferences()
        userRepository.savePreferences(prefs.copy(themeMode = mode))
        loadSettings()
    }

    fun setFontScale(scale: Float) {
        val prefs = userRepository.getPreferences()
        userRepository.savePreferences(prefs.copy(fontScale = scale))
        loadSettings()
    }
}
