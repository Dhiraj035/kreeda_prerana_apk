package com.example.kreedaprerana.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kreedaprerana.ui.theme.*
import com.example.kreedaprerana.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel
) {
    val stats by viewModel.stats.collectAsState()
    val chartPoints by viewModel.chartPoints.collectAsState()
    val chartPointsSecondary by viewModel.chartPointsSecondary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedTimeFilter by remember { mutableStateOf("30D") }
    val tabs = listOf("Talent Curve", "Performance", "Trends")
    val timeFilters = listOf("7D", "30D", "90D", "1Y")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.SemiBold) },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tabs
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    tabs.forEachIndexed { index, label ->
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
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Time filters
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeFilters.forEach { filter ->
                        val selected = filter == selectedTimeFilter
                        FilterChip(
                            selected = selected,
                            onClick = { selectedTimeFilter = filter },
                            label = { Text(filter, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.outlineVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(borderColor = Color.Transparent, selectedBorderColor = MaterialTheme.colorScheme.primary, enabled = true, selected = selected)
                        )
                    }
                }

                // Chart Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Talent Curve (Overall)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val primaryPoints = chartPoints
                        val lineColor = MaterialTheme.colorScheme.primary
                        val gridColor = MaterialTheme.colorScheme.outlineVariant

                        // Y-axis labels + chart
                        Row(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                            // Y-axis
                            Column(
                                modifier = Modifier.fillMaxHeight().width(32.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("100", "75", "50", "25", "0").forEach {
                                    Text(it, style = MaterialTheme.typography.labelSmall, color = (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)))
                                }
                            }

                            val outlineColor = MaterialTheme.colorScheme.outline
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Grid
                                for (i in 0..4) {
                                    val y = h * i / 4
                                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                                }

                                if (primaryPoints.isNotEmpty()) {
                                    val points = primaryPoints.mapIndexed { index, value ->
                                        val x = if (primaryPoints.size == 1) w / 2f else w * index / (primaryPoints.size - 1)
                                        val y = h * (1f - value) * 0.9f + h * 0.05f
                                        Offset(x, y)
                                    }

                                    if (points.size >= 2) {
                                        val path = Path().apply {
                                            moveTo(points.first().x, points.first().y)
                                            for (i in 1 until points.size) {
                                                val prev = points[i - 1]
                                                val curr = points[i]
                                                val cx = (prev.x + curr.x) / 2
                                                cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                                            }
                                        }
                                        drawPath(path, lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
                                    }

                                    points.forEach { pt ->
                                        drawCircle(lineColor, radius = 5f, center = pt)
                                        drawCircle(Color.White, radius = 2.5f, center = pt)
                                    }
                                } else {
                                    // Placeholder
                                    val placeholders = listOf(0.2f, 0.35f, 0.3f, 0.55f, 0.7f, 0.65f, 0.8f)
                                    val pts = placeholders.mapIndexed { i, v ->
                                        Offset(w * i / (placeholders.size - 1), h * (1f - v) * 0.9f + h * 0.05f)
                                    }
                                    val path = Path().apply {
                                        moveTo(pts.first().x, pts.first().y)
                                        for (i in 1 until pts.size) {
                                            val prev = pts[i - 1]; val curr = pts[i]
                                            val cx = (prev.x + curr.x) / 2
                                            cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                                        }
                                    }
                                    drawPath(path, outlineColor, style = Stroke(width = 2f, cap = StrokeCap.Round))
                                }
                            }
                        }

                        // X-axis labels
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 32.dp, top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("May 1", "May 8", "May 15", "May 22", "May 29").forEach {
                                Text(it, style = MaterialTheme.typography.labelSmall, color = (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Performance Summary
                Text(
                    "Performance Summary",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(Modifier.weight(1f), "Avg. Time", stats.avgImprovement, "↓ 2.5%", Success)
                    SummaryCard(Modifier.weight(1f), "Best Time", stats.topScore, "↑ 5.3%", Error)
                    SummaryCard(Modifier.weight(1f), "Participants", stats.athleteCount.toString(), "↑ 12.8%", MaterialTheme.colorScheme.primary)
                }

                // Top Performer
                Text(
                    "Top Performer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(Blue100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Top Athlete", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                            Text(stats.topScoreLabel.ifBlank { "100m Sprint" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            stats.topScore,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    title: String,
    value: String,
    trend: String,
    trendColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(trend, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = trendColor)
        }
    }
}
