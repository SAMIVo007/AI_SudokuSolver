package com.example.myapplication

class SudokuSolver {
    companion object {
        private const val SIZE = 9
        private const val EMPTY_CELL = 0

        fun fillSudoku(grid: Array<IntArray>): Boolean {
            for (row in 0 until SIZE) {
                for (col in 0 until SIZE) {
                    if (grid[row][col] == EMPTY_CELL) {
                        for (num in 1..SIZE) {
                            if (isValidMove(grid, row, col, num)) {
                                grid[row][col] = num
                                if (fillSudoku(grid)) {
                                    return true
                                }
                                grid[row][col] = EMPTY_CELL
                            }
                        }
                        return false
                    }
                }
            }
            return true
        }

        private fun isValidMove(grid: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
            return !isInRow(grid, row, num) &&
                    !isInColumn(grid, col, num) &&
                    !isInBox(grid, row - row % 3, col - col % 3, num)
        }

        private fun isInRow(grid: Array<IntArray>, row: Int, num: Int): Boolean {
            for (col in 0 until SIZE) {
                if (grid[row][col] == num) {
                    return true
                }
            }
            return false
        }

        private fun isInColumn(grid: Array<IntArray>, col: Int, num: Int): Boolean {
            for (row in 0 until SIZE) {
                if (grid[row][col] == num) {
                    return true
                }
            }
            return false
        }

        private fun isInBox(grid: Array<IntArray>, startRow: Int, startCol: Int, num: Int): Boolean {
            for (row in 0 until 3) {
                for (col in 0 until 3) {
                    if (grid[row + startRow][col + startCol] == num) {
                        return true
                    }
                }
            }
            return false
        }
    }
}
