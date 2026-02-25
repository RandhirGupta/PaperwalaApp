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
import com.paperwala.domain.repository.UserRepository
import com.paperwala.data.sync.BackgroundSyncScheduler
import com.paperwala.domain.ai.AiStatus
import com.paperwala.domain.ai.LlmModel
import com.paperwala.domain.ai.ModelManager
import com.paperwala.domain.model.ThemeMode
import com.paperwala.domain.model.UserPreferences
import com.paperwala.util.ApiKeys
import com.paperwala.util.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
            aiStatus = resolveAiStatus(prefs.enableLocalLlm, modelDownloaded),
            readingTimeMinutes = prefs.readingTimeMinutes,
            deliveryTimeHour = prefs.deliveryTimeHour,
            enableNotifications = prefs.enableNotifications,
            themeMode = prefs.themeMode,
            fontScale = prefs.fontScale
        )
    }

    private fun resolveAiStatus(enableLocalLlm: Boolean, modelDownloaded: Boolean): AiStatus {
        return when {
            enableLocalLlm && modelDownloaded -> AiStatus.LOCAL_LLM
            ApiKeys.GEMINI_API_KEY.isNotBlank() -> AiStatus.CLOUD
            else -> AiStatus.RULE_BASED
        }
    }

    /** Saves a preference change and applies a targeted state update. */
    private inline fun updatePreference(
        transform: (UserPreferences) -> UserPreferences,
        stateUpdate: (SettingsState) -> SettingsState
    ) {
        val prefs = userRepository.getPreferences()
        userRepository.savePreferences(transform(prefs))
        _state.update(stateUpdate)
    }

    fun toggleLocalLlm(enabled: Boolean) {
        updatePreference(
            transform = { it.copy(enableLocalLlm = enabled) },
            stateUpdate = { state ->
                val modelDownloaded = modelManager.isModelDownloaded(state.selectedModel)
                state.copy(
                    enableLocalLlm = enabled,
                    aiStatus = resolveAiStatus(enabled, modelDownloaded)
                )
            }
        )
    }

    fun selectModel(model: LlmModel) {
        val modelDownloaded = modelManager.isModelDownloaded(model)
        val modelSizeBytes = modelManager.getModelSizeBytes(model)
        updatePreference(
            transform = { it.copy(selectedLlmModel = model) },
            stateUpdate = {
                it.copy(
                    selectedModel = model,
                    isModelDownloaded = modelDownloaded,
                    modelSizeMb = modelSizeBytes / (1024 * 1024),
                    aiStatus = resolveAiStatus(it.enableLocalLlm, modelDownloaded)
                )
            }
        )
    }

    fun toggleNotifications(enabled: Boolean) {
        if (enabled) {
            notificationManager.requestPermission()
        }
        updatePreference(
            transform = { it.copy(enableNotifications = enabled) },
            stateUpdate = { it.copy(enableNotifications = enabled) }
        )
    }

    fun updateReadingTime(minutes: Int) {
        updatePreference(
            transform = { it.copy(readingTimeMinutes = minutes) },
            stateUpdate = { it.copy(readingTimeMinutes = minutes) }
        )
    }

    fun updateDeliveryTime(hour: Int) {
        updatePreference(
            transform = { it.copy(deliveryTimeHour = hour) },
            stateUpdate = { it.copy(deliveryTimeHour = hour) }
        )
        syncScheduler.scheduleEditionSync(hour)
    }

    fun downloadModel() {
        val model = _state.value.selectedModel
        screenModelScope.launch {
            _state.update {
                it.copy(isDownloading = true, downloadProgress = 0f, downloadError = null)
            }
            try {
                modelManager.downloadModel(model) { progress ->
                    _state.update { it.copy(downloadProgress = progress) }
                }
                val modelSizeBytes = modelManager.getModelSizeBytes(model)
                _state.update {
                    it.copy(
                        isDownloading = false,
                        isModelDownloaded = true,
                        modelSizeMb = modelSizeBytes / (1024 * 1024),
                        aiStatus = resolveAiStatus(it.enableLocalLlm, true)
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isDownloading = false, downloadError = e.message ?: "Download failed")
                }
            }
        }
    }

    fun deleteModel() {
        modelManager.deleteModel(_state.value.selectedModel)
        _state.update {
            it.copy(
                isModelDownloaded = false,
                modelSizeMb = 0,
                aiStatus = resolveAiStatus(it.enableLocalLlm, false)
            )
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        updatePreference(
            transform = { it.copy(themeMode = mode) },
            stateUpdate = { it.copy(themeMode = mode) }
        )
    }

    fun setFontScale(scale: Float) {
        updatePreference(
            transform = { it.copy(fontScale = scale) },
            stateUpdate = { it.copy(fontScale = scale) }
        )
    }
}
