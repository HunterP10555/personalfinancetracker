package com.example.personalfinancetracker

// Data class to define a Transaction object
data class Transaction(
    val type: String,         // Type of transaction: "Income" or "Expense"
    val amount: Double,       // Amount of money involved in the transaction
    val category: String,     // Category for the transaction, e.g., "Salary" or "Groceries"
    val date: String          // Date of the transaction, stored as a String in "yyyy-MM-dd" format
)
