package com.dishly.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dishly.app.model.Comment
import com.dishly.app.ui.components.DishlyTopBar
import com.dishly.app.ui.components.SectionTitle
import com.dishly.app.ui.theme.*
import com.dishly.app.viewmodel.RecipeDetailViewModel

@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    onBack: () -> Unit,
    viewModel: RecipeDetailViewModel = viewModel(factory = RecipeDetailViewModel.Factory(recipeId))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(recipeId) { viewModel.load() }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val recipe = state.recipe
    if (recipe == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Recipe not found")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        DishlyTopBar(onBack = onBack)
        Box(modifier = Modifier.height(240.dp)) {
            Image(
                painter = painterResource(recipe.imageRes),
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = viewModel::toggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .background(PurplePrimary, CircleShape)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    "Favorite",
                    tint = if (recipe.isFavorite) Magenta else White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    recipe.title,
                    color = PurplePrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, recipe.title)
                            putExtra(Intent.EXTRA_TEXT, "${recipe.title}\n\n${recipe.description}")
                        }
                        context.startActivity(Intent.createChooser(share, "Share recipe"))
                    },
                    modifier = Modifier
                        .background(PurplePrimary, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Share, "Share recipe", tint = White, modifier = Modifier.size(20.dp))
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Icon(Icons.Default.Favorite, null, tint = Magenta, modifier = Modifier.size(16.dp))
                Text(
                    " ${recipe.rating} favorites",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }
            Text(recipe.description, modifier = Modifier.padding(top = 12.dp), fontSize = 14.sp)

            SectionTitle("Ingredients")
            recipe.ingredients.forEach { ing ->
                Text("•  $ing", modifier = Modifier.padding(vertical = 4.dp), fontSize = 14.sp)
            }

            SectionTitle("Preparation steps")
            Spacer(Modifier.height(6.dp))
            recipe.steps.forEachIndexed { i, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgLight)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${i + 1}",
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(step, fontSize = 14.sp, modifier = Modifier.weight(1f))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 22.dp), color = DividerColor)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = Magenta, modifier = Modifier.size(22.dp))
                SectionTitle("Comments")
                Text(" (${recipe.comments.size})", color = TextGray, fontSize = 14.sp)
            }

            if (recipe.comments.isEmpty()) {
                Text(
                    "Be the first to comment on this recipe!",
                    color = TextGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            recipe.comments.forEach { comment ->
                CommentItem(comment)
            }

            Row(
                modifier = Modifier.padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.commentText,
                    onValueChange = viewModel::onCommentTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Leave your comment about this recipe…") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Magenta,
                        unfocusedBorderColor = PurplePrimary,
                        cursorColor = PurplePrimary
                    )
                )
                IconButton(
                    onClick = viewModel::sendComment,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .background(PurplePrimary, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = White)
                }
            }
        }
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    Row(modifier = Modifier.padding(vertical = 10.dp)) {
        Icon(
            Icons.Default.Person,
            null,
            tint = PurplePrimary,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ChipBg)
                .padding(9.dp)
        )
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(comment.authorName, color = PurplePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(comment.text, fontSize = 13.sp)
            Text(comment.timeLabel, color = TextGray, fontSize = 11.sp)
        }
    }
}
