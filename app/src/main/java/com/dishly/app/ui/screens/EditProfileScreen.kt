package com.dishly.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dishly.app.ui.components.DishlyPrimaryButton
import com.dishly.app.ui.components.DishlyTextField
import com.dishly.app.ui.components.DishlyTopBar
import com.dishly.app.ui.theme.*
import com.dishly.app.viewmodel.EditProfileViewModel

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        DishlyTopBar(title = "Edit Profile", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Icon(
                    Icons.Default.Person,
                    null,
                    tint = PurplePrimary,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(ChipBg)
                        .padding(26.dp)
                )
                Icon(
                    Icons.Default.CameraAlt,
                    null,
                    tint = White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary)
                        .padding(7.dp)
                )
            }
            Text("Edit Profile Picture", color = PurplePrimary, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))

            DishlyTextField(state.name, viewModel::onNameChange, "Name", modifier = Modifier.padding(top = 20.dp))
            DishlyTextField(state.username, viewModel::onUsernameChange, "Username", modifier = Modifier.padding(top = 12.dp))

            DishlyPrimaryButton(
                text = "Save",
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
            )
        }
    }
}
