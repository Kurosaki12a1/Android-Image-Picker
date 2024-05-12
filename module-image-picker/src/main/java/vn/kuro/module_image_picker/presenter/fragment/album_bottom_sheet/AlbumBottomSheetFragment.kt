package vn.kuro.module_image_picker.presenter.fragment.album_bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import vn.kuro.module_image_picker.databinding.AlbumBottomSheetBinding
import vn.kuro.module_image_picker.domain.model.Album
import vn.kuro.module_image_picker.presenter.activity.ImagePickerViewModel

@AndroidEntryPoint
class AlbumBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(): AlbumBottomSheetFragment {
            return AlbumBottomSheetFragment()
        }
    }

    private val viewModel: ImagePickerViewModel by activityViewModels()

    private var _binding: AlbumBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var adapter: AlbumPickerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = AlbumBottomSheetBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        setUpObservers()
    }

    private fun setUpView() {
        adapter = AlbumPickerAdapter(object : AlbumPickerListener {
            override fun onAlbumClick(album: Album, position: Int) {
                viewModel.getPhotos(album)
                dismiss()
            }
        })
        binding.rvAlbum.setHasFixedSize(true)
        binding.rvAlbum.adapter = adapter
    }

    private fun setUpObservers() {
        lifecycleScope.launch {
            viewModel.albums.flowWithLifecycle(lifecycle).collectLatest { albums ->
                adapter?.submitList(albums)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        adapter = null
        super.onDestroyView()
    }
}