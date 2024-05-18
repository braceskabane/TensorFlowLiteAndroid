package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
//import android.view.View
//import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.lifecycleScope
import com.dicoding.asclepius.R
import com.dicoding.asclepius.data.lokal.entity.CancerIdentify
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.view.viewmodel.CancerSaveUpdateViewModel
//import com.google.android.material.snackbar.Snackbar
//import kotlinx.coroutines.launch


class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var cancerViewModel: CancerSaveUpdateViewModel
//    private var label: String = "Cancer"
//    private var score: Float = 0.0F
//    private var image: String? = null
//    private var isSave: Boolean = false
//    private lateinit var progressBar: ProgressBar
//
//    private fun loadDataFromApi() {
//        progressBar.visibility = View.VISIBLE
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        imageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.resultImage.setImageURI(it)
        }

        val resultLabels = intent.getStringArrayExtra(EXTRA_CATEGORY_LABELS)
        val resultProbs = intent.getFloatArrayExtra(EXTRA_CATEGORY_PROBS)
        if (resultLabels != null && resultProbs != null) {
            displayResults(resultLabels, resultProbs)
        } else {
            binding.resultText.text = getString(R.string.no_prediction_available)
        }

        binding.imageBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


//        cancerViewModel = ViewModelProvider(this)[CancerSaveUpdateViewModel::class.java]
//        checkIsSaved(label, score)
//        cancerViewModel.getCancerIdentifyByLabelAndScore(label,score).observe(this) { saveCancer ->
//            isSave = saveCancer != null
//            updateSaveButtonIcon()
//        }
//        binding.saveButton.setOnClickListener {
//            toggleFavoriteStatus(label, image, score)
//        }
    }

//    private fun toggleFavoriteStatus(label: String, image: String?, score: Float?) {
//        lifecycleScope.launch {
//            if (isSave) {
//                removeFromFavorites(label, image, score)
//            } else {
//                addToFavorites(label, image, score)
//            }
//            updateSaveButtonIcon()
//        }
//    }
//
//    private fun addToFavorites(label: String, image: String?, score: Float?) {
//        val saveCancer = CancerIdentify(label, image, score)
//        cancerViewModel.insert(saveCancer)
//        Snackbar.make(binding.root, "User added to favorites", Snackbar.LENGTH_SHORT).show()
//        isSave = true
//        updateSaveButtonIcon()
//    }
//
//    private fun removeFromFavorites(label: String, image: String?, score: Float?) {
//        val saveCancer = CancerIdentify(label, image, score)
//        cancerViewModel.delete(saveCancer)
//        Snackbar.make(binding.root, "User removed from favorites", Snackbar.LENGTH_SHORT).show()
//        isSave = false
//        updateSaveButtonIcon()
//    }
//
//    private fun checkIsSaved(label: String, score: Float) {
//        cancerViewModel.getCancerIdentifyByLabelAndScore(label, score).observe(this) { saveCancer ->
//            isSave = saveCancer != null
//            updateSaveButtonIcon()
//        }
//    }
//
//    private fun updateSaveButtonIcon() {
//        if (isSave) {
//            binding.saveButton.setImageResource(R.drawable.ic_checkbox)
//        } else {
//            binding.saveButton.setImageResource(R.drawable.ic_checkbox_2)
//        }
//    }

    private fun displayResults(labels: Array<String>, probs: FloatArray) {
        val highestIndex = findHighestProbabilityIndex(probs)

        if (highestIndex >= 0) {
            // Dapatkan label dengan indeks tertinggi
            val label = labels[highestIndex]
            // Dapatkan skor (probabilitas) dengan indeks tertinggi
            val score = probs[highestIndex]

            // Simpan URL gambar dari intent
            val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
            val imageUriString = imageUri.toString()
            imageUriString.let { imageUrl ->
                val cancerIdentify = CancerIdentify(
                    image = imageUrl,
                    labels = label,
                    score = score
                )

                // Simpan objek CancerIdentify ke dalam database lokal menggunakan ViewModel
                cancerViewModel = ViewModelProvider(this)[CancerSaveUpdateViewModel::class.java]
                cancerViewModel.insert(cancerIdentify)
            }

            binding.conclusion.text = labels[highestIndex]  // Display highest label
            val resultString = buildString {
                for (i in labels.indices) {
                    append("${labels[i]}: ${probs[i]}\n")
                }
            }
            binding.resultText.text = resultString

        } else {
            Log.e("ResultActivity", "No valid probability found")
        }
    }

    // New function to find the index of the highest probability
    private fun findHighestProbabilityIndex(probs: FloatArray): Int {
        var highestIndex = -1
        for (i in probs.indices) {
            if (highestIndex == -1 || probs[i] > probs[highestIndex]) {
                highestIndex = i
            }
        }
        return highestIndex
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_CATEGORY_LABELS = "extra_labels"
        const val EXTRA_CATEGORY_PROBS = "extra_probs"
    }
}