package com.example.personalfinancetracker

data class Transaction(
    val type: String,
    val amount: Double,
    val category: String,
    val date: String
)