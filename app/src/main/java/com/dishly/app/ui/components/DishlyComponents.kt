package com.dishly.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dishly.app.model.Recipe
import com.dishly.app.ui.theme.*

/**
 * Top bar built with Material3 [CenterAlignedTopAppBar], keeping the Dishly look:
 * purple background, rounded bottom corners, white logo + wordmark (or a title) and an
 * optional white back button on the left.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishlyTopBar(
    title: String? = null,
    onBack: (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp)),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = PurplePrimary,
            titleContentColor = White,
            navigationIconContentColor = White,
            actionIconContentColor = White
        ),
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                }
            }
        },
        title = {
            if (title != null) {
                Text(text = title, color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    DishlyLogo(variant = LogoVariant.White, modifier = Modifier.size(40.dp))
                    DishlyWordmark(
                        pink = true,
                        modifier = Modifier
                            .width(120.dp)
                            .height(44.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun DishlyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Magenta,
    contentColor: Color = White
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Composable
fun DishlyOutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, PurplePrimary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PurplePrimary)
    ) {
        Text(text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DishlyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Magenta,
            unfocusedBorderColor = PurplePrimary,
            focusedLabelColor = Magenta,
            unfocusedLabelColor = PurplePrimary,
            cursorColor = PurplePrimary
        ),
        singleLine = true,
        visualTransformation = if (isPassword) {
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        }
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(text = text, color = Magenta, fontSize = 22.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun SectionSubtitle(text: String) {
    Text(text = text, color = PurplePrimary, fontSize = 13.sp)
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(190.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.foundation.Image(
                painter = painterResource(recipe.imageRes),
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .background(PurplePrimary, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Favorite, null, tint = White, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(recipe.rating.toString(), color = White, fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.75f)))
                    )
            )
            Text(
                text = recipe.title,
                color = White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun RecipeListItem(recipe: Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(recipe.imageRes),
                contentDescription = recipe.title,
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        recipe.title,
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        recipe.description,
                        color = TextDark,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Magenta,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Cook!", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
