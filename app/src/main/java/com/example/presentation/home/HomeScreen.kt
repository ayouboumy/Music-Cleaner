package com.example.presentation.home

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.presentation.viewmodel.HomeViewModel
import com.example.presentation.uiState.HomeUiState
import com.example.domain.models.AudioFile
import com.example.common.extensions.formatSize
import com.example.common.extensions.formatDuration
import com.example.ui.theme.*

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    val context = LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_AUDIO
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanFiles()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.scanFiles()
        } else {
            permissionLauncher.launch(permission)
        }
    }
    
    if (uiState.showReviewScreen) {
        com.example.presentation.review.DuplicateReviewScreen(
            exactDuplicateGroups = uiState.exactDuplicateGroups,
            onBack = { viewModel.closeReview() }
        )
    } else {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                BottomActionBar(
                    uiState = uiState,
                    onScanClick = { viewModel.scanFiles() }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AppBar()
                BentoStatsSection(uiState)
                
                if (uiState.exactGroupsFound > 0 && !uiState.isScanning && !uiState.isHashing) {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Button(
                            onClick = { viewModel.openReview() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                        ) {
                            Text("Review Duplicates", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Background)
                        }
                    }
                }
                
                SearchFilterTab()
                ExactDuplicateGroupsList(uiState)
            }
        }
    }
}

@Composable
fun AppBar() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(BorderColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Rounded.Menu, contentDescription = "Menu", tint = AccentPrimary)
      }
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = "Music Cleaner",
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
      )
    }
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(BorderColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Rounded.Search, contentDescription = "Search", tint = AccentPrimary)
    }
  }
}

@Composable
fun BentoStatsSection(uiState: HomeUiState) {
  val context = LocalContext.current
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().height(140.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1: Exact Duplicates
        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(24.dp)).background(Surface1).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Box(
                  modifier = Modifier.size(32.dp).clip(CircleShape).background(BorderColor),
                  contentAlignment = Alignment.Center
                ) {
                  Text("🎵", fontSize = 16.sp)
                }
                Column {
                    Text(text = "${uiState.exactDuplicatesCount}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AccentPrimary)
                    Text(text = "EXACT DUPLICATES", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                }
            }
        }
        // Card 2: Candidate Groups -> Exact Groups
        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(24.dp)).background(Surface2).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Box(
                  modifier = Modifier.size(32.dp).clip(CircleShape).background(BorderColor),
                  contentAlignment = Alignment.Center
                ) {
                  Text("📁", fontSize = 16.sp)
                }
                Column {
                    Text(text = "${uiState.exactGroupsFound}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AccentSecondary)
                    Text(text = "GROUPS FOUND", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                }
            }
        }
    }
    Row(
      modifier = Modifier.fillMaxWidth().height(140.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 3: Candidate Files -> Reclaimable Size
        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(24.dp)).background(Surface3).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Box(
                  modifier = Modifier.size(32.dp).clip(CircleShape).background(BorderColor),
                  contentAlignment = Alignment.Center
                ) {
                  Text("💾", fontSize = 16.sp)
                }
                Column {
                    Text(text = uiState.reclaimableSize.formatSize(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AccentPrimary)
                    Text(text = "RECLAIMABLE", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                }
            }
        }
        // Card 4: Est. to Hash -> Total Candidates
        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(24.dp)).background(Background).border(1.dp, BorderColor, RoundedCornerShape(24.dp)).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Box(
                  modifier = Modifier.size(32.dp).clip(CircleShape).background(BorderColor),
                  contentAlignment = Alignment.Center
                ) {
                  Text("📄", fontSize = 16.sp)
                }
                Column {
                    Text(text = "${uiState.candidateFilesCount}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "FILES HASHED", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                }
            }
        }
    }
    
    // Status text below cards
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp, start = 8.dp)) {
        if (uiState.hasPermissionError) {
             Text(text = "PERMISSION DENIED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentDelete)
             Spacer(modifier = Modifier.width(8.dp))
             TextButton(
                 onClick = {
                     val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                         data = android.net.Uri.fromParts("package", context.packageName, null)
                     }
                     context.startActivity(intent)
                 },
                 contentPadding = PaddingValues(0.dp)
             ) {
                 Text("Open Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentPrimary)
             }
        } else if (uiState.isScanning) {
             Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(AccentPrimary)
             )
             Spacer(modifier = Modifier.width(8.dp))
             Text(text = "Scanning Storage... ${uiState.scanProgress}%", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AccentPrimary)
        } else if (uiState.isHashing) {
             Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(AccentSecondary)
             )
             Spacer(modifier = Modifier.width(8.dp))
             Text(text = "Computing SHA-256... ${uiState.hashProgress}%", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AccentSecondary)
        } else {
             Text(text = "Scan Complete", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
        }
    }
  }
}

@Composable
fun SearchFilterTab() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(BorderColor.copy(alpha = 0.3f))
      .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
      .padding(4.dp)
  ) {
    Box(
      modifier = Modifier
        .weight(1f)
        .clip(RoundedCornerShape(12.dp))
        .background(BorderColor)
        .padding(vertical = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Text("Exact Matches", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
    Box(
      modifier = Modifier
        .weight(1f)
        .padding(vertical = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Text("Similar Metadata", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
    }
  }
}

@Composable
fun ExactDuplicateGroupsList(uiState: HomeUiState) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(uiState.exactDuplicateGroups) { group ->
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(24.dp))
          .background(Surface2)
          .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(AccentPrimary, Surface3))),
              contentAlignment = Alignment.Center
            ) {
              Text("🎵", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(group.files.first().title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1)
              Text("${group.files.first().artist} • ${group.sizeBytes.formatSize()} • ${group.files.first().durationMs.formatDuration()}", fontSize = 12.sp, color = TextSecondary, maxLines = 1)
            }
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(BorderColor)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text("${group.files.size} EXACT COPIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentPrimary)
          }
        }
      }
    }
  }
}

@Composable
fun BottomActionBar(uiState: HomeUiState, onScanClick: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(Background)
      .border(1.dp, BorderColor)
      .padding(16.dp)
  ) {
    Button(
      onClick = onScanClick,
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp),
      colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
      shape = RoundedCornerShape(28.dp),
      enabled = !uiState.isScanning && !uiState.isHashing
    ) {
      Text(if (uiState.isScanning || uiState.isHashing) "Scanning..." else "Rescan Library", color = Surface3, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
  }
}
