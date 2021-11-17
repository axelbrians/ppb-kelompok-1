package com.machina.ppb_kelompok_1

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import com.machina.ppb_kelompok_1.core.Response
import com.machina.ppb_kelompok_1.databinding.ActivityMainBinding
import com.machina.ppb_kelompok_1.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.fragment.app.DialogFragment
import com.machina.ppb_kelompok_1.databinding.DialogCompleteActionBinding
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.os.Environment
import android.util.Base64
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.machina.ppb_kelompok_1.network.request.PostTweetRequest
import timber.log.Timber
import java.io.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var postAdapter: PostAdapter

    private val viewModel by viewModels<MainActivityViewModel>()

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        subscribeToObservable()
    }

    private fun setupView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postAdapter = PostAdapter()
        binding.activityMainRecycler.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
        }

        binding.activityMainFab.setOnClickListener {


            val dialogBinding = DialogCompleteActionBinding.inflate(layoutInflater)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .show()

            dialogBinding.dialogCompleteActionGallery.setOnClickListener {
                launchGalleryIntent()
                dialog.dismiss()
            }

            dialogBinding.dialogCompleteActionCamera.setOnClickListener {
                launchCameraIntent()
                dialog.dismiss()
            }
        }

        binding.activityMainRefreshLayout.setOnRefreshListener {
            viewModel.getAllPost()
        }
    }

    private fun subscribeToObservable() {
        viewModel.postTweetResponse.observe(this) { res ->
            when (res) {
                is Response.Success -> viewModel.getAllPost()
                is Response.Error -> Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                is Response.Loading -> { }
                else -> Unit
            }
        }

        viewModel.allPostsResult.observe(this) { res ->
            when (res) {
                is Response.Success -> binding.activityMainRefreshLayout.isRefreshing = false
                is Response.Error -> binding.activityMainRefreshLayout.isRefreshing = false
                is Response.Loading -> binding.activityMainRefreshLayout.isRefreshing = true
                else -> Unit
            }
        }

        viewModel.posts.observe(this) { res ->
            postAdapter.dataSet = res
        }

        viewModel.getAllPost()
    }

    private fun isCameraPermissionAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED
    }

    private fun launchGalleryIntent() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent, "Pick a picture")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

        startActivityForResult(chooserIntent, PICK_IMAGE_CODE)
    }

    private fun launchCameraIntent() {

        if (!isCameraPermissionAllowed()) {
            ActivityCompat.requestPermissions(
                this,
                listOf(Manifest.permission.CAMERA).toTypedArray(),
                REQ_CAMERA_PERMISSION
            )
            return
        }

        // Create intent to launch camera app
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Create the File where the photo should go
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.machina.ppb_kelompok_1",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent,
                    CAPTURE_IMAGE_CODE
                )
            }
        }
    }

    lateinit var currentPhotoPath: String
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "temp", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_CAMERA_PERMISSION && isCameraPermissionAllowed()) {
            launchCameraIntent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_CODE && resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let {
                val imageUri = it
                createAndLaunchTweetRequest(imageUri)
            }

        } else if (resultCode == Activity.RESULT_OK && requestCode == CAPTURE_IMAGE_CODE) {
            val imageUri = Uri.fromFile(File(currentPhotoPath))
            createAndLaunchTweetRequest(imageUri)
        }
    }

    private fun createAndLaunchTweetRequest(imageUri: Uri?) {
        imageUri?.let {
            val stream = contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(stream)
            stream?.close()

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

            val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
            val request = PostTweetRequest(encoded)
            Log.d("MainActivity", request.toString())

            viewModel.postTweet(request)
        }
    }

    companion object {
        const val PICK_IMAGE_CODE = 11231
        const val CAPTURE_IMAGE_CODE = 41231
        const val REQ_CAMERA_PERMISSION = 122
        const val THUMBNAIL_SIZE = 300.0
    }
}