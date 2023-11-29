package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

class Solution : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {

                SudokuGame1()
            }
        }
    }
}


@Composable
fun SudokuGame1() {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Display the provided Sudoku puzzle
        for (row in board.indices) {
            SudokuRow1(board[row])
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
fun SudokuRow1(row: IntArray) {
    Row(
        modifier = Modifier.padding(1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (col in row.indices) {
            SudokuCell1(row[col])
        }
    }
}

@Composable
fun SudokuCell1(value: Int) {
    val density = LocalDensity.current.density
    val textColor = Color.Blue
    val borderModifier = Modifier.border(
        width = 1.dp,
        color = Color.Gray
    )

    Box(
        modifier = Modifier
            .background(Color.White)
            .padding(1.dp)
            .width(15.dp * density)
            .height(15.dp * density)
            .then(borderModifier)
    ) {
        Text(
            text = if (value != 0) value.toString() else "",
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = textColor
            )
        )
    }
}

