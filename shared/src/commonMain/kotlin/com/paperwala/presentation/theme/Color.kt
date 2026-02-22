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
package com.paperwala.presentation.theme

import androidx.compose.ui.graphics.Color

// Newspaper-inspired color palette
object PaperwalaColors {
    // Paper tones
    val PaperWhite = Color(0xFFF5F0E8)
    val PaperCream = Color(0xFFFAF6EE)
    val InkBlack = Color(0xFF1A1A1A)
    val InkDarkGray = Color(0xFF2D2D2D)
    val InkGray = Color(0xFF4A4A4A)
    val InkLightGray = Color(0xFF8A8A8A)

    // Accent colors
    val MastheadRed = Color(0xFFC41E3A)
    val StreakOrange = Color(0xFFE65100)
    val SectionBlue = Color(0xFF1565C0)
    val LinkBlue = Color(0xFF1976D2)

    // Category colors
    val PoliticsRed = Color(0xFFB71C1C)
    val TechBlue = Color(0xFF0D47A1)
    val SportsGreen = Color(0xFF1B5E20)
    val BusinessGold = Color(0xFFE65100)
    val EntertainmentPurple = Color(0xFF4A148C)
    val ScienceTeal = Color(0xFF006064)
    val HealthPink = Color(0xFF880E4F)
    val WorldNavy = Color(0xFF1A237E)
    val IndiaOrange = Color(0xFFFF6F00)
    val OpinionBrown = Color(0xFF3E2723)
    val EnvironmentGreen = Color(0xFF2E7D32)
    val EducationIndigo = Color(0xFF283593)

    // Surface colors
    val SurfaceLight = Color(0xFFFFFBF5)
    val SurfaceDark = Color(0xFF121212)
    val CardBackground = Color(0xFFFFFFFF)
    val DividerColor = Color(0xFFD7CFC4)

    // Sepia reading mode
    val SepiaBackground = Color(0xFFF4ECD8)
    val SepiaText = Color(0xFF5B4636)
}

fun categoryColor(category: String): Color {
    return when (category.uppercase()) {
        "POLITICS" -> PaperwalaColors.PoliticsRed
        "TECHNOLOGY" -> PaperwalaColors.TechBlue
        "SPORTS" -> PaperwalaColors.SportsGreen
        "BUSINESS" -> PaperwalaColors.BusinessGold
        "ENTERTAINMENT" -> PaperwalaColors.EntertainmentPurple
        "SCIENCE" -> PaperwalaColors.ScienceTeal
        "HEALTH" -> PaperwalaColors.HealthPink
        "WORLD_NEWS" -> PaperwalaColors.WorldNavy
        "INDIA" -> PaperwalaColors.IndiaOrange
        "OPINION" -> PaperwalaColors.OpinionBrown
        "ENVIRONMENT" -> PaperwalaColors.EnvironmentGreen
        "EDUCATION" -> PaperwalaColors.EducationIndigo
        else -> PaperwalaColors.InkGray
    }
}
