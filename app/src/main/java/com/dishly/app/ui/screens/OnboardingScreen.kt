package com.dishly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.dishly.app.ui.components.DishlyLogo
import com.dishly.app.ui.components.DishlyPrimaryButton
import com.dishly.app.ui.components.LogoVariant
import com.dishly.app.ui.theme.*

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to PurplePrimary,
                    0.65f to PurplePrimary,
                    1.0f to Magenta
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        DishlyLogo(
            variant = LogoVariant.White,
            modifier = Modifier.size(400.dp)
        )
        Spacer(Modifier.weight(1f))
        Text(
            "Let's\nCook!",
            color = White,
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 50.sp,
            textAlign = TextAlign.Center
        )
        Text(
            "Cook smarter with what you have.",
            color = White.copy(0.9f),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )
        DishlyPrimaryButton(
            text = "Start cooking!",
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 48.dp),
            backgroundColor = White,
            contentColor = PurplePrimary
        )
    }
}
