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
package com.paperwala.presentation.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.paperwala.data.repository.UserRepository
import com.paperwala.presentation.screens.morningedition.MorningEditionScreen
import com.paperwala.presentation.screens.onboarding.OnboardingScreen
import com.paperwala.presentation.theme.PaperwalaColors
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

class SplashScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userRepository = koinInject<UserRepository>()

        // Animations
        val titleAlpha = remember { Animatable(0f) }
        val titleScale = remember { Animatable(0.8f) }
        val subtitleAlpha = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            // Animate title in
            titleAlpha.animateTo(1f, animationSpec = tween(800, easing = LinearEasing))
            titleScale.animateTo(1f, animationSpec = tween(800))

            // Animate subtitle
            subtitleAlpha.animateTo(1f, animationSpec = tween(600))

            delay(500)

            // Navigate based on onboarding state
            val hasOnboarded = userRepository.hasCompletedOnboarding()
            if (hasOnboarded) {
                navigator.replaceAll(MorningEditionScreen())
            } else {
                navigator.replaceAll(OnboardingScreen())
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PaperwalaColors.PaperCream),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Masthead title with animation
                Text(
                    text = "PAPERWALA",
                    style = MaterialTheme.typography.displayLarge,
                    color = PaperwalaColors.MastheadRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = titleAlpha.value
                        scaleX = titleScale.value
                        scaleY = titleScale.value
                    }
                )

                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .graphicsLayer { alpha = titleAlpha.value },
                    thickness = 2.dp,
                    color = PaperwalaColors.MastheadRed
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your morning paper, delivered.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PaperwalaColors.InkLightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = subtitleAlpha.value
                    }
                )
            }
        }
    }
}
