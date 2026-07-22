package com.lianshan.lslife.feature.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.ui.components.LoadingBox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import kotlin.math.max

data class CropUiState(
    val loading: Boolean = false,
    val uploading: Boolean = false,
    val bitmap: ImageBitmap? = null,
    val originalWidth: Int = 0,
    val originalHeight: Int = 0,
    val message: String? = null,
    val uploadedUrl: String? = null
)

@HiltViewModel
class CropViewModel @Inject constructor(
    private val lsRepository: LsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CropUiState())
    val state: StateFlow<CropUiState> = _state

    fun loadBitmap(context: Context, uriString: String) {
        if (_state.value.bitmap != null) return
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(loading = true) }
            try {
                val file = java.io.File(context.cacheDir, "avatar_temp.jpg")
                if (!file.exists()) {
                    throw Exception("文件不存在或保存失败")
                }

                // 1. Check original size
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(file.absolutePath, options)
                
                // 2. Calculate inSampleSize to avoid OOM
                var scale = 1
                while (options.outWidth / scale > 2048 || options.outHeight / scale > 2048) {
                    scale *= 2
                }
                
                // 3. Decode actual bitmap
                options.inJustDecodeBounds = false
                options.inSampleSize = scale
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                var bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                
                if (bitmap != null) {
                    // 4. Handle Exif rotation
                    val exif = android.media.ExifInterface(file.absolutePath)
                    val orientation = exif.getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL
                    )
                    
                    val matrix = android.graphics.Matrix()
                    when (orientation) {
                        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    }
                    
                    if (!matrix.isIdentity) {
                        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                        if (rotatedBitmap != bitmap) {
                            bitmap.recycle()
                            bitmap = rotatedBitmap
                        }
                    }

                    val imageBitmap = bitmap.asImageBitmap()
                    _state.update { 
                        it.copy(
                            loading = false, 
                            bitmap = imageBitmap,
                            originalWidth = bitmap.width,
                            originalHeight = bitmap.height
                        ) 
                    }
                } else {
                    throw Exception("图片格式不支持或无法解码")
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                _state.update { it.copy(loading = false, message = "无法加载图片: ${e.message}") }
            }
        }
    }

    fun cropAndUpload(context: Context, bitmap: ImageBitmap, offset: Offset, scale: Float, cropSize: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(uploading = true) }
            try {
                // 1. Convert ImageBitmap to Android Bitmap
                val androidBitmap = bitmap.asAndroidBitmap()
                
                // 2. Calculate crop rect
                // The image is drawn at center of the screen, with pan/zoom.
                // We need to reverse the transform to find what part of original image is in the crop circle.
                // Let's use a simpler approach: Draw the original bitmap into a new canvas with the transform, and extract the center.
                val resultSize = 500
                val resultBmp = Bitmap.createBitmap(resultSize, resultSize, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(resultBmp)
                
                val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
                
                // We need to map the center 500x500 box from the screen coordinates to the bitmap coordinates.
                // It is easier to just apply the same scale/translate to the canvas.
                // cropSize is the size of the square on screen.
                // The ratio from resultSize to cropSize is `resultSize / cropSize`
                val ratio = resultSize / cropSize
                
                canvas.scale(ratio, ratio)
                
                // Move to center of result 
                canvas.translate(cropSize / 2f, cropSize / 2f)
                
                // Apply the user's offset & scale
                canvas.translate(offset.x, offset.y)
                canvas.scale(scale, scale)
                
                // Draw the original image centered
                canvas.translate(-androidBitmap.width / 2f, -androidBitmap.height / 2f)
                
                canvas.drawBitmap(androidBitmap, 0f, 0f, paint)

                // 3. Compress using AvatarCompressor
                val compressedBytes = AvatarCompressor.compressAvatar(resultBmp, 50 * 1024, 500)
                resultBmp.recycle()

                // 4. Upload
                val reqFile = compressedBytes.toRequestBody("image/*".toMediaTypeOrNull())
                val part = okhttp3.MultipartBody.Part.createFormData("image", "avatar.jpg", reqFile)
                
                val res = lsRepository.uploadImage(part)
                if (res.isSuccess) {
                    val url = res.getOrNull()?.url
                    if (url != null) {
                        withContext(Dispatchers.Main) {
                            _state.update { it.copy(uploading = false, uploadedUrl = url) }
                        }
                    } else {
                        throw Exception("Upload returned null url")
                    }
                } else {
                    throw Exception("Upload failed")
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(uploading = false, message = "裁剪上传失败: ${e.message ?: "未知错误"}") }
                }
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    uriString: String,
    onBack: () -> Unit,
    onCropped: (String) -> Unit,
    viewModel: CropViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(uriString) {
        viewModel.loadBitmap(context, uriString)
    }

    LaunchedEffect(state.uploadedUrl) {
        state.uploadedUrl?.let { onCropped(it) }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("裁剪头像") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "取消")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Black
    ) { padding ->
        if (state.loading) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }

        val bitmap = state.bitmap
        if (bitmap == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("无法加载图片", color = Color.White)
            }
            return@Scaffold
        }

        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var viewportSize by remember { mutableStateOf(IntSize.Zero) }

        // The size of the circular crop hole
        val cropSize = if (viewportSize.width > 0) (viewportSize.width * 0.8f) else 0f

        // Initial setup to fit the image in the crop circle
        LaunchedEffect(bitmap, viewportSize) {
            if (viewportSize.width > 0 && bitmap.width > 0 && scale == 1f) {
                // Determine initial scale to just cover the crop hole
                val minDim = minOf(bitmap.width, bitmap.height).toFloat()
                scale = cropSize / minDim
            }
        }

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .onSizeChanged { viewportSize = it }
        ) {
            Canvas(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.1f, 5f)
                        offset += pan
                    }
                }) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw the image
                withTransform({
                    this.translate(left = canvasWidth / 2f + offset.x, top = canvasHeight / 2f + offset.y)
                    this.scale(scaleX = scale, scaleY = scale)
                    this.translate(left = -bitmap.width / 2f, top = -bitmap.height / 2f)
                }) {
                    drawImage(image = bitmap)
                }

                // Draw the mask
                val path = Path().apply {
                    addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                    addOval(
                        Rect(
                            center = Offset(canvasWidth / 2, canvasHeight / 2),
                            radius = cropSize / 2
                        )
                    )
                    fillType = PathFillType.EvenOdd
                }
                drawPath(path = path, color = Color.Black.copy(alpha = 0.6f))
                
                // Draw crop circle outline
                drawCircle(
                    color = Color.White,
                    radius = cropSize / 2,
                    center = Offset(canvasWidth / 2, canvasHeight / 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }

            // Bottom Confirm Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (state.uploading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    FloatingActionButton(
                        onClick = {
                            viewModel.cropAndUpload(context, bitmap, offset, scale, cropSize)
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "确认裁剪")
                    }
                }
            }
        }
    }
}
