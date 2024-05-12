package vn.kuro.module_image_picker.presenter.activity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import vn.kuro.module_image_picker.R
import vn.kuro.module_image_picker.constants.Constants.RADIUS
import vn.kuro.module_image_picker.databinding.PickPhotoItemBinding
import vn.kuro.module_image_picker.databinding.TakePhotoItemBinding
import vn.kuro.module_image_picker.domain.model.Photo

/**
 * An adapter for the RecyclerView used in an image picker, supporting paging and custom image handling.
 * @param listener An instance of ImagePickerListener for callback events.
 * @author Thinh Huynh
 * @since 27/02/2024
 */
class ImagePickerAdapter(
    private val width: Int,
    private val listener: ImagePickerListener
) :
    PagingDataAdapter<Photo, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    // Tracks the images that have been selected.
    var imagesChosen = ArrayList<Photo>()

    private var isMultiSelectMode = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.take_photo_item -> {
                val binding =
                    TakePhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TakePhotoViewHolder(binding, listener)
            }

            else -> {
                val binding =
                    PickPhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PickPhotoViewHolder(binding, listener, width)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == FIRST_INDEX_ITEM) R.layout.take_photo_item
        else R.layout.album_picker_item
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is PickPhotoViewHolder) {
            holder.unBind()
        }
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TakePhotoViewHolder -> {
                // Do nothing
            }

            is PickPhotoViewHolder -> {
                holder.bind(getItem(position - 1), imagesChosen, isMultiSelectMode)
            }
        }
    }

    override fun getItemCount(): Int = super.getItemCount() + 1 // +1 for the camera item

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

    class PickPhotoViewHolder(
        private val binding: PickPhotoItemBinding,
        private val listener: ImagePickerListener,
        size: Int
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
      //      .override(size)

        fun bind(photo: Photo?, imagesChosen: ArrayList<Photo>, isMultiSelectMode: Boolean) {
            if (photo == null) return

            Glide.with(binding.ivPhoto.context)
                .load(photo.uri)
                .apply(requestOptions)
                .into(binding.ivPhoto)

            if (photo.isSelected) {
                binding.ivChoose.setImageResource(R.drawable.ic_circle_checked)
            } else {
                binding.ivChoose.setImageResource(R.drawable.ic_circle_unchecked)
            }

            binding.root.setOnClickListener {
                photo.isSelected = !photo.isSelected
                if (!isMultiSelectMode) {
                    imagesChosen.clear()
                }
                if (photo.isSelected) {
                    binding.ivChoose.setImageResource(R.drawable.ic_circle_checked)
                    imagesChosen.add(photo)
                } else {
                    binding.ivChoose.setImageResource(R.drawable.ic_circle_unchecked)
                    imagesChosen.remove(photo)
                }
            }

            binding.root.setOnLongClickListener {
                listener.onLongClick()
                true
            }
        }

        fun unBind() {
            Glide.with(binding.root.context)
                .clear(binding.ivPhoto)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun toggleMultiSelectMode() {
        isMultiSelectMode = !isMultiSelectMode
        notifyDataSetChanged()
    }

    companion object {
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
}