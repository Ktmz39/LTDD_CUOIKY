package com.example.app_doublekrestaurant.data.model

data class CartItem(
    val foodItem: FoodItem = FoodItem(),
    val quantity: Int = 1,
    val note: String = ""
) {
    val totalPrice: Double get() = foodItem.price * quantity
}
