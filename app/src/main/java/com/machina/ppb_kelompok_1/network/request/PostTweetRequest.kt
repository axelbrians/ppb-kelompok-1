package com.machina.ppb_kelompok_1.network.request

import com.google.gson.annotations.SerializedName

data class PostTweetRequest(
    @SerializedName("image")
    val image: String
)