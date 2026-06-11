package com.dishly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dishly.app.ui.components.DishlyLogo
import com.dishly.app.ui.components.DishlyOutlinedButton
import com.dishly.app.ui.components.DishlyPrimaryButton
import com.dishly.app.ui.components.DishlyTextField
import com.dishly.app.ui.components.DishlyWordmark
import com.dishly.app.ui.components.LogoVariant
import com.dishly.app.ui.theme.*
import com.dishly.app.viewmodel.AuthViewModel

@Composable
fun SignInScreen(
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigateToMain) {
        if (state.navigateToMain) {
            viewModel.resetNavigation()
            onSignIn()
        }
    }

    AuthScaffold(showWelcome = true, onBack = onBack, scrollable = false) {
        Text("Sign In", color = PurplePrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        DishlyTextField(state.username, viewModel::onUsernameChange, "Username")
        Spacer(Modifier.height(14.dp))
        DishlyTextField(state.password, viewModel::onPasswordChange, "Password", isPassword = true)
        var rememberMe by rememberSaveable { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Magenta,
                    uncheckedColor = PurplePrimary
                )
            )
            Text("Remember me", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            Text("Forgot Your Password?", color = TextGray, fontSize = 12.sp)
        }
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        DishlyPrimaryButton(
            text = "Sign In",
            onClick = viewModel::signIn,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            backgroundColor = PurplePrimary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
            Text("Don't have an account?", color = TextGray, modifier = Modifier.padding(horizontal = 12.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
        }
        DishlyOutlinedButton(
            text = "Sign Up",
            onClick = onSignUp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
    }
}

@Composable
fun SignUpScreen(
    onBack: () -> Unit,
    onRegistered: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigateToMain) {
        if (state.navigateToMain) {
            viewModel.resetNavigation()
            onRegistered()
        }
    }

    AuthScaffold(showWelcome = false, onBack = onBack, scrollable = true) {
        Text("Sign Up", color = PurplePrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        DishlyTextField(state.username, viewModel::onUsernameChange, "Username")
        Spacer(Modifier.height(14.dp))
        DishlyTextField(state.password, viewModel::onPasswordChange, "Password", isPassword = true)
        Spacer(Modifier.height(14.dp))
        DishlyTextField(state.confirmPassword, viewModel::onConfirmPasswordChange, "Confirm Password", isPassword = true)
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        DishlyPrimaryButton(
            text = "Sign Up",
            onClick = viewModel::signUp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 26.dp),
            backgroundColor = PurplePrimary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account?", color = TextGray)
            Text(
                " Sign In",
                color = Magenta,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onBack)
            )
        }
    }
}

@Composable
private fun AuthScaffold(
    showWelcome: Boolean,
    onBack: (() -> Unit)?,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PurplePrimary, Magenta)))
            .statusBarsPadding()
            .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = White)
            }
        } else {
            Spacer(Modifier.height(24.dp))
        }
        if (showWelcome) {
            Text(
                "Welcome to",
                color = White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Medium
            )
            DishlyWordmark(
                pink = true,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(210.dp)
                    .height(80.dp)
            )
        }
        DishlyLogo(
            variant = LogoVariant.White,
            modifier = Modifier
                .padding(top = 8.dp)
                .size(if (showWelcome) 64.dp else 100.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .background(White, RoundedCornerShape(28.dp))
                .padding(24.dp),
            content = content
        )
    }
}
