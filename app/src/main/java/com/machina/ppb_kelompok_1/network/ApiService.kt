package com.machina.ppb_kelompok_1.network

import com.machina.ppb_kelompok_1.core.Endpoints
import com.machina.ppb_kelompok_1.network.request.PostTweetRequest
import com.machina.ppb_kelompok_1.network.response.AllPostResponse
import com.machina.ppb_kelompok_1.network.response.GenericResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET(Endpoints.ALL_POST)
    suspend fun getAllPost(): Response<AllPostResponse>

    @POST(Endpoints.SUBMIT_POST)
    suspend fun submitPost(
        @Body request: PostTweetRequest
    ): Response<GenericResponse>
}