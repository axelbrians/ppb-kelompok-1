package com.machina.ppb_kelompok_1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machina.ppb_kelompok_1.core.Response
import com.machina.ppb_kelompok_1.network.ApiService
import com.machina.ppb_kelompok_1.network.Post
import com.machina.ppb_kelompok_1.network.request.PostTweetRequest
import com.machina.ppb_kelompok_1.network.response.AllPostResponse
import com.machina.ppb_kelompok_1.network.response.GenericResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val apiService: ApiService
): ViewModel() {

    private val _allPostsResult : MutableLiveData<Response<AllPostResponse>> = MutableLiveData()
    val allPostsResult : LiveData<Response<AllPostResponse>> = _allPostsResult

    private val _posts : MutableLiveData<List<Post>> = MutableLiveData()
    val posts : LiveData<List<Post>> = _posts

    private val _postTweetResponse : MutableLiveData<Response<GenericResponse>> = MutableLiveData()
    val postTweetResponse : LiveData<Response<GenericResponse>> = _postTweetResponse


    fun getAllPost() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService.getAllPost()
                val body = res.body()
                Timber.d(body.toString())


                if (res.isSuccessful) {
                    _allPostsResult.postValue(Response.Success(body!!))
                    _posts.postValue(body.data)
                }
            } catch (err: Exception) {

            }
        }
    }

    fun postTweet(request: PostTweetRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService.submitPost(request)
                val body = res.body()

                if (res.isSuccessful && body != null) {
                    _postTweetResponse.postValue(Response.Success(body))
                } else {
                    _postTweetResponse.postValue(Response.Error("Something went wrong"))
                }

            } catch (error: Error) {

            }
        }
    }

}