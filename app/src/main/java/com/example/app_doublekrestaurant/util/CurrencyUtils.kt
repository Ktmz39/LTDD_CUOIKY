package com.example.app_doublekrestaurant.util

import java.text.NumberFormat
import java.util.Locale

fun formatVnd(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(amount.toLong())} đ"
}
