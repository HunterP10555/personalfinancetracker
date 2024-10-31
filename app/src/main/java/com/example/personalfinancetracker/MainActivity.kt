package com.example.personalfinancetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

// Main activity to initialize the application content
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalFinanceTrackerApp() // Set the main composable
        }
    }
}

// Main composable function for the personal finance tracker app
@Composable
fun PersonalFinanceTrackerApp() {
    // Mutable list to store transactions
    var transactions = remember { mutableStateListOf<Transaction>() }
    var balance by remember { mutableStateOf(0.0) } // Holds the balance amount
    var showDialog by remember { mutableStateOf(false) } // Controls dialog visibility
    var transactionType by remember { mutableStateOf("Income") } // Stores type (Income/Expense)

    // Categories for transactions, retained at the app level
    val predefinedCategories = remember { mutableStateListOf("Salary", "Shopping", "Utilities", "Groceries") }

    // Main column layout for balance, buttons, and transaction list
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display the current balance
        Text(
            text = "Current Balance: $${"%.2f".format(balance)}",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Row for Add Income and Add Expense buttons
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Button(
                onClick = {
                    transactionType = "Income" // Set transaction type to Income
                    showDialog = true // Show the dialog to add transaction
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Add Income")
            }

            Button(
                onClick = {
                    transactionType = "Expense" // Set transaction type to Expense
                    showDialog = true // Show the dialog to add transaction
                }
            ) {
                Text("Add Expense")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of transactions
        TransactionList(transactions)

        // Show the dialog if showDialog is true
        if (showDialog) {
            CustomTransactionDialog(
                transactionType = transactionType,
                predefinedCategories = predefinedCategories, // Pass categories to the dialog
                onAddTransaction = { amount, category ->
                    // Add transaction to the list and update balance based on type
                    val newTransaction = addTransaction(transactionType, amount, category)
                    transactions.add(newTransaction)
                    balance += if (transactionType == "Income") amount else -amount
                    showDialog = false // Hide the dialog after adding
                },
                onDismiss = { showDialog = false } // Hide the dialog on dismissal
            )
        }
    }
}

// Dialog to add a new transaction with custom amount and category
@Composable
fun CustomTransactionDialog(
    transactionType: String,
    predefinedCategories: MutableList<String>,
    onAddTransaction: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") } // Holds the transaction amount
    var selectedCategory by remember { mutableStateOf("") } // Selected category for transaction
    var expanded by remember { mutableStateOf(false) } // Dropdown menu visibility
    var showCustomCategoryDialog by remember { mutableStateOf(false) } // Custom category dialog visibility

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title showing transaction type (Income/Expense)
                Text(text = "Enter $transactionType Amount", style = MaterialTheme.typography.h6)

                Spacer(modifier = Modifier.height(8.dp))

                // Text field to input the transaction amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category selection dropdown
                Box {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }, // Show dropdown on click
                        readOnly = true
                    )

                    // Dropdown menu to choose category
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        predefinedCategories.forEach { category ->
                            DropdownMenuItem(onClick = {
                                selectedCategory = category // Set selected category
                                expanded = false // Close dropdown
                            }) {
                                Text(text = category)
                            }
                        }
                        DropdownMenuItem(onClick = {
                            showCustomCategoryDialog = true // Show custom category dialog
                            expanded = false // Close dropdown
                        }) {
                            Text(text = "Add New Category")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Row with Add and Cancel buttons
                Row {
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0.0 && selectedCategory.isNotBlank()) {
                                onAddTransaction(amount, selectedCategory) // Add transaction
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Add")
                    }
                    Button(onClick = onDismiss) { // Close dialog on cancel
                        Text("Cancel")
                    }
                }
            }
        }
    }

    // Show custom category dialog if showCustomCategoryDialog is true
    if (showCustomCategoryDialog) {
        CustomCategoryDialog(
            onAddCategory = { newCategory ->
                if (newCategory.isNotBlank()) {
                    predefinedCategories.add(newCategory) // Add new category to list
                    selectedCategory = newCategory // Set the new category as selected
                }
                showCustomCategoryDialog = false // Hide custom category dialog
                expanded = true // Reopen dropdown to show the new category
            },
            onDismiss = { showCustomCategoryDialog = false } // Hide dialog on cancel
        )
    }
}

// Dialog to add a custom category for transactions
@Composable
fun CustomCategoryDialog(
    onAddCategory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var customCategory by remember { mutableStateOf("") } // Input for custom category name

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title for dialog
                Text(text = "Enter New Category", style = MaterialTheme.typography.h6)

                Spacer(modifier = Modifier.height(8.dp))

                // Text field to input custom category name
                OutlinedTextField(
                    value = customCategory,
                    onValueChange = { customCategory = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Row with Add Category and Cancel buttons
                Row {
                    Button(
                        onClick = {
                            onAddCategory(customCategory) // Add new category to the list
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Add Category")
                    }
                    Button(onClick = onDismiss) { // Close dialog on cancel
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

// Function to create a transaction with specified details
fun addTransaction(type: String, amount: Double, category: String): Transaction {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) // Get current date
    return Transaction(type, amount, category, date) // Return new transaction object
}

// Composable to display the list of transactions
@Composable
fun TransactionList(transactions: List<Transaction>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(transactions) { transaction ->
            TransactionItem(transaction) // Display each transaction item
        }
    }
}

// Composable to display a single transaction item
@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Type: ${transaction.type}", style = MaterialTheme.typography.body1)
            Text(text = "Amount: $${"%.2f".format(transaction.amount)}", style = MaterialTheme.typography.body1)
            Text(text = "Category: ${transaction.category}", style = MaterialTheme.typography.body2)
            Text(text = "Date: ${transaction.date}", style = MaterialTheme.typography.body2)
        }
    }
}
