package com.tristannio.todo.user

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.modernstorage.permissions.RequestAccess
import com.google.modernstorage.permissions.StoragePermissions
import com.google.modernstorage.storage.AndroidFileSystem
import com.tristannio.todo.R
import com.tristannio.todo.databinding.ActivityUserInfoBinding
import com.tristannio.todo.network.Api
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*


class UserInfoActivity : AppCompatActivity() {
    private var _binding: ActivityUserInfoBinding? = null
    private val binding get() = _binding!!

    private val webService = Api.userWebService

    private val fileSystem by lazy { AndroidFileSystem(this) }

    private lateinit var photoUri: Uri

    private val getPhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            binding.avatarUser.load(photoUri)
            lifecycleScope.launch {
                val response = webService.updateAvatar(photoUri.toRequestBody())
                binding.avatarUser.load(response.body()?.avatar) {
                    error(R.drawable.ic_launcher_background)
                    placeholder(R.drawable.ic_launcher_background)
                }
            }
        }

    private val requestCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { accepted ->

            photoUri = fileSystem.createMediaStoreUri(
                filename = "picture-${UUID.randomUUID()}.jpg",
                collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                directory = "Todo",
            )!!
            getPhoto.launch(photoUri)
        }

    val requestWriteAccess = registerForActivityResult(RequestAccess()) { accepted ->
        val camPermission = Manifest.permission.CAMERA
        val permissionStatus = checkSelfPermission(camPermission)
        val isAlreadyAccepted = permissionStatus == PackageManager.PERMISSION_GRANTED
        val isExplanationNeeded = shouldShowRequestPermissionRationale(camPermission)
        when {
            isAlreadyAccepted -> getPhoto.launch(photoUri)
            isExplanationNeeded -> showMessage("Permission required")
            else -> requestCamera.launch(camPermission)
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setAction("Open Settings") {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                startActivity(intent)
            }
            .show()
    }

    fun launchCameraWithPermissions() {
        requestWriteAccess.launch(
            RequestAccess.Args(
                action = StoragePermissions.Action.READ_AND_WRITE,
                types = listOf(StoragePermissions.FileType.Image),
                createdBy = StoragePermissions.CreatedBy.Self
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityUserInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val uploadImage = binding.uploadImageButton
        val takePicture = binding.takePictureButton
        takePicture.setOnClickListener {
            launchCameraWithPermissions()
        }

        uploadImage.setOnClickListener {
            openGallery()
        }
    }

    // launcher pour la permission d'accès au stockage
    val requestReadAccess = registerForActivityResult(RequestAccess()) { hasAccess ->
        if (hasAccess) {
            galleryLauncher.launch("image/*")
        } else {
            showMessage("Permission of Gallery required")
        }
    }

    fun openGallery() {
        requestReadAccess.launch(
            RequestAccess.Args(
                action = StoragePermissions.Action.READ,
                types = listOf(StoragePermissions.FileType.Image),
                createdBy = StoragePermissions.CreatedBy.AllApps
            )
        )
    }

    private fun Uri.toRequestBody(): MultipartBody.Part {
        val fileInputStream = contentResolver.openInputStream(this)!!
        val fileBody = fileInputStream.readBytes().toRequestBody()
        return MultipartBody.Part.createFormData(
            name = "avatar",
            filename = "temp.jpeg",
            body = fileBody
        )
    }

    // register
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@registerForActivityResult
            binding.avatarUser.load(uri)
            lifecycleScope.launch {
                val response = webService.updateAvatar(uri.toRequestBody())
                binding.avatarUser.load(response.body()?.avatar) {
                    error(R.drawable.ic_launcher_background)
                    placeholder(R.drawable.ic_launcher_background)
                }
            }

        }

}