package com.example.presentation.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.common.extensions.formatDuration
import com.example.common.extensions.formatSize
import com.example.domain.models.AudioFile
import com.example.domain.models.DuplicateReviewItem
import com.example.domain.models.ExactDuplicateGroup
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateReviewScreen(
    exactDuplicateGroups: List<ExactDuplicateGroup>,
    onBack: () -> Unit,
    viewModel: DuplicateReviewViewModel = viewModel(
        factory = DuplicateReviewViewModelFactory(exactDuplicateGroups)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel.audioPlayer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel.audioPlayer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Duplicates", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            if (!uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Background)
                        .border(1.dp, BorderColor)
                        .padding(16.dp)
                ) {
                    val cleanSize = uiState.filesSelectedForDeletion.sumOf { it.sizeBytes }
                    Button(
                        onClick = {
                            // Deletion logic would go here
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                        shape = RoundedCornerShape(28.dp),
                        enabled = uiState.filesSelectedForDeletion.isNotEmpty()
                    ) {
                        Text("Clean ${uiState.selectedFilesCount} Files (${cleanSize.formatSize()})", color = Surface3, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Background
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ReviewStatistics(uiState)
                SelectionActions(viewModel)
                ReviewList(uiState, viewModel)
            }
        }
    }
}

@Composable
fun SelectionActions(viewModel: DuplicateReviewViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val items = uiState.reviewItems
    
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val actions = listOf(
            "Keep Recommended" to { viewModel.selectionManager.keepRecommended(items) },
            "Keep Newest" to { viewModel.selectionManager.keepNewest(items) },
            "Keep Oldest" to { viewModel.selectionManager.keepOldest(items) },
            "Keep Largest" to { viewModel.selectionManager.keepLargest(items) },
            "Invert" to { viewModel.selectionManager.invertSelection(items) },
            "Select All" to { viewModel.selectionManager.selectAllDuplicates(items) },
            "Clear" to { viewModel.selectionManager.clearSelection() }
        )
        actions.forEach { (label, action) ->
            AssistChip(
                onClick = action,
                label = { Text(label, fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Surface2,
                    labelColor = TextPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            )
        }
    }
}

@Composable
fun ReviewStatistics(uiState: DuplicateReviewUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Groups", "${uiState.duplicateGroupsCount}", Modifier.weight(1f))
            StatCard("Files", "${uiState.duplicateFilesCount}", Modifier.weight(1f))
            StatCard("Recoverable", uiState.totalRecoverableStorage.formatSize(), Modifier.weight(1.5f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Selected", "${uiState.selectedFilesCount}", Modifier.weight(1f), AccentPrimary)
            StatCard("Est. After Deletion", uiState.estimatedStorageAfterDeletion.formatSize(), Modifier.weight(1f), AccentSecondary)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = TextPrimary) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = TextSecondary, maxLines = 1)
    }
}

@Composable
fun ReviewList(uiState: DuplicateReviewUiState, viewModel: DuplicateReviewViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(uiState.reviewItems) { item ->
            DuplicateGroupCard(item, uiState, viewModel)
        }
    }
}

@Composable
fun DuplicateGroupCard(
    item: DuplicateReviewItem,
    uiState: DuplicateReviewUiState,
    viewModel: DuplicateReviewViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Surface2)
            .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Surface3),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎵", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(item.group.files.first().title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1)
                    Text("${item.group.files.first().artist} • ${item.group.files.size} copies • ${((item.group.files.size - 1) * item.group.sizeBytes).formatSize()} recoverable", fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = TextSecondary
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300))
        ) {
            LaunchedEffect(expanded) {
                if (expanded) {
                    viewModel.loadMetadataForGroup(item)
                }
            }

            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (item.isMetadataLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentPrimary, modifier = Modifier.size(24.dp))
                    }
                }
                item.group.files.forEach { file ->
                    val isSelected = uiState.filesSelectedForDeletion.contains(file)
                    val isRecommended = file == item.recommendedToKeep
                    val isPlaying = uiState.currentlyPlayingPath == file.uri
                    
                    FileItemCard(
                        file = file,
                        item = item,
                        isSelected = isSelected,
                        isRecommended = isRecommended,
                        isPlaying = isPlaying,
                        onToggleSelect = { viewModel.selectionManager.toggleSelection(file, item) },
                        onTogglePlay = {
                            if (isPlaying) viewModel.audioPlayer.pause()
                            else viewModel.audioPlayer.play(file.uri)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FileItemCard(
    file: AudioFile,
    item: DuplicateReviewItem,
    isSelected: Boolean,
    isRecommended: Boolean,
    isPlaying: Boolean,
    onToggleSelect: () -> Unit,
    onTogglePlay: () -> Unit
) {
    val meta = item.fileMetadata?.get(file)
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(Date(file.dateModifiedMs))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, if (isSelected) AccentDelete else if (isRecommended) AccentPrimary else BorderColor, RoundedCornerShape(16.dp))
            .background(if (isSelected) AccentDelete.copy(alpha = 0.1f) else Background)
            .clickable { onToggleSelect() }
            .padding(12.dp)
    ) {
        if (isRecommended) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Icon(Icons.Default.Star, contentDescription = "Recommended", tint = AccentPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Recommended to Keep", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentPrimary)
            }
            Text("Recommended because:\n• ${item.recommendationReasons[file]?.joinToString("\n• ")}", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Surface2)
                    .size(40.dp)
            ) {
                Icon(if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow, contentDescription = "Play/Pause", tint = AccentPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(file.displayName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1)
                if (meta != null) {
                    Text("${meta.album} • ${meta.bitrate} kbps", fontSize = 12.sp, color = TextSecondary)
                } else {
                    Text("Loading metadata...", fontSize = 12.sp, color = TextSecondary)
                }
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(checkedColor = AccentDelete, uncheckedColor = TextSecondary)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
            Box(modifier = Modifier.background(Surface3, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📁", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${file.relativePath}/", fontSize = 10.sp, color = TextSecondary)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(dateStr, fontSize = 10.sp, color = TextSecondary)
            if (meta != null) {
                Text(meta.mimeType, fontSize = 10.sp, color = TextSecondary)
            }
            Text(file.sizeBytes.formatSize(), fontSize = 10.sp, color = TextSecondary)
            Text(file.durationMs.formatDuration(), fontSize = 10.sp, color = TextSecondary)
        }
    }
}
