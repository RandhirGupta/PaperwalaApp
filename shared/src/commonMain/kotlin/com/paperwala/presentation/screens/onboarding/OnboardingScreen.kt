package com.paperwala.presentation.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.paperwala.presentation.components.TopicChip
import com.paperwala.presentation.screens.morningedition.MorningEditionScreen
import com.paperwala.presentation.theme.PaperwalaColors

class OnboardingScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<OnboardingViewModel>()
        val state by viewModel.state.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { (state.currentStep + 1) / 4f },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                color = PaperwalaColors.MastheadRed,
                trackColor = PaperwalaColors.DividerColor
            )

            // Step indicator
            Text(
                text = "Step ${state.currentStep + 1} of 4",
                style = MaterialTheme.typography.labelMedium,
                color = PaperwalaColors.InkLightGray,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // Content area
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width } togetherWith
                            slideOutHorizontally { width -> -width }
                },
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    0 -> TopicSelectionStep(viewModel, state)
                    1 -> SourceSelectionStep(viewModel, state)
                    2 -> ReadingTimeStep(viewModel, state)
                    3 -> DeliveryTimeStep(viewModel, state)
                }
            }

            // Bottom navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.currentStep > 0) {
                    TextButton(onClick = { viewModel.previousStep() }) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (state.currentStep < 3) {
                            viewModel.nextStep()
                        } else {
                            viewModel.completeOnboarding {
                                navigator.replaceAll(MorningEditionScreen())
                            }
                        }
                    },
                    enabled = state.canProceed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PaperwalaColors.MastheadRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (state.currentStep < 3) "Next" else "Start Reading",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicSelectionStep(viewModel: OnboardingViewModel, state: OnboardingState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "What interests you?",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select at least 3 topics to personalize your morning edition",
            style = MaterialTheme.typography.bodyLarge,
            color = PaperwalaColors.InkLightGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.allTopics) { topic ->
                TopicChip(
                    topic = topic,
                    isSelected = topic in state.selectedTopics,
                    onClick = { viewModel.toggleTopic(topic) }
                )
            }
        }
    }
}

@Composable
private fun SourceSelectionStep(viewModel: OnboardingViewModel, state: OnboardingState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pick your newspapers",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose the sources you trust and enjoy reading",
                style = MaterialTheme.typography.bodyLarge,
                color = PaperwalaColors.InkLightGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "INDIAN NEWSPAPERS",
                style = MaterialTheme.typography.titleLarge,
                color = PaperwalaColors.InkGray
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(viewModel.indianSources) { source ->
            SourceRow(
                name = source.displayName,
                isSelected = source.id in state.selectedSources,
                onClick = { viewModel.toggleSource(source.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "INTERNATIONAL",
                style = MaterialTheme.typography.titleLarge,
                color = PaperwalaColors.InkGray
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(viewModel.internationalSources) { source ->
            SourceRow(
                name = source.displayName,
                isSelected = source.id in state.selectedSources,
                onClick = { viewModel.toggleSource(source.id) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SourceRow(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PaperwalaColors.MastheadRed.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() }
            )
        }
    }
}

@Composable
private fun ReadingTimeStep(viewModel: OnboardingViewModel, state: OnboardingState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "How long do you want to read?",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We'll curate the right number of stories to fit your time",
            style = MaterialTheme.typography.bodyLarge,
            color = PaperwalaColors.InkLightGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            viewModel.readingTimeOptions.forEach { minutes ->
                val isSelected = state.readingTimeMinutes == minutes
                Surface(
                    modifier = Modifier
                        .size(72.dp)
                        .clickable { viewModel.setReadingTime(minutes) },
                    shape = CircleShape,
                    color = if (isSelected) PaperwalaColors.MastheadRed
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$minutes",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PaperwalaColors.PaperWhite
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "min",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) PaperwalaColors.PaperWhite.copy(alpha = 0.8f)
                                else PaperwalaColors.InkLightGray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val articleCount = state.readingTimeMinutes / 3
        Text(
            text = "~$articleCount stories \u00b7 Perfect for your morning routine",
            style = MaterialTheme.typography.bodyMedium,
            color = PaperwalaColors.InkLightGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DeliveryTimeStep(viewModel: OnboardingViewModel, state: OnboardingState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "When should we deliver?",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your paper will be ready and waiting",
            style = MaterialTheme.typography.bodyLarge,
            color = PaperwalaColors.InkLightGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Time selector - hours from 5 AM to 10 AM
        val hours = (5..10).toList()
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            hours.forEach { hour ->
                val isSelected = state.deliveryTimeHour == hour
                val timeLabel = if (hour < 12) "$hour:00 AM" else "$hour:00 PM"

                OutlinedButton(
                    onClick = { viewModel.setDeliveryTime(hour) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) PaperwalaColors.MastheadRed.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) {
                        ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            width = 2.dp
                        )
                    } else {
                        ButtonDefaults.outlinedButtonBorder(enabled = true)
                    }
                ) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isSelected) PaperwalaColors.MastheadRed
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your paper will be curated by ${state.deliveryTimeHour}:00 AM",
            style = MaterialTheme.typography.bodyMedium,
            color = PaperwalaColors.InkLightGray,
            textAlign = TextAlign.Center
        )
    }
}
