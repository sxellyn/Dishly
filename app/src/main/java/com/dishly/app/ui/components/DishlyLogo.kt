package com.dishly.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.dishly.app.R

enum class LogoVariant { White, Pink, Purple }

@Composable
fun DishlyLogo(modifier: Modifier = Modifier, variant: LogoVariant = LogoVariant.White) {
    val res = when (variant) {
        LogoVariant.White -> R.drawable.logo_white
        LogoVariant.Pink -> R.drawable.logo_pink
        LogoVariant.Purple -> R.drawable.logo_purple
    }
    Image(
        painter = painterResource(res),
        contentDescription = "Dishly",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun DishlyWordmark(modifier: Modifier = Modifier, pink: Boolean = true) {
    Image(
        painter = painterResource(if (pink) R.drawable.wordmark_pink else R.drawable.wordmark_white),
        contentDescription = "Dishly",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
