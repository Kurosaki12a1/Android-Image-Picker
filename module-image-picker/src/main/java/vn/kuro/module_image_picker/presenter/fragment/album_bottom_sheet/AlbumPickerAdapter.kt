package vn.kuro.module_image_picker.presenter.fragment.album_bottom_sheet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import vn.kuro.module_image_picker.R
import vn.kuro.module_image_picker.constants.Constants.RADIUS
import vn.kuro.module_image_picker.databinding.AlbumPickerItemBinding
import vn.kuro.module_image_picker.domain.model.Album

/**
 * Adapter for displaying albums in a RecyclerView.
 *
 * This adapter is responsible for binding album data to views represented by ViewHolder instances. It allows
 * users to pick an album from a displayed list. Each album displays its name, image count, and a thumbnail
 * of the first image in the album.
 *
 * @param listener An instance of [AlbumPickerListener] to handle click events on album items.
 * @author Thinh Huynh
 * @since 27/02/2024
 */
class AlbumPickerAdapter(private val listener: AlbumPickerListener) :
    ListAdapter<Album, AlbumPickerAdapter.ViewHolder>(AlbumDiffCallback()) {

    /**
     * Creates new ViewHolder instances for album items.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val root =
            LayoutInflater.from(parent.context).inflate(R.layout.album_picker_item, parent, false)
        return ViewHolder(AlbumPickerItemBinding.bind(root))
    }

    /**
     * Binds the album data to the ViewHolder.
     *
     * This method updates the content of the ViewHolder to reflect the album at the given position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(holder.itemView.context, position, getItem(position))
    }

    /**
     * Called when a view created by the adapter is being recycled.
     * Ensures any heavy resources are released to avoid memory leaks.
     */
    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        // Release resources bound to the view to prevent memory leaks
        holder.unBind()
    }

    /**
     * Updates the selection status of albums and notifies changes for efficient item updates.
     *
     * @param position The position of the album that was selected.
     */
    private fun updateData(position: Int) {
        val tempList = currentList.map { it.copy() } as ArrayList
        tempList.forEach { it.isSelected = false }
        tempList[position].isSelected = true
        submitList(tempList)
    }

    /**
     * ViewHolder class for album items.
     *
     * Holds references to the UI components within the RecyclerView item for quick access.
     *
     * @param binding The binding for the album picker item.
     */
    inner class ViewHolder(private val binding: AlbumPickerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transform(CenterCrop(), RoundedCorners(RADIUS))

        /**
         * Binds data to the view holder at the specified position.
         * Sets up the views in the view holder with the album's details.
         *
         * @param context the context in which the adapter is running.
         * @param position the position of the item within the adapter's data set.
         * @param data the album data to be bound to the views.
         */
        fun bind(
            context: Context,
            position: Int,
            data: Album
        ) {
            binding.tvAlbumName.text = data.name
            binding.tvImageCount.text =
                context.getString(R.string.images_count, data.images.size.toString())
            if (data.images.isNotEmpty()) {
                Glide.with(context)
                    .load(data.images[0].uri)
                    .apply(requestOptions)
                    .into(binding.ivAlbum)
            }

            if (data.isSelected) {
                binding.ivCheck.visibility = View.VISIBLE
            } else {
                binding.ivCheck.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                updateData(position)
                listener.onAlbumClick(data, position)
            }
        }

        /**
         * Clears any resources being used by the ImageView to free up memory.
         * Useful when the view is being recycled.
         */
        fun unBind() {
            Glide.with(binding.root.context)
                .clear(binding.ivAlbum)
        }
    }

    class AlbumDiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.id == newItem.id && oldItem.isSelected == newItem.isSelected
        }

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem == newItem
        }
    }

}

interface AlbumPickerListener {
    fun onAlbumClick(album: Album, position: Int)
}