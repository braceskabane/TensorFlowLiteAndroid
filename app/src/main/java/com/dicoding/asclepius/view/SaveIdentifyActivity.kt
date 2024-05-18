package com.dicoding.asclepius.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
//import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.app.AppCompatDelegate
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
//import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivitySaveIdentifyBinding
import com.dicoding.asclepius.utils.CancerAdapter
import com.dicoding.asclepius.view.factory.SaveIdentifyViewModelFactory
import com.dicoding.asclepius.view.viewmodel.SaveIdentifyViewModel

class SaveIdentifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaveIdentifyBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var saveIdentifyViewModel: SaveIdentifyViewModel
    private lateinit var adapter: CancerAdapter

    private fun loadDataFromApi() {
        progressBar.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveIdentifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = binding.progressBar
        loadDataFromApi()

        adapter = CancerAdapter()
        binding.usersRecycleView2.layoutManager = LinearLayoutManager(this)
        binding.usersRecycleView2.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        binding.usersRecycleView2.adapter = adapter

        val factory = SaveIdentifyViewModelFactory(application)
        saveIdentifyViewModel = ViewModelProvider(this, factory)[SaveIdentifyViewModel::class.java]

        saveIdentifyViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        saveIdentifyViewModel.cancers.observe(this) { cancers ->
            adapter.submitList(cancers)
        }

        saveIdentifyViewModel.getAllCancers()

        binding.imageBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.imageSignOut.setOnClickListener {
            saveIdentifyViewModel.deleteAllCancers()
        }
    }
}