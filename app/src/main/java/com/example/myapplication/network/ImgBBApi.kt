package com.example.myapplication.network

import com.example.myapplication.model.ImgBBResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImgBBApi {
    @Multipart
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part,
        @Query("name") name: String? = null,
        @Query("expiration") expiration: Int? = null
    ): Response<ImgBBResponse>
}
