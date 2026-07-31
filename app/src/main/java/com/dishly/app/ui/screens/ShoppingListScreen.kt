package com.dishly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dishly.app.api.MealService
import com.dishly.app.model.ShoppingListItem
import com.dishly.app.ui.components.DishlyTopBar
import com.dishly.app.ui.components.SectionTitle
import com.dishly.app.ui.theme.BgLight
import com.dishly.app.ui.theme.Magenta
import com.dishly.app.ui.theme.PurplePrimary
import com.dishly.app.ui.theme.TextGray
import com.dishly.app.ui.theme.White
import com.dishly.app.viewmodel.ShoppingListViewModel

@Composable
fun ShoppingListScreen(
    recipeId: Int,
    mealService: MealService,
    onBack: () -> Unit,
    viewModel: ShoppingListViewModel = viewModel(
        factory = ShoppingListViewModel.Factory(recipeId, mealService)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recipeId) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        DishlyTopBar(title = "Shopping list", onBack = onBack)

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Magenta)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = Magenta, fontSize = 14.sp)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Magenta,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.size(10.dp))
                        Column {
                            SectionTitle("For this recipe")
                            Text(
                                state.recipeTitle,
                                color = PurplePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (state.pantryCount == 0) {
                            "Based on your pantry (empty) · ${state.items.size} to buy"
                        } else {
                            "You already have ${state.coveredCount} · ${state.items.size} to buy"
                        },
                        color = TextGray,
                        fontSize = 13.sp
                    )

                    state.message?.let { tip ->
                        Spacer(Modifier.height(12.dp))
                        Text(
                            tip,
                            color = PurplePrimary,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BgLight)
                                .padding(12.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    if (state.isEmpty) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Magenta,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Nothing to buy!",
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(state.items, key = { it.id }) { item ->
                                ShoppingListRow(
                                    item = item,
                                    onToggle = { viewModel.toggleChecked(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingListRow(
    item: ShoppingListItem,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgLight)
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.checked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = Magenta,
                uncheckedColor = PurplePrimary
            )
        )
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = item.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (item.checked) TextGray else PurplePrimary,
                textDecoration = if (item.checked) TextDecoration.LineThrough else null
            )
            if (item.measure.isNotBlank()) {
                Text(
                    text = item.measure,
                    fontSize = 13.sp,
                    color = TextGray,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else null
                )
            }
        }
    }
}
