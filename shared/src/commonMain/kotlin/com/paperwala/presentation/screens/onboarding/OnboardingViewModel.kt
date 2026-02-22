package com.paperwala.presentation.screens.onboarding

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.paperwala.data.remote.api.NewsSources
import com.paperwala.data.repository.UserRepository
import com.paperwala.domain.model.TopicCategory
import com.paperwala.domain.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingState(
    val currentStep: Int = 0,
    val selectedTopics: Set<TopicCategory> = emptySet(),
    val selectedSources: Set<String> = emptySet(),
    val readingTimeMinutes: Int = 10,
    val deliveryTimeHour: Int = 7,
    val canProceed: Boolean = false
)

class OnboardingViewModel(
    private val userRepository: UserRepository
) : ScreenModel {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    val allTopics = TopicCategory.entries
    val indianSources = NewsSources.INDIAN_SOURCES
    val internationalSources = NewsSources.INTERNATIONAL_SOURCES
    val readingTimeOptions = listOf(5, 10, 15, 20, 30)

    fun toggleTopic(topic: TopicCategory) {
        val current = _state.value.selectedTopics.toMutableSet()
        if (topic in current) current.remove(topic) else current.add(topic)
        _state.value = _state.value.copy(
            selectedTopics = current,
            canProceed = current.size >= 3
        )
    }

    fun toggleSource(sourceId: String) {
        val current = _state.value.selectedSources.toMutableSet()
        if (sourceId in current) current.remove(sourceId) else current.add(sourceId)
        _state.value = _state.value.copy(
            selectedSources = current,
            canProceed = current.isNotEmpty()
        )
    }

    fun setReadingTime(minutes: Int) {
        _state.value = _state.value.copy(
            readingTimeMinutes = minutes,
            canProceed = true
        )
    }

    fun setDeliveryTime(hour: Int) {
        _state.value = _state.value.copy(
            deliveryTimeHour = hour,
            canProceed = true
        )
    }

    fun nextStep() {
        val nextStep = _state.value.currentStep + 1
        _state.value = _state.value.copy(
            currentStep = nextStep,
            canProceed = when (nextStep) {
                0 -> _state.value.selectedTopics.size >= 3
                1 -> _state.value.selectedSources.isNotEmpty()
                2 -> true // Reading time always has a default
                3 -> true // Delivery time always has a default
                else -> false
            }
        )
    }

    fun previousStep() {
        val prevStep = (_state.value.currentStep - 1).coerceAtLeast(0)
        _state.value = _state.value.copy(
            currentStep = prevStep,
            canProceed = true
        )
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        screenModelScope.launch {
            val state = _state.value
            val prefs = UserPreferences(
                selectedTopics = state.selectedTopics.toList(),
                preferredSources = state.selectedSources.toList(),
                readingTimeMinutes = state.readingTimeMinutes,
                deliveryTimeHour = state.deliveryTimeHour,
                hasCompletedOnboarding = true,
                enableNotifications = true,
                enableLocalLlm = false
            )
            userRepository.savePreferences(prefs)
            onComplete()
        }
    }
}
