package vn.kuro.module_image_picker.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import vn.kuro.module_image_picker.constants.Constants.PAGE_SIZE
import vn.kuro.module_image_picker.data.paging_source.ImagePickerPagingSource
import vn.kuro.module_image_picker.domain.model.Album
import vn.kuro.module_image_picker.domain.model.Photo
import vn.kuro.module_image_picker.domain.repository.ImagePickerRepository
import javax.inject.Inject

class ImagePickerRepositoryImpl @Inject constructor(private val contentResolver: ContentResolver) :
    ImagePickerRepository {
    companion object {
        private const val ID_TOTAL_PHOTO_ALBUM = 230797250203L
        private const val NAME_TOTAL_PHOTO_ALBUM = "All Photos"
    }

    override fun getAlbums(): Flow<List<Album>> = flow {
        val albumMap = mutableMapOf<Long, Album>()

        // Query albums from media store
        val projection = arrayOf(
            // Get Album Name
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            // Get Album ID
            MediaStore.Images.Media.BUCKET_ID,
            // Get ID from image
            MediaStore.Images.Media._ID
        )

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            // Sort by date added
            MediaStore.Images.Media.DATE_ADDED + " ASC"
        )?.use { cursor ->
            val bucketNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val bucketName = cursor.getString(bucketNameColumn)
                val bucketId = cursor.getLong(bucketIdColumn)
                // Get list Uri images from id
                val contentUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                val photo = Photo(id, contentUri)
                val album = albumMap.getOrPut(bucketId) {
                    Album(bucketId, bucketName, arrayListOf())
                }
                album.images.add(photo)
            }
        }
        val albums = ArrayList<Album>()
        // Create "All Photo Albums" album
        val totalPhotoAlbum = Album(
            ID_TOTAL_PHOTO_ALBUM,
            NAME_TOTAL_PHOTO_ALBUM,
            albumMap.values.flatMap { it.images }.toCollection(ArrayList()),
            true
        )
        albums.add(totalPhotoAlbum)
        albums.addAll(albumMap.values)
        emit(albums)
    }.flowOn(Dispatchers.IO)

    override fun getPhotos(album: Album): Flow<PagingData<Photo>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE * 2
            ),
            pagingSourceFactory = {
                ImagePickerPagingSource(album.images.reversed().toCollection(ArrayList()))
            }
        ).flow.flowOn(Dispatchers.IO)
}