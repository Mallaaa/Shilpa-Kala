package com.shilpakala.app.ui.studio

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.shilpakala.app.data.BrandBackground
import com.shilpakala.app.util.ShareUtil
import android.graphics.Bitmap
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import androidx.compose.foundation.Image

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun StudioScreen(
    onBack: () -> Unit,
    viewModel: StudioViewModel = viewModel()
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message, state.error) {
        val msg = state.message
        val err = state.error
        when {
            msg != null -> {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearMessage()
            }
            err != null -> {
                snackbarHostState.showSnackbar(err)
                viewModel.clearMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Heritage Studio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            StepHeader(step = state.step, onStepSelected = { viewModel.goToStep(it) })
            when (state.step) {
                0 -> CaptureStep(viewModel)
                1 -> BrandFormStep(viewModel)
                2 -> ResultStep(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun StepHeader(step: Int, onStepSelected: (Int) -> Unit) {
    val labels = listOf("Capture", "Brand", "Result")
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { index, label ->
            val active = index == step
            val done = index < step
            val color = when {
                active -> MaterialTheme.colorScheme.primary
                done -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStepSelected(index) }
                    .padding(4.dp)
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = color,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CaptureStep(viewModel: StudioViewModel) {
    val permission = rememberPermissionState(Manifest.permission.CAMERA)
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    val pickGallery = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.loadBitmapFromUri(it) }
    }

    LaunchedEffect(Unit) {
        if (!permission.status.isGranted) {
            permission.launchPermissionRequest()
        }
    }

    Column(Modifier.fillMaxSize()) {
        when {
            permission.status.isGranted -> {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    CameraPreview(
                        lifecycleOwner = lifecycleOwner,
                        onCaptureReady = { viewModel.imageCapture = it },
                        modifier = Modifier.fillMaxSize()
                    )
                    GuideOverlay(Modifier.fillMaxSize())
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { pickGallery.launch("image/*") }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Upload from Gallery")
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = { viewModel.takePhoto(cameraExecutor) },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Capture")
                    }
                }
            }

            permission.status.shouldShowRationale -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Camera access helps you frame crafts with guided lines. You can still upload from gallery.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { permission.launchPermissionRequest() }) {
                        Text("Allow camera")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { pickGallery.launch("image/*") }) {
                        Text("Use gallery only")
                    }
                }
            }

            else -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Camera permission is needed for live preview.", textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { permission.launchPermissionRequest() }) {
                        Text("Request permission")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { pickGallery.launch("image/*") }) {
                        Text("Upload from Gallery")
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    onCaptureReady: (ImageCapture) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val providerRef = remember { AtomicReference<ProcessCameraProvider?>(null) }

    DisposableEffect(lifecycleOwner, previewView) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()
            providerRef.set(provider)
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            onCaptureReady(capture)
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    capture
                )
            } catch (_: Exception) {
            }
        }, mainExecutor)
        onDispose {
            providerRef.getAndSet(null)?.unbindAll()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@Composable
fun GuideOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val grid = Color.White.copy(alpha = 0.4f)
        val stroke = Stroke(width = 1.5f)

        val x1 = w / 3f
        val x2 = w * 2f / 3f
        val y1 = h / 3f
        val y2 = h * 2f / 3f
        drawLine(grid, Offset(x1, 0f), Offset(x1, h), strokeWidth = stroke.width)
        drawLine(grid, Offset(x2, 0f), Offset(x2, h), strokeWidth = stroke.width)
        drawLine(grid, Offset(0f, y1), Offset(w, y1), strokeWidth = stroke.width)
        drawLine(grid, Offset(0f, y2), Offset(w, y2), strokeWidth = stroke.width)

        val sq = minOf(w, h) * 0.55f
        val left = (w - sq) / 2f
        val top = (h - sq) / 2f
        val dash = Stroke(
            width = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(left, top),
            size = Size(sq, sq),
            cornerRadius = CornerRadius(12f, 12f),
            style = dash
        )

        val bracket = 36f
        val t = 4f
        val white = Color.White.copy(alpha = 0.9f)
        fun cornerL(x0: Float, y0: Float, dx: Float, dy: Float) {
            drawLine(white, Offset(x0, y0), Offset(x0 + dx, y0), strokeWidth = t, cap = StrokeCap.Round)
            drawLine(white, Offset(x0, y0), Offset(x0, y0 + dy), strokeWidth = t, cap = StrokeCap.Round)
        }
        cornerL(left, top, bracket, 0f)
        cornerL(left, top, 0f, bracket)
        cornerL(left + sq, top, -bracket, 0f)
        cornerL(left + sq, top, 0f, bracket)
        cornerL(left, top + sq, bracket, 0f)
        cornerL(left, top + sq, 0f, -bracket)
        cornerL(left + sq, top + sq, -bracket, 0f)
        cornerL(left + sq, top + sq, 0f, -bracket)
    }
}

@Composable
private fun BrandFormStep(viewModel: StudioViewModel) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = state.productName,
            onValueChange = viewModel::setProductName,
            label = { Text("Product name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.woodType,
            onValueChange = viewModel::setWoodType,
            label = { Text("Wood / material") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.price,
            onValueChange = viewModel::setPrice,
            label = { Text("Price (₹)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.artisanName,
            onValueChange = viewModel::setArtisanName,
            label = { Text("Artisan name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Background", style = MaterialTheme.typography.titleMedium)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BackgroundThumb(
                title = "Cream Silk",
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFF8E9C4), Color(0xFFE9C97A))
                ),
                selected = state.background == BrandBackground.CREAM,
                onClick = { viewModel.setBackground(BrandBackground.CREAM) },
                modifier = Modifier.weight(1f)
            )
            BackgroundThumb(
                title = "Walnut",
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF6B3E1F), Color(0xFF3A2010))
                ),
                selected = state.background == BrandBackground.WOOD,
                onClick = { viewModel.setBackground(BrandBackground.WOOD) },
                modifier = Modifier.weight(1f)
            )
            BackgroundThumb(
                title = "Terracotta",
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFC56B3A), Color(0xFF7A2E12))
                ),
                selected = state.background == BrandBackground.TERRACOTTA,
                onClick = { viewModel.setBackground(BrandBackground.TERRACOTTA) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { viewModel.generateHeritagePhoto() },
            enabled = !state.isGenerating && state.sourceBitmap != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (state.isGenerating) {
                CircularProgressIndicator(
                    Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Generate heritage photo")
            }
        }
    }
}

@Composable
private fun BackgroundThumb(
    title: String,
    brush: Brush,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(brush, RoundedCornerShape(12.dp))
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                )
        )
        Spacer(Modifier.height(6.dp))
        Text(title, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ResultStep(viewModel: StudioViewModel) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val bmp = state.resultBitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Branded photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1200f / 1500f),
                contentScale = ContentScale.Fit
            )
        } else {
            Text("No result yet.")
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val b = state.resultBitmap ?: return@OutlinedButton
                    val uri = ShareUtil.saveBitmapToGallery(
                        context,
                        b,
                        state.productName.ifBlank { "Shilpa-Kala" }
                    )
                    Toast.makeText(
                        context,
                        if (uri != null) "Saved to gallery" else "Could not save",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.weight(1f),
                enabled = bmp != null && !state.isSaving
            ) { Text("Save to Gallery") }

            OutlinedButton(
                onClick = {
                    val b = state.resultBitmap ?: return@OutlinedButton
                    val file = File(context.cacheDir, "share-${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { out ->
                        b.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    }
                    ShareUtil.shareJpegFile(context, file)
                },
                modifier = Modifier.weight(1f),
                enabled = bmp != null
            ) { Text("Share") }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val b = state.resultBitmap ?: return@OutlinedButton
                    val tmp = File(context.cacheDir, "dl-${System.currentTimeMillis()}.jpg")
                    FileOutputStream(tmp).use { out ->
                        b.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    }
                    val uri = ShareUtil.copyFileToPicturesFolder(context, tmp)
                    tmp.delete()
                    Toast.makeText(
                        context,
                        if (uri != null) "Saved to Pictures/Shilpa-Kala" else "Could not save",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.weight(1f),
                enabled = bmp != null
            ) { Text("Download") }

            OutlinedButton(
                onClick = { viewModel.startOver() },
                modifier = Modifier.weight(1f)
            ) { Text("Start Over") }
        }

        OutlinedButton(
            onClick = { viewModel.saveToAppGallery() },
            modifier = Modifier.fillMaxWidth(),
            enabled = bmp != null && !state.isSaving
        ) { Text("Add to My Gallery (in app)") }
    }
}
