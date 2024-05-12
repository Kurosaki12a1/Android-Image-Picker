package vn.kuro.module_image_picker.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import vn.kuro.module_image_picker.domain.model.Album
import vn.kuro.module_image_picker.domain.model.Photo

interface ImagePickerRepository {

    fun getAlbums(): Flow<List<Album>>

    fun getPhotos(album: Album): Flow<PagingData<Photo>>
}