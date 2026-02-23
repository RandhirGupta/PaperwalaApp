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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.paperwala.domain.ai.AiStatus
import com.paperwala.domain.ai.LlmModel
import com.paperwala.presentation.theme.PaperwalaColors

class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<SettingsViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\u2190",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .let { modifier ->
                            modifier
                        }
                )
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Summaries section
            AiSettingsSection(
                state = state,
                onToggleLocalLlm = viewModel::toggleLocalLlm,
                onSelectModel = viewModel::selectModel,
                onDownloadModel = viewModel::downloadModel,
                onDeleteModel = viewModel::deleteModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            // General section
            SectionHeader("General")

            SettingsRow(
                label = "Reading Time",
                value = "${state.readingTimeMinutes} min"
            )

            SettingsRow(
                label = "Delivery Time",
                value = "${state.deliveryTimeHour}:00"
            )

            // Notifications toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.enableNotifications,
                    onCheckedChange = { viewModel.toggleNotifications(it) },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = PaperwalaColors.MastheadRed
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About section
            SectionHeader("About")

            SettingsRow(label = "Version", value = "1.0.0")
            SettingsRow(label = "Built with", value = "Kotlin Multiplatform")
        }
    }
}

@Composable
private fun AiSettingsSection(
    state: SettingsState,
    onToggleLocalLlm: (Boolean) -> Unit,
    onSelectModel: (LlmModel) -> Unit,
    onDownloadModel: () -> Unit,
    onDeleteModel: () -> Unit
) {
    SectionHeader("AI Summaries")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                AiStatusChip(status = state.aiStatus)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = PaperwalaColors.DividerColor)
            Spacer(modifier = Modifier.height(16.dp))

            // On-device AI toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "On-Device AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Summaries generated privately on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.enableLocalLlm,
                    onCheckedChange = onToggleLocalLlm,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = PaperwalaColors.MastheadRed
                    )
                )
            }

            // Model selection & management (shown when local LLM is enabled)
            if (state.enableLocalLlm) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = PaperwalaColors.DividerColor)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Choose Model",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Model picker
                LlmModel.entries.forEach { model ->
                    ModelOptionCard(
                        model = model,
                        isSelected = state.selectedModel == model,
                        onSelect = { onSelectModel(model) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Download / status for selected model
                if (state.isDownloading) {
                    Text(
                        text = "Downloading ${state.selectedModel.displayName}... ${(state.downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { state.downloadProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = PaperwalaColors.MastheadRed
                    )
                } else if (state.isModelDownloaded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${state.selectedModel.displayName} installed (${state.modelSizeMb} MB)",
                            style = MaterialTheme.typography.bodySmall,
                            color = PaperwalaColors.SportsGreen
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        OutlinedButton(
                            onClick = onDeleteModel,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PaperwalaColors.MastheadRed
                            )
                        ) {
                            Text("Delete", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                } else {
                    Button(
                        onClick = onDownloadModel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PaperwalaColors.MastheadRed
                        )
                    ) {
                        Text("Download ${state.selectedModel.displayName} (${state.selectedModel.sizeDescription})")
                    }
                }

                // Error message
                if (state.downloadError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.downloadError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelOptionCard(
    model: LlmModel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) PaperwalaColors.MastheadRed else PaperwalaColors.DividerColor

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = PaperwalaColors.MastheadRed
            ),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${model.sizeDescription} · ${model.speedDescription} · ${model.qualityDescription}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AiStatusChip(status: AiStatus) {
    val (label, color) = when (status) {
        AiStatus.LOCAL_LLM -> "On-Device" to PaperwalaColors.SportsGreen
        AiStatus.CLOUD -> "Cloud API" to PaperwalaColors.SectionBlue
        AiStatus.RULE_BASED -> "Basic" to PaperwalaColors.InkLightGray
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = PaperwalaColors.InkLightGray,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = PaperwalaColors.InkLightGray
        )
    }
}
