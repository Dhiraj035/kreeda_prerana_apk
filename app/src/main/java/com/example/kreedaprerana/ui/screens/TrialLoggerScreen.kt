package com.example.kreedaprerana.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kreedaprerana.ui.theme.*
import com.example.kreedaprerana.util.StopwatchManager
import com.example.kreedaprerana.viewmodel.TrialLoggerViewModel

// ─── Activity type definitions for each tab ───

/** Stopwatch-based activities (sprint/timed) */
private val stopwatchActivityTypes = listOf(
    "100m Sprint", "200m Sprint", "400m Sprint",
    "Swimming", "Kabaddi"
)

/** Manual entry activities with their field configs */
private data class ManualActivityConfig(
    val name: String,
    val fieldLabel: String,
    val unit: String,
    val placeholder: String
)

private val manualActivityConfigs = listOf(
    ManualActivityConfig("Long Jump", "Jump Distance", "m", "5.7"),
    ManualActivityConfig("High Jump", "Jump Height", "m", "1.85"),
    ManualActivityConfig("Shot Put", "Throw Distance", "m", "12.4"),
    ManualActivityConfig("Discus Throw", "Throw Distance", "m", "35.0"),
    ManualActivityConfig("Javelin Throw", "Throw Distance", "m", "45.0"),
    ManualActivityConfig("Triple Jump", "Jump Distance", "m", "13.2"),
    ManualActivityConfig("Pole Vault", "Vault Height", "m", "3.5"),
    ManualActivityConfig("Distance Throw", "Throw Distance", "m", "20.0")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrialLoggerScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: TrialLoggerViewModel
) {
    val elapsedMillis by viewModel.stopwatch.elapsedMillis.collectAsState()
    val isRunning by viewModel.stopwatch.isRunning.collectAsState()
    val athletes by viewModel.athletes.collectAsState()
    val isLogging by viewModel.isLogging.collectAsState()
    val logSuccess by viewModel.logSuccess.collectAsState()
    val error by viewModel.error.collectAsState()
    val newBadges by viewModel.newBadges.collectAsState()

    // ─── Shared state ───
    var selectedAthleteId by remember { mutableStateOf("") }
    var selectedAthleteName by remember { mutableStateOf("") }
    var athleteDropdownExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    // ─── Stopwatch tab state ───
    var selectedStopwatchActivity by remember { mutableStateOf(stopwatchActivityTypes.first()) }
    var stopwatchActivityExpanded by remember { mutableStateOf(false) }
    var stopwatchDistance by remember { mutableStateOf("") }

    // ─── Manual entry tab state ───
    var selectedManualActivity by remember { mutableStateOf(manualActivityConfigs.first()) }
    var manualActivityExpanded by remember { mutableStateOf(false) }
    var manualValue by remember { mutableStateOf("") }
    var manualValueError by remember { mutableStateOf(false) }
    var athleteError by remember { mutableStateOf(false) }

    val displayTime = StopwatchManager.formatTime(elapsedMillis)
    val snackbarHostState = remember { SnackbarHostState() }

    // Animated ring progress
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val ringAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "ringRotation"
    )

    val context = LocalContext.current

    LaunchedEffect(logSuccess) {
        if (logSuccess) {
            val badgeMsg = if (newBadges.isNotEmpty()) "\n🏅 New badge: ${newBadges.first().badgeName}" else ""
            Toast.makeText(context, "Performance saved successfully!$badgeMsg", Toast.LENGTH_SHORT).show()
            viewModel.resetLogSuccess()
            viewModel.clearNewBadges()
            stopwatchDistance = ""; notes = ""; manualValue = ""
            viewModel.stopwatch.reset()
            onNavigateToHome()
        }
    }
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar("Error: $it"); viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trial Logger", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.onSurface) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─── Segmented Tabs ───
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf("Stopwatch", "Manual Entry").forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == index) Color.White else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // ══════════════════════════════════════════
                // STOPWATCH MODE
                // ══════════════════════════════════════════

                // Circular Stopwatch
                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
                    val primaryColor = MaterialTheme.colorScheme.primary

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = outlineVariantColor,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    if (isRunning) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = primaryColor,
                                startAngle = -90f + ringAngle,
                                sweepAngle = 120f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    } else if (elapsedMillis > 0) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = primaryColor,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = displayTime,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = selectedStopwatchActivity,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Control buttons
                Row(
                    modifier = Modifier.padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.stopwatch.reset() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Text("Reset", fontWeight = FontWeight.SemiBold)
                    }

                    if (isRunning) {
                        Button(
                            onClick = { viewModel.stopwatch.stop() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Error)
                        ) {
                            Text("Stop", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.stopwatch.start() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Start", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ─── Stopwatch form fields ───
                // Athlete dropdown
                AthleteDropdown(
                    selectedAthleteName = selectedAthleteName,
                    expanded = athleteDropdownExpanded,
                    onExpandedChange = { athleteDropdownExpanded = it },
                    athletes = athletes,
                    isError = athleteError,
                    onSelect = { id, name ->
                        selectedAthleteId = id
                        selectedAthleteName = name
                        athleteError = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Activity type
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Text("Activity Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    ExposedDropdownMenuBox(
                        expanded = stopwatchActivityExpanded,
                        onExpandedChange = { stopwatchActivityExpanded = !stopwatchActivityExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedStopwatchActivity,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(stopwatchActivityExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(expanded = stopwatchActivityExpanded, onDismissRequest = { stopwatchActivityExpanded = false }) {
                            stopwatchActivityTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = { selectedStopwatchActivity = type; stopwatchActivityExpanded = false }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Distance (optional for stopwatch)
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Text("Distance (Optional)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = stopwatchDistance,
                        onValueChange = { stopwatchDistance = it },
                        placeholder = { Text("100", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("m", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.padding(end = 8.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                NotesField(notes = notes, onValueChange = { notes = it })

                Spacer(modifier = Modifier.height(24.dp))

                // Save button (stopwatch)
                Button(
                    onClick = {
                        if (selectedAthleteId.isBlank()) {
                            athleteError = true
                            return@Button
                        }
                        val dist = stopwatchDistance.toDoubleOrNull() ?: 0.0
                        viewModel.logTrial(selectedAthleteId, elapsedMillis, dist, selectedStopwatchActivity, notes)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = selectedAthleteId.isNotBlank() && (elapsedMillis > 0 || stopwatchDistance.isNotBlank()) && !isLogging
                ) {
                    SaveButtonContent(isLogging = isLogging)
                }

            } else {
                // ══════════════════════════════════════════
                // MANUAL ENTRY MODE
                // ══════════════════════════════════════════

                // Header card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Manual Performance Entry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "For field events like jumps and throws",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ─── Athlete Selection ───
                AthleteDropdown(
                    selectedAthleteName = selectedAthleteName,
                    expanded = athleteDropdownExpanded,
                    onExpandedChange = { athleteDropdownExpanded = it },
                    athletes = athletes,
                    isError = athleteError,
                    onSelect = { id, name ->
                        selectedAthleteId = id
                        selectedAthleteName = name
                        athleteError = false
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ─── Activity Type Selection ───
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Text("Activity Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    ExposedDropdownMenuBox(
                        expanded = manualActivityExpanded,
                        onExpandedChange = { manualActivityExpanded = !manualActivityExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedManualActivity.name,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(manualActivityExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(expanded = manualActivityExpanded, onDismissRequest = { manualActivityExpanded = false }) {
                            manualActivityConfigs.forEach { config ->
                                DropdownMenuItem(
                                    text = { Text(config.name) },
                                    onClick = {
                                        selectedManualActivity = config
                                        manualActivityExpanded = false
                                        manualValue = ""
                                        manualValueError = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ─── Dynamic Performance Value Field ───
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Text(
                        selectedManualActivity.fieldLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = manualValue,
                        onValueChange = {
                            manualValue = it
                            manualValueError = false
                        },
                        placeholder = {
                            Text(
                                selectedManualActivity.placeholder,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = manualValueError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = {
                            Text(
                                selectedManualActivity.unit,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        },
                        supportingText = if (manualValueError) {
                            { Text("Please enter a valid numeric value", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ─── Notes ───
                NotesField(notes = notes, onValueChange = { notes = it })

                Spacer(modifier = Modifier.height(24.dp))

                // ─── Save Button (manual) ───
                Button(
                    onClick = {
                        // Validation
                        var hasError = false
                        if (selectedAthleteId.isBlank()) {
                            athleteError = true
                            hasError = true
                        }
                        val parsedValue = manualValue.toDoubleOrNull()
                        if (parsedValue == null || parsedValue <= 0) {
                            manualValueError = true
                            hasError = true
                        }
                        if (hasError) return@Button

                        // Save: sprintTimeMillis = 0 since this is not timed
                        // distance field stores the measurement value
                        viewModel.logTrial(
                            athleteId = selectedAthleteId,
                            sprintTimeMillis = 0L,
                            distance = parsedValue!!,
                            activityType = selectedManualActivity.name,
                            notes = notes
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = selectedAthleteId.isNotBlank() && manualValue.isNotBlank() && !isLogging
                ) {
                    SaveButtonContent(isLogging = isLogging)
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════
// Reusable composables extracted to avoid duplication
// ══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AthleteDropdown(
    selectedAthleteName: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    athletes: List<com.example.kreedaprerana.data.model.Athlete>,
    isError: Boolean,
    onSelect: (id: String, name: String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { onExpandedChange(!expanded) }
        ) {
            OutlinedTextField(
                value = selectedAthleteName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Athlete") },
                placeholder = { Text("Choose an athlete", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                isError = isError,
                supportingText = if (isError) {
                    { Text("Please select an athlete", color = MaterialTheme.colorScheme.error) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                athletes.forEach { athlete ->
                    DropdownMenuItem(
                        text = { Text("${athlete.name} (${athlete.sport})") },
                        onClick = {
                            onSelect(athlete.athleteId, athlete.name)
                            onExpandedChange(false)
                        }
                    )
                }
                if (athletes.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No athletes found", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        onClick = { onExpandedChange(false) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesField(notes: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Text("Notes (Optional)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = onValueChange,
            placeholder = { Text("Enter notes", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )
    }
}

@Composable
private fun SaveButtonContent(isLogging: Boolean) {
    if (isLogging) {
        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Saving...", fontWeight = FontWeight.SemiBold)
    } else {
        Text("Save Performance", fontWeight = FontWeight.SemiBold, fontSize = MaterialTheme.typography.titleMedium.fontSize)
    }
}
