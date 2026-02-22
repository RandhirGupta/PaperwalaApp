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
package com.paperwala.presentation.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/** Newspaper section unfold: 3D rotation from top edge with elastic bounce */
@Composable
fun Modifier.newspaperUnfold(
    visible: Boolean,
    index: Int = 0,
    staggerDelayMs: Long = 120L
): Modifier {
    var triggered by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible && !triggered) {
            delay(index * staggerDelayMs)
            triggered = true
        }
    }

    val rotationX by animateFloatAsState(
        targetValue = if (triggered) 0f else -90f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessLow
        )
    )
    val alpha by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(durationMillis = 350)
    )
    val scaleY by animateFloatAsState(
        targetValue = if (triggered) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    return this.graphicsLayer {
        this.rotationX = rotationX
        this.alpha = alpha
        this.scaleY = scaleY
        this.cameraDistance = 14f * density
        this.transformOrigin = TransformOrigin(0.5f, 0f)
    }
}

/** Fade-slide-up entrance for individual article cards within a section */
@Composable
fun Modifier.articleEntrance(
    visible: Boolean,
    index: Int = 0,
    staggerDelayMs: Long = 80L
): Modifier {
    var triggered by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible && !triggered) {
            delay(index * staggerDelayMs)
            triggered = true
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(300)
    )
    val translationY by animateFloatAsState(
        targetValue = if (triggered) 0f else 30f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
    }
}

/** Masthead slide-down entrance */
@Composable
fun Modifier.mastheadEntrance(): Modifier {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(600, easing = EaseOutCubic))
    }

    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * -40f
    }
}

/** Hero article scale-up entrance with delay */
@Composable
fun Modifier.heroEntrance(delayMs: Int = 300): Modifier {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        progress.animateTo(1f, animationSpec = tween(500, easing = EaseOutCubic))
    }

    return this.graphicsLayer {
        alpha = progress.value
        scaleX = 0.95f + (0.05f * progress.value)
        scaleY = 0.95f + (0.05f * progress.value)
    }
}
