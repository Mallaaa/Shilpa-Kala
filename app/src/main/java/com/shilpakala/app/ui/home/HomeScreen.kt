package com.shilpakala.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenStudio: () -> Unit,
    onMyGallery: () -> Unit
) {
    val heroBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(heroBrush)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = "Shilpa-Kala",
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Turn craft photos into luxury catalog shots.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                    )
                }
            }

            item {
                FeatureCard(
                    title = "Guided Capture",
                    subtitle = "Rule-of-thirds framing for heirloom-worthy shots.",
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) }
                )
            }
            item {
                FeatureCard(
                    title = "Heritage Label",
                    subtitle = "Gold seal and Karnataka story on every frame.",
                    icon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null) }
                )
            }
            item {
                FeatureCard(
                    title = "Price Tag",
                    subtitle = "Elegant pricing for boutique and craft fairs.",
                    icon = { Icon(Icons.Default.Sell, contentDescription = null) }
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Button(
                    onClick = onOpenStudio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Open Studio", style = MaterialTheme.typography.labelLarge)
                }
            }

            item {
                OutlinedButton(
                    onClick = onMyGallery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("My Gallery", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth()) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.align(Alignment.CenterStart),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(Modifier.padding(10.dp)) {
                        icon()
                    }
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
