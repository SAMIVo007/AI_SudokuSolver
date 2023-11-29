package com.example.myapplication

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

fun preProcess(img: Mat): Mat {
    val imgGray = Mat()
    Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_BGR2GRAY)
    Imgproc.GaussianBlur(imgGray, imgGray, Size(5.0, 5.0), 1.0)
    val imgThreshold = Mat()
    Imgproc.adaptiveThreshold(imgGray, imgThreshold, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 11, 2.0)
    return imgThreshold
}



fun reorder(myPoints: MatOfPoint): MatOfPoint {

    val points = myPoints.toList()

    val myPointsNew = Array(4) { Point() }

    val add = points.map { it.x + it.y }
    myPointsNew[0] = points[add.indexOf(add.minOrNull())]
    myPointsNew[3] = points[add.indexOf(add.maxOrNull())]

    val diff = points.map { it.y - it.x }
    myPointsNew[1] = points[diff.indexOf(diff.minOrNull())]
    myPointsNew[2] = points[diff.indexOf(diff.maxOrNull())]

    return MatOfPoint(*myPointsNew)
}



fun biggestContour(contours: List<MatOfPoint>): Pair<MatOfPoint, Double> {
    var biggest = MatOfPoint()
    var maxArea = 0.0
    for (contour in contours) {
        val area = Imgproc.contourArea(contour)
        if (area > 50) {
            val peri = Imgproc.arcLength(MatOfPoint2f(*contour.toArray()), true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*contour.toArray()), approx, 0.02 * peri, true)
            if (area > maxArea && approx.toArray().size == 4) {
                biggest = MatOfPoint()
                approx.convertTo(biggest, CvType.CV_32S)
                maxArea = area
            }
        }
    }
    return Pair(biggest, maxArea)
}



fun splitBoxes(img: Mat): List<Mat> {
    val rows = mutableListOf<Mat>()
    val boxes = mutableListOf<Mat>()

    val cellHeight = img.rows() / 9
    val cellWidth = img.cols() / 9

    for (r in 0 until 9) {
        for (c in 0 until 9) {
            val cell = img.submat(r * cellHeight, (r + 1) * cellHeight, c * cellWidth, (c + 1) * cellWidth)
            boxes.add(cell)
        }
    }

    return boxes
}



fun getPrediction(images: List<InputImage>, onTextRecognized: (List<Int>) -> Unit) {
    val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    val recognizedTextList = mutableListOf<Int>()

    for (image in images) {
        textRecognizer.process(image)
            .addOnSuccessListener { visionText: Text ->
                val detectedText: String = visionText.text
                if (detectedText.isNotBlank()) {
                    try {
                        val intValue = detectedText.toDouble().toInt()
                        recognizedTextList.add(intValue)
                    } catch (e: NumberFormatException) {
                        // Handle the case where the text is not a valid integer
                        recognizedTextList.add(0) // Add 0 to the list for invalid or non-integer text
                    }
                } else {
                    recognizedTextList.add(0) // Add 0 to the list for blank text
                }
            }
            .addOnCompleteListener {
                // If all images are processed, invoke the callback with the results
                if (recognizedTextList.size == images.size) {
                    onTextRecognized(recognizedTextList)
                }
            }
    }
}



fun displayNumbers(img: Mat, numbers: List<Int>, color: Scalar): Mat {
    val secW = img.cols() / 9
    val secH = img.rows() / 9
    for (x in 0 until 9) {
        for (y in 0 until 9) {
            if (numbers[y * 9 + x] != 0) {
                Imgproc.putText(
                    img,
                    numbers[y * 9 + x].toString(),
                    Point(x * secW + secW / 2 - 10.0, (y + 0.8) * secH),
                    Imgproc.FONT_HERSHEY_COMPLEX_SMALL,
                    2.0,
                    color,
                    2
                )
            }
        }
    }
    return img
}



fun convertMatToInputImage(mat: Mat, rotationDegrees: Int): InputImage {
    // Convert Mat to Bitmap
    val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(mat, bitmap)

    // Create InputImage from Bitmap
    return InputImage.fromBitmap(bitmap, rotationDegrees)
}



// Define a function to solve the Sudoku puzzle
fun solveSudoku(bo: Array<IntArray>): Boolean {
    val find = findEmpty(bo)
    if (find == null) {
        return true
    } else {
        val (row, col) = find
        for (i in 1..9) {
            if (isValid(bo, i, row, col)) {
                bo[row][col] = i
                if (solveSudoku(bo)) {
                    return true
                }
                bo[row][col] = 0
            }
        }
        return false
    }
}

// Define a function to check if a number is valid in a cell
fun isValid(bo: Array<IntArray>, num: Int, row: Int, col: Int): Boolean {
    // Check row
    for (i in bo[row]) {
        if (i == num) {
            return false
        }
    }
    // Check column
    for (i in bo) {
        if (i[col] == num) {
            return false
        }
    }
    // Check box
    val boxRow = row / 3 * 3
    val boxCol = col / 3 * 3
    for (i in boxRow until boxRow + 3) {
        for (j in boxCol until boxCol + 3) {
            if (bo[i][j] == num) {
                return false
            }
        }
    }
    return true
}

// Define a function to find an empty cell in the Sudoku board
fun findEmpty(bo: Array<IntArray>): Pair<Int, Int>? {
    for (i in bo.indices) {
        for (j in bo[i].indices) {
            if (bo[i][j] == 0) {
                return Pair(i, j)
            }
        }
    }
    return null
}



//fun drawGrid(img: Mat): Mat {
//    val secW = img.cols() / 9
//    val secH = img.rows() / 9
//    for (i in 0 until 9) {
//        val pt1 = Point(0.0, secH * i.toDouble())
//        val pt2 = Point(img.cols().toDouble(), secH * i.toDouble())
//        val pt3 = Point(secW * i.toDouble(), 0.0)
//        val pt4 = Point(secW * i.toDouble(), img.rows().toDouble())
//        Imgproc.line(img, pt1, pt2, Scalar(255.0, 255.0, 0.0), 2)
//        Imgproc.line(img, pt3, pt4, Scalar(255.0, 255.0, 0.0), 2)
//    }
//    return img
//}

