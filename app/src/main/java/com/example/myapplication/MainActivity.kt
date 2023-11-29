package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.mlkit.vision.common.InputImage
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc


val numbers = IntArray(81)

class MainActivity : ComponentActivity() {

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data ?: return@registerForActivityResult
            // Update the selectedImage Uri state
            setSelectedImageUri(selectedImageUri)
        }
    }

    private var selectedImageUri = mutableStateOf<Uri?>(null)


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {

            if (OpenCVLoader.initDebug()) Log.d("LOADED", "success")
            else Log.d("LOADED", "err")

            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    private fun setSelectedImageUri(uri: Uri) {
        selectedImageUri.value = uri
    }

    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


    @Composable
    fun MainScreen(){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    // Open the gallery to select an image
                    galleryLauncher.launch(Intent(Intent.ACTION_PICK).setType("image/*"))

                },
                content = {
                    Text("Select Image")
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Display the selected image if available
            if (selectedImageUri.value != null) {
                // Load the image from the Uri and display it using an Image composable
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    bitmap =  processAndLaunchActivity(selectedImageUri.value!!),
                    contentDescription = null
                )


            } else {
                Text("No image selected")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }



    private fun processAndLaunchActivity(uri: Uri): ImageBitmap {

        // Check if the selectedImageUri has a value and proceed with processing
        val contentResolver = contentResolver
        val stream = contentResolver.openInputStream(uri)
        var bitmap = BitmapFactory.decodeStream(stream)

        // Check if the image needs to be rotated
        val exif = uri.path?.let { ExifInterface(contentResolver.openInputStream(uri)!!) }

        // Apply rotation to the bitmap based on the orientation
        when (exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                bitmap = rotateBitmap(bitmap, 90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                bitmap = rotateBitmap(bitmap, 180f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                bitmap = rotateBitmap(bitmap, 270f)
            }
            // Add more cases if needed
        }

//////////////////////////////////////////////////////// IMG PROCESSING ///////////////////////////////////////////////////////////////////////////////////////

        if (bitmap != null) {

            //#################### 1. PREPARE THE IMAGE ######################
            //resizing not done
            val img = Mat()
            Utils.bitmapToMat(bitmap, img)

            // Preprocess the image
            val imgThreshold = preProcess(img)


            //#################### 2. FIND ALL CONTOURS ######################
            val hierarchy = Mat()
            val contours = mutableListOf<MatOfPoint>()
            Imgproc.findContours(imgThreshold, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
            // DRAW ALL DETECTED CONTOURS
            // Imgproc.drawContours(imgThreshold, contours, -1, Scalar(255.0, 255.0, 255.0), 5)


            //#################### 3. FIND THE BIGGEST CONTOUR AND USE IT AS SUDOKU ######################
            val (biggest, maxArea) = biggestContour(contours)
            Log.d("Biggest Contour", biggest.toString())

            if (biggest.width() != 0 && biggest.height() != 0) {
                try {
                    // Reorder the points of the biggest contour
                    val biggestReordered = reorder(biggest)
                    Log.d("Reordered Biggest Contour", biggestReordered.toString())

                    // Draw the biggest contour on the imgDetectedDigits image
//                    Imgproc.drawContours(imgThreshold, listOf(biggestReordered), -1, Scalar(255.0, 255.0, 255.0), 15)

                    // Prepare points for the perspective warp
                    val biggestReorderedArray = biggestReordered.toArray()
                    val pt1 = Point(biggestReorderedArray[0].x, biggestReorderedArray[0].y)
                    val pt2 = Point(biggestReorderedArray[1].x, biggestReorderedArray[1].y)
                    val pt3 = Point(biggestReorderedArray[2].x, biggestReorderedArray[2].y)
                    val pt4 = Point(biggestReorderedArray[3].x, biggestReorderedArray[3].y)

                    val pts1 = MatOfPoint2f(pt1, pt2, pt3, pt4)

                    // Use the dimensions of the destination image directly
                    val pts2 = MatOfPoint2f(
                        Point(0.0, 0.0),
                        Point(imgThreshold.cols().toDouble(), 0.0),
                        Point(0.0, imgThreshold.rows().toDouble()),
                        Point(imgThreshold.cols().toDouble(), imgThreshold.rows().toDouble())
                    )

                    // Check for invalid points
                    if (pts1.size() != pts2.size() || pts1.rows() != 4 || pts2.rows() != 4) {
                        Log.e("ImageProcessing", "Error: Invalid points for perspective warp.")
                        return ImageBitmap(1, 1)
                    }

                    // Calculate the transformation matrix for the perspective warp
                    val matrix = Imgproc.getPerspectiveTransform(pts1, pts2)

                    // Apply the perspective warp to the input image
                    val imgWarpColored = Mat()
                    Imgproc.warpPerspective(imgThreshold, imgWarpColored, matrix, imgThreshold.size())

                    // Convert the image to a bitmap
                    bitmap = Bitmap.createBitmap(imgWarpColored.cols(), imgWarpColored.rows(), Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(imgWarpColored, bitmap)




                    //#################### 4. SPLIT THE IMAGE AND FIND EACH DIGIT AVAILABLE ######################

                    val boxes = splitBoxes(imgWarpColored)
                    println(boxes.size)

                    // Convert Mat images to InputImage
                    val images = mutableListOf<InputImage>()
                    for (box in boxes) {
                        val inputImage = convertMatToInputImage(box,0)
                        images.add(inputImage)
                    }


                    getPrediction(images) { recognizedNumbers ->
                        println("Recognized Numbers Size: ${recognizedNumbers.size}")
                        recognizedNumbers.toIntArray().copyInto(numbers)
                        println("Total Numbers Size: ${numbers.size}")
                        println("Numbers: ${numbers.joinToString()}")
                    }


                    // Check if the activity should be launched
                    Intent(applicationContext, SudokuDisplay::class.java).also {
                        startActivity(it)
                    }


                } catch (e: Exception) {
                    Log.e("ImageProcessing", "Error during perspective warp: ${e.message}")
                    e.printStackTrace()
                    return ImageBitmap(1, 1)
                }
            }
        }

        return bitmap?.asImageBitmap() ?: ImageBitmap(1, 1)

    }
}
