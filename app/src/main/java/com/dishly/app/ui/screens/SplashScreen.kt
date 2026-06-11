package com.dishly.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import com.dishly.app.ui.components.DishlyLogo
import com.dishly.app.ui.components.DishlyWordmark
import com.dishly.app.ui.components.LogoVariant
import com.dishly.app.ui.theme.LightPink
import com.dishly.app.ui.theme.Magenta
import com.dishly.app.ui.theme.PurplePrimary
import com.dishly.app.ui.theme.White
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigate: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onNavigate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurplePrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DishlyLogo(
                variant = LogoVariant.White,
                modifier = Modifier.size(260.dp)
            )
            DishlyWordmark(
                pink = true,
                modifier = Modifier
                    .width(340.dp)
                    .height(170.dp)
            )
            Spacer(Modifier.height(48.dp))
            LoadingDots()
        }
    }
}

@Composable
private fun LoadingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(White, LightPink, Magenta).forEachIndexed { index, color ->
            val alpha by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
