package com.paperwala.presentation.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.paperwala.presentation.screens.splash.SplashScreen

@Composable
fun AppNavigator() {
    Navigator(SplashScreen()) { navigator ->
        SlideTransition(navigator)
    }
}
