package vn.kuro.module_image_picker.presenter.activity

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import vn.kuro.module_image_picker.R
import vn.kuro.module_image_picker.databinding.ActivityImagePickerBinding
import vn.kuro.module_image_picker.presenter.fragment.album_bottom_sheet.AlbumBottomSheetFragment
import vn.kuro.module_image_picker.utils.createImageUri
import vn.kuro.module_image_picker.utils.saveUriListsToFile

@AndroidEntryPoint
class ImagePickerActivity : AppCompatActivity() {
    companion object {
        // Permissions required for reading and writing to external storage, and camera access
        private const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val CAMERA = Manifest.permission.CAMERA

        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        private const val READ_MEDIA_VISUAL_USER_SELECTED =
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES

        // Request codes for permission requests
        private const val REQUEST_CODE_READ_FILE = 0
        private const val REQUEST_CODE_TAKE_PICTURE = 1

        // Number of columns in the grid layout for displaying images
        private const val COLUMN = 3
    }

    private lateinit var binding: ActivityImagePickerBinding

    private var albumDialog: AlbumBottomSheetFragment? = null

    private var adapter: ImagePickerAdapter? = null

    private val viewModel: ImagePickerViewModel by viewModels()

    // Currently selected album index
    private var selectedAlbum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkRequiredPermission()
        setUpAdapters()
        setUpViews()
        setUpObservers()
    }

    private fun setUpAdapters() {
        val widthItem = Resources.getSystem().displayMetrics.widthPixels / COLUMN
        adapter = ImagePickerAdapter(widthItem, object : ImagePickerListener {
            override fun onTakePhoto() {
                requestPermission(arrayOf(CAMERA), REQUEST_CODE_TAKE_PICTURE)
            }

            override fun onLongClick() {
                adapter?.toggleMultiSelectMode()
            }
        })

        binding.rvPhoto.layoutManager =
            GridLayoutManager(this, COLUMN, GridLayoutManager.VERTICAL, false)
        binding.rvPhoto.setHasFixedSize(true)
        binding.rvPhoto.adapter = adapter

        binding.rvPhoto.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                val column = position % COLUMN
                val spacing = resources.getDimensionPixelSize(R.dimen.dimen5)
                outRect.left = spacing - column * spacing / COLUMN
                outRect.right = (column + 1) * spacing / COLUMN

                if (position < COLUMN) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            }
        })
    }

    private fun setUpViews() {
        albumDialog = AlbumBottomSheetFragment.newInstance()

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.save.setOnClickListener {
            // Save selected images and return to previous activity
            saveUriListsToFile(this, adapter?.imagesChosen?.map { it.uri } as ArrayList<Uri>)
            setResult(Activity.RESULT_OK, Intent())
            finish()
        }

        binding.tvAlbum.setOnClickListener {
            if (albumDialog != null && !albumDialog!!.isVisible) {
                albumDialog?.show(supportFragmentManager, "AlbumBottomSheet")
            }
        }
    }

    private fun setUpObservers() {
        lifecycleScope.launch {
            viewModel.albums.flowWithLifecycle(lifecycle).collectLatest { albums ->
                if (albums.isNotEmpty()) {
                    binding.tvAlbum.text = albums[selectedAlbum].name
                    viewModel.getPhotos(albums[selectedAlbum])
                }
            }
        }

        lifecycleScope.launch {
            viewModel.photos.flowWithLifecycle(lifecycle).collectLatest {
                adapter?.submitData(lifecycle, it)
            }
        }
    }

    private fun checkRequiredPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPermission(
                arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED, WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_FILE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission(
                arrayOf(READ_MEDIA_IMAGES, WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_FILE
            )
        } else {
            requestPermission(
                arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_FILE
            )
        }
    }

    /**
     * Requests the necessary permissions for the activity.
     */
    private fun requestPermission(permissions: Array<out String>, code: Int) {
        requestPermissions(permissions, code)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_READ_FILE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.getAlbums()
                } else {
                    // Cannot get images
                }
            }

            REQUEST_CODE_TAKE_PICTURE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePictureLauncher.launch(createImageUri(contentResolver))
                } else {
                    // Cannot take images
                }
            }

        }
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                viewModel.getAlbums() // Update album
            }
        }

    override fun onDestroy() {
        albumDialog = null
        adapter = null
        super.onDestroy()
    }
}