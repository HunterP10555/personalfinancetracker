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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalFinanceTrackerApp()
        }
    }
}

@Composable
fun PersonalFinanceTrackerApp() {
    var transactions = remember { mutableStateListOf<Transaction>() }
    var balance by remember { mutableStateOf(0.0) }
    var showDialog by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf("Income") }

    // Move predefined categories to the main app level to persist across transactions
    val predefinedCategories = remember { mutableStateListOf("Salary", "Shopping", "Utilities", "Groceries") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Current Balance: $${"%.2f".format(balance)}",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Button(
                onClick = {
                    transactionType = "Income"
                    showDialog = true
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Add Income")
            }

            Button(
                onClick = {
                    transactionType = "Expense"
                    showDialog = true
                }
            ) {
                Text("Add Expense")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TransactionList(transactions)

        if (showDialog) {
            CustomTransactionDialog(
                transactionType = transactionType,
                predefinedCategories = predefinedCategories, // Pass categories list
                onAddTransaction = { amount, category ->
                    val newTransaction = addTransaction(transactionType, amount, category)
                    transactions.add(newTransaction)
                    balance += if (transactionType == "Income") amount else -amount
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}

// Updated CustomTransactionDialog to receive predefinedCategories as a parameter
@Composable
fun CustomTransactionDialog(
    transactionType: String,
    predefinedCategories: MutableList<String>,
    onAddTransaction: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showCustomCategoryDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Enter $transactionType Amount", style = MaterialTheme.typography.h6)

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Box to manage the dropdown field for category
                Box {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        readOnly = true
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        predefinedCategories.forEach { category ->
                            DropdownMenuItem(onClick = {
                                selectedCategory = category
                                expanded = false
                            }) {
                                Text(text = category)
                            }
                        }
                        DropdownMenuItem(onClick = {
                            showCustomCategoryDialog = true
                            expanded = false
                        }) {
                            Text(text = "Add New Category")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0.0 && selectedCategory.isNotBlank()) {
                                onAddTransaction(amount, selectedCategory)
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Add")
                    }
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    if (showCustomCategoryDialog) {
        CustomCategoryDialog(
            onAddCategory = { newCategory ->
                if (newCategory.isNotBlank()) {
                    predefinedCategories.add(newCategory)
                    selectedCategory = newCategory
                }
                showCustomCategoryDialog = false
                expanded = true // Reopen dropdown to show the new category
            },
            onDismiss = { showCustomCategoryDialog = false }
        )
    }
}

@Composable
fun CustomCategoryDialog(
    onAddCategory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var customCategory by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Enter New Category", style = MaterialTheme.typography.h6)

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customCategory,
                    onValueChange = { customCategory = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = {
                            onAddCategory(customCategory)
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Add Category")
                    }
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

fun addTransaction(type: String, amount: Double, category: String): Transaction {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    return Transaction(type, amount, category, date)
}

@Composable
fun TransactionList(transactions: List<Transaction>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(transactions) { transaction ->
            TransactionItem(transaction)
        }
    }
}

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
