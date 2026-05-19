package com.example.app_doublekrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.app_doublekrestaurant.data.model.FoodItem

@Composable
fun FoodItemCard(foodItem: FoodItem, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color(0xFFF0E6E6)),
                contentAlignment = Alignment.Center
            ) {
                if (foodItem.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = foodItem.imageUrl,
                        contentDescription = foodItem.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = Color(0xFFAC2D00).copy(0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = foodItem.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "${foodItem.price} VND", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onAddClick) {
                    Text("Thêm vào giỏ")
                }
            }
        }
    }
}
