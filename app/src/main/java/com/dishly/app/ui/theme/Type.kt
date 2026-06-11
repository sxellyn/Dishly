package com.dishly.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.dishly.app.R

val AfacadFlux = FontFamily(Font(R.font.afacad_flux))

private val base = Typography()

val DishlyTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = AfacadFlux),
    displayMedium = base.displayMedium.copy(fontFamily = AfacadFlux),
    displaySmall = base.displaySmall.copy(fontFamily = AfacadFlux),
    headlineLarge = base.headlineLarge.copy(fontFamily = AfacadFlux),
    headlineMedium = base.headlineMedium.copy(fontFamily = AfacadFlux),
    headlineSmall = base.headlineSmall.copy(fontFamily = AfacadFlux),
    titleLarge = base.titleLarge.copy(fontFamily = AfacadFlux),
    titleMedium = base.titleMedium.copy(fontFamily = AfacadFlux),
    titleSmall = base.titleSmall.copy(fontFamily = AfacadFlux),
    bodyLarge = base.bodyLarge.copy(fontFamily = AfacadFlux),
    bodyMedium = base.bodyMedium.copy(fontFamily = AfacadFlux),
    bodySmall = base.bodySmall.copy(fontFamily = AfacadFlux),
    labelLarge = base.labelLarge.copy(fontFamily = AfacadFlux),
    labelMedium = base.labelMedium.copy(fontFamily = AfacadFlux),
    labelSmall = base.labelSmall.copy(fontFamily = AfacadFlux)
)
