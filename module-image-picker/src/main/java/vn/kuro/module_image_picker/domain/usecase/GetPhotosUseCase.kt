package vn.kuro.module_image_picker.domain.usecase

import vn.kuro.module_image_picker.domain.model.Album
import vn.kuro.module_image_picker.domain.repository.ImagePickerRepository
import javax.inject.Inject

/**
 * Use case for retrieving photos within a specific album.
 *
 * Similar to GetAlbumsUseCase, this class is designed to fetch photos for a given album from the repository.
 * It abstracts the details of data fetching from the presentation layer, allowing for a cleaner architecture and
 * easier testing.
 *
 * @param repo The repository from which photos will be fetched. This dependency is also provided via constructor injection.
 * @author Thinh Huynh
 * @since 27/02/2024
 */
class GetPhotosUseCase @Inject constructor(private val repo : ImagePickerRepository) {
    /**
     * Invokes the use case to retrieve photos for a specific album.
     *
     * @param album The album for which photos are to be fetched.
     * @return A flow containing a list of photos from the specified album.
     */
    operator fun invoke(album: Album) = repo.getPhotos(album)
}