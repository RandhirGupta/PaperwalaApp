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

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import paperwala.shared.generated.resources.Res
import paperwala.shared.generated.resources.lora_italic_variable
import paperwala.shared.generated.resources.lora_variable
import paperwala.shared.generated.resources.playfair_display_variable

@Composable
fun PlayfairDisplayFontFamily(): FontFamily = FontFamily(
    Font(Res.font.playfair_display_variable, FontWeight.Normal),
    Font(Res.font.playfair_display_variable, FontWeight.Medium),
    Font(Res.font.playfair_display_variable, FontWeight.SemiBold),
    Font(Res.font.playfair_display_variable, FontWeight.Bold),
    Font(Res.font.playfair_display_variable, FontWeight.ExtraBold),
    Font(Res.font.playfair_display_variable, FontWeight.Black),
)

@Composable
fun LoraFontFamily(): FontFamily = FontFamily(
    Font(Res.font.lora_variable, FontWeight.Normal),
    Font(Res.font.lora_variable, FontWeight.Medium),
    Font(Res.font.lora_variable, FontWeight.SemiBold),
    Font(Res.font.lora_variable, FontWeight.Bold),
    Font(Res.font.lora_italic_variable, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.lora_italic_variable, FontWeight.Medium, FontStyle.Italic),
    Font(Res.font.lora_italic_variable, FontWeight.Bold, FontStyle.Italic),
)
