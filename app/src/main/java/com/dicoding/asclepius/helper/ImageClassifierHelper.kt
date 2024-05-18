package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier


class ImageClassifierHelper(
    private var threshold: Float = 0.1f,
    private var maxResults: Int = 3,
    private val modelName: String = "cancer_classification.tflite",
    val context: Context
) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }

    private fun setupImageClassifier() {
        // TODO: Menyiapkan Image Classifier untuk memproses gambar.
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)
        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(4)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            Log.e(TAG, e.message.toString())
        }
    }

    fun classifyStaticImage(uri: Uri, onAnalysisComplete: (imageUri: String, labels: Array<String>, probs: FloatArray) -> Unit) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }?.copy(Bitmap.Config.ARGB_8888, true)

            bitmap?.let { it ->
                val tensorImage = TensorImage.fromBitmap(it)

                val classificationResult = imageClassifier?.classify(tensorImage)

                classificationResult?.let { result ->
                    val categories = result.map {
                        it.categories.map { category ->
                            Category(category.label, category.score)
                        } }.flatten()
                    val labels = categories.map { it.label }.toTypedArray()
                    val probs = categories.map { it.score }.toFloatArray()

                    onAnalysisComplete(uri.toString(), labels, probs)
                }
            } ?: run {
                Log.e(TAG, "Failed to load bitmap from URI")
                showToast("Failed to load bitmap from URI")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing image: ${e.message}")
            showToast("Error analyzing image: ${e.message}")
        }
    }

    data class Category(val label: String, val score: Float)

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}