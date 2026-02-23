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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paperwala.presentation.theme.PaperwalaColors

@Composable
fun AiSummaryBadge(modifier: Modifier = Modifier) {
    Text(
        text = "AI",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = PaperwalaColors.SectionBlue,
        modifier = modifier
            .background(
                PaperwalaColors.SectionBlue.copy(alpha = 0.12f),
                RoundedCornerShape(3.dp)
            )
            .padding(horizontal = 5.dp, vertical = 1.dp)
    )
}
