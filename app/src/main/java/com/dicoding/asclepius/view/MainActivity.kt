package com.dicoding.asclepius.view

import android.Manifest
//import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.utils.getImageUri
//import com.dicoding.asclepius.utils.uriToFile


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast("Permission request granted")
            } else {
                showToast("Permission request denied")
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let { uri ->
                analyzeImage(uri) { imageUri, labels, probs ->
                    moveToResult(imageUri, labels, probs)
                }
            } ?: run {
                showToast(getString(R.string.empty_image_warning))
            }
        }
        binding.historySave.setOnClickListener{
            startActivity(Intent(this, SaveIdentifyActivity::class.java))
        }

    }

    private fun startGallery() {
        // TODO: Mendapatkan gambar dari Gallery.

        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

//    private fun analyzeImage(uri: Uri, onAnalysisComplete: (imageUri: String, labels: Array<String>, probs: FloatArray) -> Unit) {
//        // TODO: Menganalisa gambar yang berhasil ditampilkan.
//
//        binding.progressIndicator.visibility = View.VISIBLE
//        // Initialize your image classifier model here
//        var model: CancerClassification? = null
//        try {
//            model = CancerClassification.newInstance(this)
//
//            // Convert URI to Bitmap for image analysis
//            val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                val source = ImageDecoder.createSource(contentResolver, uri)
//                ImageDecoder.decodeBitmap(source)
//            } else {
//                MediaStore.Images.Media.getBitmap(contentResolver, uri)
//            }?.copy(Bitmap.Config.ARGB_8888, true)
//
//            // Check if bitmap is null
//            bitmap?.let { imageBitmap ->
//                // Process the bitmap using the image classifier model
//                val tensorImage = TensorImage.fromBitmap(imageBitmap)
//                val imageProcessor = ImageProcessor.Builder()
//                    // Add image processing options if needed
//                    .build()
//
//                val processedImage = imageProcessor.process(tensorImage)
//                val outputs = model.process(processedImage)
//                val probability = outputs.probabilityAsCategoryList
//
//                // Convert CategoryList to separate labels and probabilities
//                val categoryLabels = mutableListOf<String>()
//                val categoryProbs = mutableListOf<Float>()
//                for (category in probability) {
//                    categoryLabels.add(category.label)
//                    categoryProbs.add(category.score)
//                }
//                // Call onAnalysisComplete with the necessary data
//                onAnalysisComplete(uri.toString(), categoryLabels.toTypedArray(), categoryProbs.toFloatArray())
//            } ?: run {
//                // Log error message if bitmap is null
//                Log.e("Image Analysis", "Failed to load bitmap from URI")
//                // Inform user about the error
//                showToast("Failed to load bitmap from URI")
//            }
//        } catch (e: Exception) {
//            // Log any exceptions that occur during image analysis
//            Log.e("Image Analysis", "Error analyzing image: ${e.message}")
//            // Inform user about the error
//            showToast("Error analyzing image: ${e.message}")
//        } finally {
//            // Close the model to release resources
//            model?.close()
//            binding.progressIndicator.visibility = View.GONE
//        }
//    }

    private fun analyzeImage(uri: Uri, onAnalysisComplete: (imageUri: String, labels: Array<String>, probs: FloatArray) -> Unit) {
        binding.progressIndicator.visibility = View.VISIBLE

        val imageClassifierHelper = ImageClassifierHelper(context = this)

        imageClassifierHelper.classifyStaticImage(uri)
        { imageUri, labels, probs ->
            onAnalysisComplete(imageUri, labels, probs)
            binding.progressIndicator.visibility = View.GONE
        }
    }


    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun moveToResult(imageUri: String, labels: Array<String>, probs: FloatArray) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, imageUri)
        intent.putExtra(ResultActivity.EXTRA_CATEGORY_LABELS, labels)
        intent.putExtra(ResultActivity.EXTRA_CATEGORY_PROBS, probs)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}