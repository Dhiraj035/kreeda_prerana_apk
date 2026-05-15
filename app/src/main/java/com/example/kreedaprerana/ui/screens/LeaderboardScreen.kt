package com.example.kreedaprerana.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kreedaprerana.ui.theme.*
import com.example.kreedaprerana.viewmodel.LeaderboardEntry
import com.example.kreedaprerana.viewmodel.LeaderboardViewModel
import com.example.kreedaprerana.viewmodel.SportLeaderboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    viewModel: LeaderboardViewModel
) {
    val sportBoards by viewModel.sportLeaderboards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Derive available sport filters dynamically from data
    val availableSports = remember(sportBoards) {
        listOf("All") + sportBoards.map { it.activityType }
    }
    var selectedFilter by remember { mutableStateOf("All") }

    // Filter boards based on selection
    val filteredBoards = remember(sportBoards, selectedFilter) {
        if (selectedFilter == "All") sportBoards
        else sportBoards.filter { it.activityType == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            sportBoards.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No rankings yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Log some trials to see sport-wise leaderboards!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // ─── Filter Chips ───
                    item {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableSports.forEach { filter ->
                                val selected = filter == selectedFilter
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedFilter = filter },
                                    label = {
                                        Text(
                                            filter,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                                        enabled = true,
                                        selected = selected
                                    )
                                )
                            }
                        }
                    }

                    // ─── Sport Leaderboard Sections ───
                    items(filteredBoards) { board ->
                        SportLeaderboardSection(board = board)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Sport Leaderboard Section
// ══════════════════════════════════════════════════════════

@Composable
private fun SportLeaderboardSection(board: SportLeaderboard) {
    val ruleHint = if (board.isTimeBased) "Lowest time wins" else "Highest distance wins"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                board.activityType,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                ruleHint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Top 3 podium (if enough entries)
        if (board.entries.size >= 3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                PodiumCard(entry = board.entries[1], rank = 2, size = 72, medalColor = Silver, bgColor = SilverLight)
                PodiumCard(entry = board.entries[0], rank = 1, size = 90, medalColor = Gold, bgColor = GoldLight, showCrown = true)
                PodiumCard(entry = board.entries[2], rank = 3, size = 72, medalColor = Bronze, bgColor = BronzeLight)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Remaining entries below podium
            board.entries.drop(3).forEachIndexed { index, entry ->
                RankCard(rank = index + 4, entry = entry)
            }
        } else {
            // Less than 3 entries — show all as cards
            board.entries.forEachIndexed { index, entry ->
                val medalColor = when (index) {
                    0 -> Gold
                    1 -> Silver
                    else -> Bronze
                }
                RankCard(rank = index + 1, entry = entry, highlightColor = medalColor)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }
}

// ══════════════════════════════════════════════════════════
// Podium Card (Top 3)
// ══════════════════════════════════════════════════════════

@Composable
private fun PodiumCard(
    entry: LeaderboardEntry,
    rank: Int,
    size: Int,
    medalColor: Color,
    bgColor: Color,
    showCrown: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        if (showCrown) {
            Text(
                text = "1st",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Gold
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Avatar
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.name.take(2).uppercase(),
                fontSize = (size / 3).sp,
                fontWeight = FontWeight.Bold,
                color = medalColor
            )
        }

        // Rank badge
        Box(
            modifier = Modifier
                .offset(y = (-12).dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(medalColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(
            text = entry.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = entry.bestScore,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ══════════════════════════════════════════════════════════
// Rank Card (4th place and below, or any when < 3 entries)
// ══════════════════════════════════════════════════════════

@Composable
private fun RankCard(
    rank: Int,
    entry: LeaderboardEntry,
    highlightColor: Color? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Rank number / medal
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            highlightColor != null -> highlightColor.copy(alpha = 0.15f)
                            rank <= 3 -> Gold.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = highlightColor ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Blue100),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.name.take(2).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Name + sport
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    entry.sport,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Score + trend
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = entry.bestScore,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (entry.trend != "—") {
                    val trendColor = when {
                        entry.trend.startsWith("↑") -> Success
                        entry.trend.startsWith("↓") -> Error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = entry.trend,
                        style = MaterialTheme.typography.labelSmall,
                        color = trendColor
                    )
                }
            }
        }
    }
}
