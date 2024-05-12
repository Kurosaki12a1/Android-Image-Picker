package vn.kuro.module_image_picker.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import vn.kuro.module_image_picker.constants.Constants.TEMP_SAVED_FILE
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun createImageUri(contentResolver: ContentResolver): Uri {
    val timeStamp = convertMillisToDate()
    val imageFileName = "vn_kuro_$timeStamp.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
}

private fun convertMillisToDate(): String {
    val format = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    val calender = Calendar.getInstance()
    calender.timeInMillis = System.currentTimeMillis()
    return format.format(calender.time)
}

/**
 * Saves a list of image URIs to a file in the private storage of the app.
 * This method is useful when the list is too large to be passed through a Bundle.
 *
 * @param context The context of the application, used to access the file system.
 * @param images The list of image URIs to be saved.
 * @return The name of the file where the list of URIs is saved.
 */
fun saveUriListsToFile(context: Context, images: ArrayList<Uri>): String {
    val fileName = TEMP_SAVED_FILE
    val fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
    ObjectOutputStream(fos).use { it.writeObject(images.map { str -> str.toString() }) }
    fos.close()
    return fileName
}

/**
 * Reads a list of image URIs from a file in the private storage of the app.
 * This method retrieves the list that was previously saved when it was too large to pass through a Bundle.
 *
 * @param context The context of the application, used to access the file system.
 * @return An ArrayList of URIs that were read from the file.
 */
fun readUriListFromFile(context: Context): ArrayList<Uri> {
    val fis: FileInputStream = context.openFileInput(TEMP_SAVED_FILE)
    var uriList: ArrayList<String> = ArrayList()
    try {
        ObjectInputStream(fis).use { ois -> uriList = ois.readObject() as ArrayList<String> }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    fis.close()

    // Đảm bảo xóa file sau khi sử dụng để không chiếm dụng bộ nhớ không cần thiết
    context.deleteFile(TEMP_SAVED_FILE)
    return uriList.map { Uri.parse(it) } as ArrayList<Uri>
}