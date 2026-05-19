package com.example.app_doublekrestaurant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.app_doublekrestaurant.data.model.CartItem
import com.example.app_doublekrestaurant.data.model.FoodItem

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey
    val foodItemId: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val categoryId: String,
    val categoryName: String,
    val isAvailable: Boolean,
    val isFeatured: Boolean,
    val isSpicy: Boolean,
    val isVegetarian: Boolean,
    val rating: Double,
    val reviewCount: Int,
    val tags: String, // Lưu trữ dưới dạng chuỗi ngăn cách bằng dấu phẩy
    val createdAt: Long,
    val quantity: Int,
    val note: String
)

fun CartEntity.toCartItem(): CartItem = CartItem(
    foodItem = FoodItem(
        id = foodItemId,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        categoryId = categoryId,
        categoryName = categoryName,
        isAvailable = isAvailable,
        isFeatured = isFeatured,
        isSpicy = isSpicy,
        isVegetarian = isVegetarian,
        rating = rating,
        reviewCount = reviewCount,
        tags = tags.split(",").filter { it.isNotEmpty() },
        createdAt = createdAt
    ),
    quantity = quantity,
    note = note
)

fun CartItem.toCartEntity(): CartEntity = CartEntity(
    foodItemId = foodItem.id,
    name = foodItem.name,
    description = foodItem.description,
    price = foodItem.price,
    imageUrl = foodItem.imageUrl,
    categoryId = foodItem.categoryId,
    categoryName = foodItem.categoryName,
    isAvailable = foodItem.isAvailable,
    isFeatured = foodItem.isFeatured,
    isSpicy = foodItem.isSpicy,
    isVegetarian = foodItem.isVegetarian,
    rating = foodItem.rating,
    reviewCount = foodItem.reviewCount,
    tags = foodItem.tags.joinToString(","),
    createdAt = foodItem.createdAt,
    quantity = quantity,
    note = note
)

fun FoodItem.toCartEntity(quantity: Int = 1, note: String = ""): CartEntity = CartEntity(
    foodItemId = id,
    name = name,
    description = description,
    price = price,
    imageUrl = imageUrl,
    categoryId = categoryId,
    categoryName = categoryName,
    isAvailable = isAvailable,
    isFeatured = isFeatured,
    isSpicy = isSpicy,
    isVegetarian = isVegetarian,
    rating = rating,
    reviewCount = reviewCount,
    tags = tags.joinToString(","),
    createdAt = createdAt,
    quantity = quantity,
    note = note
)
