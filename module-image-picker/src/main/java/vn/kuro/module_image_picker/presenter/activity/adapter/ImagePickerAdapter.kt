package vn.kuro.module_image_picker.presenter.activity.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import vn.kuro.module_image_picker.R
import vn.kuro.module_image_picker.databinding.PickPhotoItemBinding
import vn.kuro.module_image_picker.databinding.TakePhotoItemBinding
import vn.kuro.module_image_picker.domain.model.Photo

/**
 * An adapter for the RecyclerView used in an image picker, supporting paging and custom image handling.
 * @param listener An instance of ImagePickerListener for callback events.
 * @property imagesChosen A list to keep track of selected images.
 * @property isMultiSelectMode A flag to indicate if multi-select mode is enabled.
 * @property oldSelected The position of the previously selected item in single selection mode.
 * @author Thinh Huynh
 * @since 27/02/2024
 */
class ImagePickerAdapter(
    private val listener: ImagePickerListener
) :
    PagingDataAdapter<Photo, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    // Tracks the images that have been selected.
    var imagesChosen = ArrayList<Photo>()

    // Flag to indicate if multi-select mode is enabled.
    private var isMultiSelectMode = false

    // This is for single selection mode
    private var oldSelected = -1


    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new RecyclerView.ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        // Calculate the size for each item based on parent width and padding.
        val size = (parent.width - parent.resources.getDimensionPixelSize(R.dimen.dimen10) * 4) / 3
        return when (viewType) {
            R.layout.take_photo_item -> {
                // Inflate the layout for the "take photo" item and set its size.
                val binding =
                    TakePhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                binding.root.layoutParams = RecyclerView.LayoutParams(size, size)
                TakePhotoViewHolder(binding, listener)
            }

            else -> {
                // Inflate the layout for a photo item and set its size.
                val binding =
                    PickPhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                binding.root.layoutParams = RecyclerView.LayoutParams(size, size)
                PickPhotoViewHolder(binding, listener, size)
            }
        }
    }

    /**
     * Returns the view type of the item at position for the purposes of view recycling.
     * @param position Position to query.
     * @return Integer value identifying the type of the view needed to represent the item at position.
     */
    override fun getItemViewType(position: Int): Int {
        return if (position == FIRST_INDEX_ITEM) R.layout.take_photo_item
        else R.layout.album_picker_item
    }

    /**
     * Called when a view created by this adapter has been recycled.
     * @param holder The ViewHolder for the view being recycled.
     */
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is PickPhotoViewHolder) {
            holder.unBind()
        }
        super.onViewRecycled(holder)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TakePhotoViewHolder -> {}

            is PickPhotoViewHolder -> {
                holder.bind(getItem(position - 1), isMultiSelectMode)
            }
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int = super.getItemCount() + 1 // +1 for the camera item

    /**
     * ViewHolder class for the "Take Photo" item.
     * @param binding The binding for the "Take Photo" item layout.
     * @param listener The listener for click events.
     */
    class TakePhotoViewHolder(
        binding: TakePhotoItemBinding,
        listener: ImagePickerListener
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.ivPhoto.setOnClickListener {
                listener.onTakePhoto()
            }
        }
    }

    /**
     * ViewHolder class for the photo item.
     * @param binding The binding for the photo item layout.
     * @param listener The listener for click events.
     * @param size The size of the photo item.
     */
    class PickPhotoViewHolder(
        private val binding: PickPhotoItemBinding,
        private val listener: ImagePickerListener,
        size: Int,
    ) : RecyclerView.ViewHolder(binding.root) {
        // Request options for loading the image with Glide.
        private val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(size)

        /**
         * Binds the photo data to the item view.
         * @param photo The photo to bind.
         * @param isMultiSelectMode Whether multi-select mode is enabled.
         */
        fun bind(photo: Photo?, isMultiSelectMode: Boolean) {
            if (photo == null) return

            Glide.with(binding.ivPhoto.context)
                .load(photo.uri)
                .apply(requestOptions)
                .into(binding.ivPhoto)

            if (isMultiSelectMode) {
                binding.ivChoose.visibility = View.VISIBLE
            } else {
                binding.ivChoose.visibility = View.GONE
            }

            if (photo.isSelected) {
                binding.ivChoose.setImageResource(R.drawable.ic_circle_checked)
                if (!isMultiSelectMode) {
                    binding.ivChoose.visibility = View.VISIBLE
                }
            } else {
                binding.ivChoose.setImageResource(R.drawable.ic_circle_unchecked)
                if (!isMultiSelectMode) {
                    binding.ivChoose.visibility = View.GONE
                }
            }

            binding.root.setOnClickListener {
                listener.onClick(photo, isMultiSelectMode)
            }

            binding.root.setOnLongClickListener {
                listener.onLongClick()
                true
            }
        }

        /**
         * Clears the image view when the view is recycled.
         */
        fun unBind() {
            Glide.with(binding.root.context).clear(binding.ivPhoto)
        }
    }

    /**
     * Toggles the multi-select mode.
     * Resets the selection and notifies the adapter of data changes.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun toggleMultiSelectMode() {
        isMultiSelectMode = !isMultiSelectMode
        imagesChosen.clear()
        oldSelected = -1
        snapshot().items.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }


    /**
     * Updates the selection state of a photo.
     * @param photo The photo to update the selection state for.
     */
    fun updatePhotoSelection(photo: Photo) {
        if (isMultiSelectMode) {
            // In multi-select mode, find the photo in the current snapshot with the same ID.
            val clickedPhoto = snapshot().items.find { it.id == photo.id }
            clickedPhoto?.let {
                // Toggle the selection status of the found photo.
                it.isSelected = !it.isSelected
                // Add the toggled photo to the list of chosen images.
                imagesChosen.add(it)
                // Notify that the item at the found index has changed to update the UI.
                notifyItemChanged(snapshot().items.indexOf(it) + 1)
            }
        } else {
            // In single-select mode, clear all previously chosen images.
            imagesChosen.clear()
            // Add the newly selected photo to the list of chosen images.
            imagesChosen.add(photo)
            val clickedPhoto = snapshot().items.find { it.id == photo.id }
            clickedPhoto?.let {
                if (oldSelected == -1) {
                    // If no photo was previously selected, update oldSelected to the new index.
                    oldSelected = snapshot().items.indexOf(it) + 1
                } else {
                    // Deselect the previously selected photo.
                    snapshot().items[oldSelected - 1].isSelected = false
                    // Notify that the old selected photo has changed to update the UI.
                    notifyItemChanged(oldSelected)
                    // Update oldSelected to the new index.
                    oldSelected = snapshot().items.indexOf(it) + 1
                }
                // Toggle the selection status of the found photo.
                it.isSelected = !it.isSelected
            }
            // Notify that the newly selected photo has changed to update the UI.
            notifyItemChanged(oldSelected)
        }
    }

    companion object {
        // DiffUtil callback for calculating the difference between two non-null items in a list.
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean =
                oldItem.uri == newItem.uri

            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean =
                oldItem == newItem
        }

        private const val FIRST_INDEX_ITEM = 0
    }
}

interface ImagePickerListener {
    fun onTakePhoto()

    fun onLongClick()

    fun onClick(photo: Photo, isMultiSelectMode: Boolean)
}