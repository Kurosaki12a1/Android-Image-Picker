package vn.kuro.image_picker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import vn.kuro.image_picker.adapter.ImageAdapter
import vn.kuro.image_picker.databinding.ActivityMainBinding
import vn.kuro.module_image_picker.presenter.activity.ImagePickerActivity
import vn.kuro.module_image_picker.utils.readUriListFromFile

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ImageAdapter

    private lateinit var binding: ActivityMainBinding

    private val getUriContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                adapter.submitList(readUriListFromFile(this))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpAdapters()
        setUpViews()
    }

    private fun setUpAdapters() {
        adapter = ImageAdapter()
        binding.rvMain.setHasFixedSize(true)
        binding.rvMain.layoutManager = LinearLayoutManager(this)
        binding.rvMain.adapter = adapter
    }

    private fun setUpViews() {
        binding.btnImagePicker.setOnClickListener {
            getUriContent.launch(Intent(this, ImagePickerActivity::class.java))
        }
    }
}
