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
package com.paperwala.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paperwala.presentation.theme.PaperwalaColors

@Composable
fun StreakBadge(
    currentStreak: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentStreak <= 0) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PaperwalaColors.StreakOrange.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .semantics { contentDescription = "$currentStreak day reading streak" }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "\uD83D\uDD25",
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = " $currentStreak",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = PaperwalaColors.StreakOrange
        )
    }
}
