package vn.kuro.module_image_picker.presenter.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import vn.kuro.module_image_picker.domain.model.Album
import vn.kuro.module_image_picker.domain.model.Photo
import vn.kuro.module_image_picker.domain.usecase.GetAlbumsUseCase
import vn.kuro.module_image_picker.domain.usecase.GetPhotosUseCase
import javax.inject.Inject

@HiltViewModel
class ImagePickerViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val getPhotosUseCase: GetPhotosUseCase
) : ViewModel() {
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums = _albums.asStateFlow()

    private val _photos = MutableStateFlow<PagingData<Photo>>(PagingData.empty())
    val photos = _photos.asStateFlow()

    fun getAlbums() {
        viewModelScope.launch {
            getAlbumsUseCase().collect {
                _albums.value = it
            }
        }
    }

    fun getPhotos(album: Album) {
        viewModelScope.launch {
            getPhotosUseCase(album = album).cachedIn(viewModelScope).collectLatest {
                _photos.value = it
            }
        }
    }
}