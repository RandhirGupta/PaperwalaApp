package com.paperwala

import androidx.compose.runtime.Composable
import com.paperwala.presentation.navigation.AppNavigator
import com.paperwala.presentation.theme.PaperwalaTheme

@Composable
fun App() {
    PaperwalaTheme {
        AppNavigator()
    }
}
