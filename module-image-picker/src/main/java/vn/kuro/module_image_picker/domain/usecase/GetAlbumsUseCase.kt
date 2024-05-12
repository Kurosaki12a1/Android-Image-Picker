package vn.kuro.module_image_picker.domain.usecase

import vn.kuro.module_image_picker.domain.repository.ImagePickerRepository
import javax.inject.Inject

/**
 * Use case for retrieving a list of albums.
 *
 * This class encapsulates the operation of fetching albums from a repository. It is a part of the domain layer
 * and acts as an intermediary between the repository and the presentation layer, ensuring that the application's
 * architecture remains clean and organized.
 *
 * @param repo The repository from which albums will be fetched. This dependency is provided via constructor injection.
 * @author Thinh Huynh
 * @since 27/02/2024
 */
class GetAlbumsUseCase @Inject constructor(private val repo : ImagePickerRepository) {
    /**
     * Invokes the use case to retrieve albums.
     *
     * @return A flow containing a list of albums.
     */
    operator fun invoke() = repo.getAlbums()
}