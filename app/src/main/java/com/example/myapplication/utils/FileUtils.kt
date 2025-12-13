package com.example.myapplication.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun uriToMultipart(context: Context, imageUri: Uri, partName: String = "image"): MultipartBody.Part {
        val file = copyUriToCache(context, imageUri)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    private fun copyUriToCache(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri) 
            ?: throw RuntimeException("Cannot open input stream for URI")
        val tempFile = File.createTempFile("imgbb_upload_", ".jpg", context.cacheDir)
        
        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output)
        }
        return tempFile
    }
}
