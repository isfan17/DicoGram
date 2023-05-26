package com.isfan17.dicogram.utils

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import android.net.Uri
import android.os.Environment
import android.os.StrictMode
import com.isfan17.dicogram.R
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

object Helper {

    /**
     * MyEditText EMAIL VALIDATION
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return emailRegex.matches(email)
    }

    /**
     * DATE AND TIME STRING GENERATOR
     */
    fun getTimeDiff(context:Context, dateString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(dateString) ?: return ""

        val now = Date()
        val duration = now.time - date.time
        val seconds = duration / 1000

        return when {
            seconds < 60 -> context.getString(R.string.date_just_now)
            seconds < 3600 -> "${seconds / 60} ${context.getString(R.string.date_minutes_ago)}"
            seconds < 86400 -> "${seconds / 3600} ${context.getString(R.string.date_hours_ago)}"
            else -> "${seconds / 86400} ${context.getString(R.string.date_days_ago)}"
        }
    }

    fun getDetailDateFormat(dateString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(dateString)

        val customDateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())
        return customDateFormat.format(date)
    }

    /**
     * IMAGE CONFIGURATION
     */
    // Creating Photo File
    private const val FILENAME_FORMAT = "dd-MMM-yyyy"

    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    fun createFile(application: Application): File {
        val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
            File(it, application.resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        val outputDirectory = if (
            mediaDir != null && mediaDir.exists()
        ) mediaDir else application.filesDir

        return File(outputDirectory, "DicoGram-$timeStamp.jpg")
    }

    fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    fun rotateBitmap(bitmap: Bitmap, isBackCamera: Int): Bitmap {
        val matrix = Matrix()

        return when (isBackCamera)
        {
            // Image from Gallery
            0 -> bitmap
            // Image from Back Camera
            1 -> {
                matrix.postRotate(90f)
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width ,bitmap.height, matrix,  true)
            }
            // Image from Front Camera
            else -> {
                matrix.postRotate(-90f)
                matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f) // flips image
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        }
    }

    fun bitmapFromURL(context: Context, urlString: String): Bitmap {
        return try {
            // Allow access content from URL
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            // Fetch image data from URL
            val url = URL(urlString)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            BitmapFactory.decodeResource(context.resources, R.drawable.dicogram_logo)
        }
    }

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    /**
     * GEOCODER
     */
    fun getAddressFromLatLng(
        context: Context,
        lat: Double,
        lon: Double
    ): String? {
        var location: String? = null
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val list = geocoder.getFromLocation(lat, lon, 1)
            if (list != null && list.size != 0) {
                location = list[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return location
    }

}