package vn.kuro.module_image_picker.data.paging_source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import vn.kuro.module_image_picker.domain.model.Photo
import javax.inject.Inject
import kotlin.math.min

/**
 * A PagingSource implementation for loading images from a list of URIs.
 *
 * This class is responsible for providing the data source for pagination of images in the app. It uses a list of [Photo] objects,
 * each representing an image URI, to supply data for paginated loading.
 *
 * @property uris The list of [Photo] objects that this PagingSource will paginate through.
 * @constructor Creates an ImagePickerPagingSource with a given list of Photo.
 *
 * @author Thinh Huynh
 * @since 27/02/2024
 */
class ImagePickerPagingSource @Inject constructor(
    private val uris: ArrayList<Photo>
) : PagingSource<Int, Photo>() {

    companion object {
        // Number of items to load per page
        const val PAGE_SIZE = 15

        // The initial index to start loading from
        private const val INITIAL_PAGE_INDEX = 0
    }

    /**
     * Determines the page key to use when the PagingSource is refreshed.
     *
     * This function computes the next page key based on the anchor position from the current [PagingState].
     *
     * @param state The current [PagingState] from which to compute the next page key.
     * @return The key of the page to load on refresh, or `null` if no such page can be determined.
     */
    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.plus(PAGE_SIZE) ?: page?.nextKey?.minus(PAGE_SIZE)
        }
    }

    /**
     * Loads a page of data.
     *
     * This function is responsible for loading a specific page of [Photo] objects based on the given [LoadResult].
     * It calculates the subset of URIs to load based on the current page index and the specified load size.
     *
     * @param params The parameters specifying the load operation, including the key of the page to load and the requested load size.
     * @return A [LoadResult] object containing the loaded data and information about the loading state.
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        return try {
            val start = params.key ?: INITIAL_PAGE_INDEX
            val end = min(start + params.loadSize, uris.size)
            val data = uris.subList(start, end)
            LoadResult.Page(
                data = data,
                prevKey = if (start == INITIAL_PAGE_INDEX) null else start - PAGE_SIZE,
                nextKey = if (end >= uris.size) null else end
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}