package com.shilpakala.app.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.shilpakala.app.data.GalleryItem
import com.shilpakala.app.data.GalleryRepository
import com.shilpakala.app.util.ShareUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onBack: () -> Unit,
    onOpenStudio: () -> Unit,
    viewModel: GalleryViewModel = viewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val repository = remember { GalleryRepository(context) }
    var viewerItem by remember { mutableStateOf<GalleryItem?>(null) }

    val columns = if (LocalConfiguration.current.screenWidthDp >= 600) 3 else 2

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Gallery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No branded photos yet.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Open the studio to create your first heritage catalog shot.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 12.dp)
                )
                Button(
                    onClick = onOpenStudio,
                    modifier = Modifier.padding(top = 24.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Open Studio")
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    GalleryCard(
                        item = item,
                        repository = repository,
                        onClick = { viewerItem = item }
                    )
                }
            }
        }
    }

    viewerItem?.let { open ->
        Dialog(
            onDismissRequest = { viewerItem = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.92f))
                    .padding(16.dp)
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { viewerItem = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                    AsyncImage(
                        model = repository.fileFor(open),
                        contentDescription = open.productName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .aspectRatio(1200f / 1500f),
                        contentScale = ContentScale.Fit
                    )
                    RowButtons(
                        onShare = {
                            ShareUtil.shareJpegFile(context, repository.fileFor(open))
                        },
                        onDelete = {
                            viewModel.delete(open)
                            viewerItem = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowButtons(onShare: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onShare,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(50)
        ) { Text("Share") }
        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(50)
        ) { Text("Delete") }
    }
}

@Composable
private fun GalleryCard(
    item: GalleryItem,
    repository: GalleryRepository,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = repository.fileFor(item),
                contentDescription = item.productName,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Text(
                    text = if (item.price.isBlank()) "—" else "₹ ${item.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
