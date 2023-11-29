package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.example.myapplication.ui.theme.MyApplicationTheme

// Provide your Sudoku puzzle as an input parameter to SudokuGame
var board = Array(9) { IntArray(9) }

class SudokuDisplay : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {

                var index = 0
                for (i in 0..8) {
                    for (j in 0..8) {
                        board[i][j] = numbers[index]
                        index++
                    }
                }

                SudokuGame()
            }
        }
    }
}

@Composable
fun SudokuGame() {

    val context = LocalContext.current

    // Function to handle text changes in SudokuCells
    val onCellTextChange: (Int, String) -> Unit = { index, newText ->
        val row = index / 9
        val col = index % 9
        val newValue = newText.toIntOrNull() ?: 0
        board[row][col] = newValue
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the provided Sudoku puzzle
        for (row in board.indices) {
            SudokuRow(row = board[row], onCellTextChange = onCellTextChange)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Update the board using SudokuSolver.fillSudoku
                SudokuSolver.fillSudoku(board)


                Intent(context, Solution::class.java).also {
                    startActivity(context, it, null)
                }

            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Solve")
        }
    }
}


@Composable
fun SudokuRow(row: IntArray, onCellTextChange: (Int, String) -> Unit) {
    Row(
        modifier = Modifier.padding(1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (col in row.indices) {
            SudokuCell(row[col], onTextChange = onCellTextChange)
        }
    }
}

@Composable
fun SudokuCell(value: Int, onTextChange: (Int, String) -> Unit) {
    val density = LocalDensity.current.density
    var text by remember { mutableStateOf(if (value == 0) "" else value.toString()) }
    val focusManager = LocalFocusManager.current

    val textColor = Color.Black

    val borderModifier = Modifier.border(
        width = 1.dp,
        color = Color.Gray
    )

    BasicTextField(
        value = text,
        onValueChange = {
            if (it.isEmpty() || it.toIntOrNull() != null) {
                text = it

                // Update the board array when the user enters a valid number
                onTextChange(value, it) // Invoke the callback with the updated value and text

                println("Board:")
                board.forEach { row ->
                    println(row.joinToString())
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Number
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (text.isEmpty()) {
                    text = if (value == 0) "" else value.toString()
                } else {
                    val newValue = text.toInt()
                    text = if (newValue in 0..9) {
                        newValue.toString()
                    } else {
                        if (value == 0) "" else value.toString()
                    }
                    focusManager.clearFocus()
                }
            }
        ),
        textStyle = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = textColor
        ),
        modifier = Modifier
            .background(Color.White)
            .padding(1.dp)
            .width(15.dp * density)
            .height(15.dp * density)
            .then(borderModifier)
            .clickable {
                text = ""
            }
    )
}


