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
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kreedaprerana.data.model.Performance
import com.example.kreedaprerana.ui.theme.*
import com.example.kreedaprerana.viewmodel.AthleteProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthleteStatsScreen(
    onBack: () -> Unit,
    viewModel: AthleteProfileViewModel? = null
) {
    val athlete = viewModel?.athlete?.collectAsState()?.value
    val performances = viewModel?.performances?.collectAsState()?.value ?: emptyList()
    val isLoading = viewModel?.isLoading?.collectAsState()?.value ?: false

    val displayName = athlete?.name ?: "Athlete"

    // Separate sprint and distance performances
    val sprintPerfs = performances.filter { it.sprintTime > 0 }.sortedBy { it.date }
    val distancePerfs = performances.filter { it.distance > 0 }.sortedBy { it.date }

    // Compute improvement analysis
    val sprintImprovement = computeImprovement(sprintPerfs.map { it.sprintTime.toFloat() }, lowerIsBetter = true)
    val distanceImprovement = computeImprovement(distancePerfs.map { it.distance.toFloat() }, lowerIsBetter = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "$displayName — Analytics",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                },
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
        if (isLoading && athlete == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ─── Top Profile Section ───
                athlete?.let { a ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(a.initials, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    a.name, 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Sport: ${a.sport.ifBlank { "—" }} • Age: ${if (a.age > 0) "${a.age}" else "—"}",
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "School: ${a.schoolName.ifBlank { "—" }}",
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // ─── Summary Statistics Cards ───
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Total Trials",
                        value = "${performances.size}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Best Sprint",
                        value = viewModel?.getBestSprint() ?: "—",
                        color = Success,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Events",
                        value = viewModel?.getEventTypes() ?: "—",
                        color = Orange500,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ─── Improvement Analysis ───
                if (sprintImprovement != null || distanceImprovement != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Performance Improvement",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            sprintImprovement?.let { imp ->
                                ImprovementRow(
                                    label = "Sprint Time Analysis",
                                    percentChange = imp,
                                    improved = imp < 0 // lower is better for sprint
                                )
                            }
                            if (sprintImprovement != null && distanceImprovement != null) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                            distanceImprovement?.let { imp ->
                                ImprovementRow(
                                    label = "Distance Analysis",
                                    percentChange = imp,
                                    improved = imp > 0 // higher is better for distance
                                )
                            }
                        }
                    }
                }

                // ─── Sprint Time Line Graph ───
                if (sprintPerfs.isNotEmpty()) {
                    LineGraphSection(
                        title = "Sprint Time Progression",
                        subtitle = "Time in seconds (lower is better)",
                        data = sprintPerfs.takeLast(10), // Limit to last 10 for clean fit
                        valueExtractor = { it.sprintTime.toFloat() },
                        unit = "s",
                        lineColor = MaterialTheme.colorScheme.primary
                    )
                }

                // ─── Distance Line Graph ───
                if (distancePerfs.isNotEmpty()) {
                    LineGraphSection(
                        title = "Distance Progression",
                        subtitle = "Distance in meters (higher is better)",
                        data = distancePerfs.takeLast(10), // Limit to last 10 for clean fit
                        valueExtractor = { it.distance.toFloat() },
                        unit = "m",
                        lineColor = Success
                    )
                }

                // ─── Performance History ───
                Text(
                    "Performance History",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (performances.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No trial records available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    performances.sortedByDescending { it.date }.forEachIndexed { index, perf ->
                        TrialRecordRow(index = performances.size - index, performance = perf)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ImprovementRow(label: String, percentChange: Float, improved: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (improved) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
            contentDescription = null,
            tint = if (improved) Success else Error,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${if (percentChange > 0) "+" else ""}${"%.1f".format(percentChange)}%",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (improved) Success else Error
        )
    }
}

@Composable
private fun LineGraphSection(
    title: String,
    subtitle: String,
    data: List<Performance>,
    valueExtractor: (Performance) -> Float,
    unit: String,
    lineColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val values = data.map { valueExtractor(it) }
            val labels = data.map { formatStatsDate(it.date) }
            val maxVal = values.maxOrNull() ?: 0f
            
            val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            val axisColor = Color.Black
            val gridColor = Color(0xFFE2E8F0)
            val textMeasurer = rememberTextMeasurer()

            val yAxisWidth = 36f
            val xAxisHeight = 32f

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                val totalWidth = size.width
                val totalHeight = size.height
                val chartLeft = yAxisWidth.dp.toPx()
                val chartBottom = totalHeight - xAxisHeight.dp.toPx()
                val chartTop = 16f
                val chartRight = totalWidth - 8f
                val chartW = chartRight - chartLeft
                val chartH = chartBottom - chartTop

                val pointCount = values.size
                val spacing = if (pointCount > 1) chartW / pointCount else chartW

                // ─── Grid lines & Y Axis Labels ───
                for (i in 0..4) {
                    val y = chartTop + chartH * i / 4
                    drawLine(
                        color = gridColor,
                        start = Offset(chartLeft, y),
                        end = Offset(chartRight, y),
                        strokeWidth = 1f
                    )

                    val yVal = maxVal * (4 - i) / 4
                    val yLabel = if (unit == "s") "%.1f".format(yVal) else "%.0f".format(yVal)
                    val yTextLayout = textMeasurer.measure(
                        text = yLabel,
                        style = TextStyle(fontSize = 9.sp, color = labelColor)
                    )
                    drawText(
                        yTextLayout,
                        topLeft = Offset(
                            chartLeft - yTextLayout.size.width - 6f,
                            y - yTextLayout.size.height / 2
                        )
                    )
                }

                // ─── Y-axis (black) ───
                drawLine(
                    color = axisColor,
                    start = Offset(chartLeft, chartTop),
                    end = Offset(chartLeft, chartBottom),
                    strokeWidth = 2f
                )

                // ─── X-axis (black) ───
                drawLine(
                    color = axisColor,
                    start = Offset(chartLeft, chartBottom),
                    end = Offset(chartRight, chartBottom),
                    strokeWidth = 2f
                )

                // ─── Line Graph ───
                val pointCoords = mutableListOf<Offset>()
                
                values.forEachIndexed { index, value ->
                    val barHeight = if (maxVal > 0) (value / maxVal) * chartH else 0f
                    val y = chartBottom - barHeight
                    val x = chartLeft + spacing * index + spacing / 2
                    pointCoords.add(Offset(x, y))
                    
                    // X-axis date label
                    val dateLayout = textMeasurer.measure(
                        text = labels[index],
                        style = TextStyle(fontSize = 9.sp, color = labelColor)
                    )
                    drawText(
                        dateLayout,
                        topLeft = Offset(
                            x - dateLayout.size.width / 2,
                            chartBottom + 8f
                        )
                    )
                    
                    // Value label above point
                    val valueText = if (unit == "s") "%.1f".format(value) else "%.0f".format(value)
                    val valLayout = textMeasurer.measure(
                        text = valueText,
                        style = TextStyle(fontSize = 9.sp, color = labelColor)
                    )
                    drawText(
                        valLayout,
                        topLeft = Offset(
                            x - valLayout.size.width / 2,
                            (y - valLayout.size.height - 8f).coerceAtLeast(chartTop)
                        )
                    )
                }

                if (pointCoords.isNotEmpty()) {
                    val path = androidx.compose.ui.graphics.Path()
                    path.moveTo(pointCoords.first().x, pointCoords.first().y)
                    
                    // Smooth curve
                    for (i in 0 until pointCoords.size - 1) {
                        val current = pointCoords[i]
                        val next = pointCoords[i + 1]
                        val controlX = (current.x + next.x) / 2
                        path.cubicTo(controlX, current.y, controlX, next.y, next.x, next.y)
                    }
                    
                    // Draw smooth line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 3.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                    
                    // Draw points
                    pointCoords.forEach { point ->
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = point,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrialRecordRow(index: Int, performance: Performance) {
    val score = when {
        performance.sprintTime > 0 -> "%.2f s".format(performance.sprintTime)
        performance.distance > 0 -> "%.1f m".format(performance.distance)
        else -> "—"
    }
    val dateStr = formatStatsDate(performance.date)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trial number
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#$index",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    performance.activityType.ifBlank { "Trial" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (performance.notes.isNotBlank()) {
                    Text(
                        performance.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    score,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatStatsDate(millis: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (e: Exception) {
        "—"
    }
}

private fun computeImprovement(values: List<Float>, lowerIsBetter: Boolean): Float? {
    if (values.size < 2) return null
    val first = values.first()
    val last = values.last()
    if (first == 0f) return null
    return ((last - first) / first) * 100f
}
