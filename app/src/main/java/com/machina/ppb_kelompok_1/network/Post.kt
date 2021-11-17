package com.machina.ppb_kelompok_1.network

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("id")
    val id: Int,

    @SerializedName("path")
    val path: String,

    @SerializedName("name")
    val name: String
)