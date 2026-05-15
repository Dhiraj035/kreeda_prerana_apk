package com.example.kreedaprerana.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kreedaprerana.data.model.Badge
import com.example.kreedaprerana.ui.theme.*
import com.example.kreedaprerana.viewmodel.BadgesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(
    onBack: () -> Unit,
    viewModel: BadgesViewModel? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val allBadges = viewModel?.badges?.collectAsState()?.value ?: emptyList()
    val isLoading = viewModel?.isLoading?.collectAsState()?.value ?: false

    // All possible badges for display (earned + upcoming)
    val allPossibleBadges = listOf(
        PossibleBadge("District Level Ready", "Awarded for outstanding performance at district level.", "🏅", listOf(MaterialTheme.colorScheme.primary, Blue700), "District"),
        PossibleBadge("State Level Potential", "For showing exceptional potential at state level.", "⭐", listOf(Success, Color(0xFF059669)), "State"),
        PossibleBadge("Rising Star", "For consistent improvement and dedication.", "🌟", listOf(MaterialTheme.colorScheme.secondary, Orange400), "Rising Star"),
        PossibleBadge("National Hope", "Keep performing to unlock this badge.", "🏆", listOf((MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)), MaterialTheme.colorScheme.onSurfaceVariant), "National")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements", fontWeight = FontWeight.SemiBold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Toggle tabs
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                listOf("All Badges", "Earned").forEachIndexed { index, label ->
                    val selected = index == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Color.White else Color.Transparent)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Earned badges section
                val earnedBadgeNames = allBadges.map { badge ->
                    // Match by prefix since badges like "District Level Ready - 100m Sprint" should match "District Level Ready"
                    allPossibleBadges.find { possible ->
                        badge.badgeName.startsWith(possible.title) || badge.level == possible.level
                    }?.title ?: badge.badgeName
                }.toSet()

                val earnedPossible = allPossibleBadges.filter { it.title in earnedBadgeNames }
                val unearnedPossible = allPossibleBadges.filter { it.title !in earnedBadgeNames }

                val displayBadges = when (selectedTab) {
                    0 -> allPossibleBadges // All Badges tab
                    else -> earnedPossible // Earned tab
                }

                displayBadges.forEach { possible ->
                    val isEarned = possible.title in earnedBadgeNames
                    val earnedBadge = allBadges.find { it.badgeName.startsWith(possible.title) || it.level == possible.level }
                    val dateStr = if (isEarned && earnedBadge != null) {
                        "Earned on ${formatDate(earnedBadge.earnedDate)}"
                    } else ""

                    BadgeCard(
                        emoji = possible.emoji,
                        title = possible.title,
                        description = possible.description,
                        date = dateStr,
                        gradientColors = if (isEarned) possible.gradientColors else listOf((MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)), MaterialTheme.colorScheme.onSurfaceVariant),
                        isEarned = isEarned
                    )
                }

                // Show "Upcoming" header when on All tab and there are unearned badges
                if (selectedTab == 0 && unearnedPossible.isNotEmpty() && earnedPossible.isNotEmpty()) {
                    // Already showing all — upcoming are grayed out automatically
                } else if (selectedTab == 1 && earnedPossible.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏅", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No badges earned yet", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Log trials to earn badges!", style = MaterialTheme.typography.bodySmall, color = (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

private data class PossibleBadge(
    val title: String,
    val description: String,
    val emoji: String,
    val gradientColors: List<Color>,
    val level: String
)

private fun formatDate(millis: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (e: Exception) {
        "—"
    }
}

@Composable
private fun BadgeCard(
    emoji: String,
    title: String,
    description: String,
    date: String,
    gradientColors: List<Color>,
    isEarned: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEarned) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Badge icon circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 26.sp)
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEarned) MaterialTheme.colorScheme.onBackground else (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEarned) MaterialTheme.colorScheme.onSurfaceVariant else (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                )
                if (date.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        date,
                        style = MaterialTheme.typography.labelSmall,
                        color = (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}
