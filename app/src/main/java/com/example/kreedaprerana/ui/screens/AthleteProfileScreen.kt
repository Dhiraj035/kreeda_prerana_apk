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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kreedaprerana.ui.theme.*
import com.example.kreedaprerana.viewmodel.AthleteProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthleteProfileScreen(
    onBack: () -> Unit,
    viewModel: AthleteProfileViewModel? = null,
    onViewFullStats: () -> Unit = {}
) {
    val athlete = viewModel?.athlete?.collectAsState()?.value
    val performances = viewModel?.performances?.collectAsState()?.value ?: emptyList()
    val isLoading = viewModel?.isLoading?.collectAsState()?.value ?: false

    val displayName = athlete?.name ?: "Loading..."
    val displaySport = athlete?.sport ?: "—"
    val displayAge = athlete?.age ?: 0
    val displayGender = athlete?.gender ?: "—"
    val displaySchool = athlete?.schoolName ?: "—"
    val bestSprint = viewModel?.getBestSprint() ?: "—"
    val eventTypes = viewModel?.getEventTypes() ?: "—"
    val totalTrials = performances.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                    .verticalScroll(rememberScrollState())
            ) {
                // ─── Blue gradient header ───
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Brush.verticalGradient(listOf(Blue700, MaterialTheme.colorScheme.primary))),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (athlete != null) {
                                Text(
                                    athlete.initials,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(44.dp),
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(displayName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                displaySport,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // ─── Stats card overlay ───
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .offset(y = (-24).dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("Age", if (displayAge > 0) "$displayAge" else "—")
                        StatItem("Trials", "$totalTrials")
                        StatItem("Events", eventTypes)
                        StatItem("Best", bestSprint)
                    }
                }

                // ─── Athlete Details Section (clean text, no icons/emojis) ───
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .offset(y = (-12).dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                        Text(
                            "Athlete Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailRow(label = "Name", value = displayName)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        DetailRow(label = "Sport", value = displaySport)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        DetailRow(label = "Age", value = if (displayAge > 0) "$displayAge years" else "—")
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        DetailRow(label = "Gender", value = displayGender)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        DetailRow(label = "School", value = displaySchool)
                    }
                }

                // ─── Performance History Section ───
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Performance History",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "${performances.size} records",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (performances.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No performances yet",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Log a trial to see history here",
                                style = MaterialTheme.typography.bodySmall,
                                color = (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            )
                        }
                    }
                } else {
                    performances.take(10).forEach { perf ->
                        val score = when {
                            perf.sprintTime > 0 -> "%.2f s".format(perf.sprintTime)
                            perf.distance > 0 -> "%.1f m".format(perf.distance)
                            else -> "—"
                        }
                        val dateStr = formatProfileDate(perf.date)
                        PerformanceHistoryCard(
                            event = perf.activityType.ifBlank { "Trial" },
                            score = score,
                            date = dateStr,
                            notes = perf.notes
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ─── Performance Overview Histogram ───
                if (performances.isNotEmpty()) {
                    Text(
                        "Performance Overview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        val barData = performances
                            .filter { it.sprintTime > 0 || it.distance > 0 }
                            .sortedBy { it.date }
                            .takeLast(8)

                        if (barData.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No numeric data to chart",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            val values = barData.map {
                                if (it.sprintTime > 0) it.sprintTime.toFloat()
                                else it.distance.toFloat()
                            }
                            val labels = barData.map { formatChartDate(it.date) }
                            val unit = if (barData.first().sprintTime > 0) "s" else "m"
                            val maxVal = values.max()

                            val barColor = MaterialTheme.colorScheme.primary
                            val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            val axisColor = Color.Black
                            val gridColor = Color(0xFFE2E8F0)
                            val textMeasurer = rememberTextMeasurer()

                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = if (unit == "s") "Sprint Time (seconds)" else "Distance (meters)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Y-axis padding left = 36dp for labels, bottom = 32dp for X labels
                                val yAxisWidth = 36f
                                val xAxisHeight = 32f

                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                ) {
                                    val totalWidth = size.width
                                    val totalHeight = size.height
                                    val chartLeft = yAxisWidth.dp.toPx()
                                    val chartBottom = totalHeight - xAxisHeight.dp.toPx()
                                    val chartTop = 16f
                                    val chartRight = totalWidth - 8f
                                    val chartW = chartRight - chartLeft
                                    val chartH = chartBottom - chartTop

                                    val barCount = values.size
                                    val barWidthPx = (chartW / barCount) * 0.4f
                                    val spacing = chartW / barCount

                                    // ─── Grid lines ───
                                    for (i in 0..4) {
                                        val y = chartTop + chartH * i / 4
                                        drawLine(
                                            color = gridColor,
                                            start = Offset(chartLeft, y),
                                            end = Offset(chartRight, y),
                                            strokeWidth = 1f
                                        )

                                        // Y-axis label
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

                                    // ─── Y-axis (black, left) ───
                                    drawLine(
                                        color = axisColor,
                                        start = Offset(chartLeft, chartTop),
                                        end = Offset(chartLeft, chartBottom),
                                        strokeWidth = 2f
                                    )

                                    // ─── X-axis (black, bottom) ───
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
                                            color = barColor,
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
                                                color = barColor,
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
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ─── Action buttons ───
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Text("Edit Profile", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onViewFullStats,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("View Full Stats", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

private fun formatProfileDate(millis: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (e: Exception) {
        "—"
    }
}

private fun formatChartDate(millis: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (e: Exception) {
        "—"
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
    }
}

/**
 * Clean text-only detail row — no icons, no emojis.
 * Uses a fixed-width label for alignment symmetry.
 */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PerformanceHistoryCard(event: String, score: String, date: String, notes: String = "") {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timeline dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (notes.isNotBlank()) {
                    Text(
                        notes,
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
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    date,
                    style = MaterialTheme.typography.labelSmall,
                    color = (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                )
            }
        }
    }
}
