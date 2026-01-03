package com.example.myapp.todo

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.myapp.BuildConfig
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImagePicker(
    originalUri: String,
    uriChanged: (String) -> Unit
) {
    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoBase64 by remember { mutableStateOf(originalUri) }
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 1000
                    0f at 0
                    -10f at 100
                    10f at 200
                    -10f at 300
                    10f at 400
                    0f at 500
                }
            )
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempPhotoUri != null) {
                val encodedString = encodeImageToBase64(context, tempPhotoUri!!)
                if (encodedString != null) {
                    currentPhotoBase64 = encodedString
                    uriChanged(currentPhotoBase64)
                }
            }
        }
    )

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { granted ->
            if (granted) {
                val file = context.createImageFile()
                val uri = FileProvider.getUriForFile(
                    Objects.requireNonNull(context),
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*if (currentPhotoBase64.isNotEmpty()) {
            val bitmap = decodeBase64ToBitmap(currentPhotoBase64)
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .size(240.dp)
                        .padding(bottom = 16.dp)
                )
            }
        }*/
        Button(
            modifier = Modifier.graphicsLayer { rotationZ = rotation.value },
            onClick = {
                if (cameraPermissionState.status.isGranted) {
                    val file = context.createImageFile()
                    val uri = FileProvider.getUriForFile(
                        Objects.requireNonNull(context),
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                    )
                    tempPhotoUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            }
        ) {
            Text(text = "Take a photo with Camera")
        }
    }
}

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    return File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )
}

// Funcție helper pentru conversie Bitmap -> Base64
// Folosim android.util.Base64 pentru compatibilitate mai bună decât java.util.Base64
fun encodeImageToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        // Scalăm imaginea ca să nu fie imensă (cum aveai în codul tău 128x128)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 128, 128, false)

        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        "data:image/jpg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Funcție helper pentru conversie Base64 -> Bitmap
fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        // Scoatem prefixul dacă există
        val cleanBase64 = if (base64Str.contains(",")) {
            base64Str.substringAfter(",")
        } else {
            base64Str
        }

        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}