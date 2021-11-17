package com.machina.ppb_kelompok_1.network.response

import com.machina.ppb_kelompok_1.network.Post

data class AllPostResponse(
    val code: Int,

    val message: String,

    val data: List<Post>
)
