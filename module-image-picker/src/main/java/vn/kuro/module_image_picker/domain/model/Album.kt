package vn.kuro.module_image_picker.domain.model

import android.net.Uri

data class Album(
    val id: Long,
    val name: String,
    val images: ArrayList<Photo>,
    var isSelected: Boolean = false
)

data class Photo(val id: Long, val uri: Uri, var isSelected: Boolean = false)
