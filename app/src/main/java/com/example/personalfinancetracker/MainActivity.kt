package com.example.personalfinancetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

// Main app composable function
@Composable
fun PersonalFinanceTrackerApp() {
    // A list to store transactions, using mutableStateListOf to observe changes
    var transactions = remember { mutableStateListOf<Transaction>() }
    var balance by remember { mutableStateOf(0.0) } // Track the balance

    // State to control visibility of the dialog
    var showDialog by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf("Income") }

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

        // Buttons for adding income and expense
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Button(
                onClick = {
                    transactionType = "Income"
                    showDialog = true // Show the custom amount dialog for Income
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Add Income")
            }

            Button(
                onClick = {
                    transactionType = "Expense"
                    showDialog = true // Show the custom amount dialog for Expense
                }
            ) {
                Text("Add Expense")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display list of transactions
        TransactionList(transactions)

        // Show dialog for custom amount
        if (showDialog) {
            CustomAmountDialog(
                transactionType = transactionType,
                onAddTransaction = { amount ->
                    val newTransaction = addTransaction(transactionType, amount)
                    transactions.add(newTransaction)
                    balance += if (transactionType == "Income") amount else -amount // Update balance based on type
                    showDialog = false // Close dialog
                },
                onDismiss = {
                    showDialog = false // Close dialog without adding transaction
                }
            )
        }
    }
}

// Custom dialog to enter a custom amount
@Composable
fun CustomAmountDialog(
    transactionType: String,
    onAddTransaction: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }

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

                // Input field for amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0.0) {
                                onAddTransaction(amount)
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
}

// Function to create a new transaction with a specified amount
fun addTransaction(type: String, amount: Double): Transaction {
    val category = if (type == "Income") "Salary" else "Shopping" // Example categories
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    return Transaction(type, amount, category, date)
}

// List to display all transactions using LazyColumn
@Composable
fun TransactionList(transactions: List<Transaction>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(transactions) { transaction ->
            TransactionItem(transaction)
        }
    }
}

// Single transaction item in the list
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