package com.example.kreedaprerana.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kreedaprerana.data.model.Athlete
import com.example.kreedaprerana.ui.theme.*
import com.example.kreedaprerana.viewmodel.AthleteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthleteListScreen(
    onBack: () -> Unit,
    viewModel: AthleteViewModel,
    onAthleteClick: (String) -> Unit = {}
) {
    val athletes by viewModel.athletes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var filterExpanded by remember { mutableStateOf(false) }
    val filters = listOf(
        "All", "Cricket", "Football", "Athletics", "Kabaddi",
        "Kho-Kho", "Volleyball", "Basketball", "Hockey", "Badminton", "Swimming"
    )

    val filteredAthletes = athletes.filter { athlete ->
        val matchesSearch = searchQuery.isBlank() || athlete.name.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilter == "All" || athlete.sport.equals(selectedFilter, ignoreCase = true)
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Athletes", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { filterExpanded = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        DropdownMenu(
                            expanded = filterExpanded,
                            onDismissRequest = { filterExpanded = false }
                        ) {
                            filters.forEach { filter ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            filter,
                                            fontWeight = if (filter == selectedFilter) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    onClick = {
                                        selectedFilter = filter
                                        filterExpanded = false
                                    }
                                )
                            }
                        }
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
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search athletes...", color = (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            // Filter chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
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

            // Athlete count
            Text(
                text = "${filteredAthletes.size} athlete${if (filteredAthletes.size != 1) "s" else ""}" +
                        if (selectedFilter != "All") " in $selectedFilter" else "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            when {
                isLoading && athletes.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                filteredAthletes.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏃", style = MaterialTheme.typography.displayLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No athletes found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (athletes.isEmpty()) "Add your first athlete!" else "Try changing filters",
                                style = MaterialTheme.typography.bodySmall,
                                color = (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredAthletes, key = { it.athleteId }) { athlete ->
                            AthleteCard(
                                athlete = athlete,
                                onClick = { onAthleteClick(athlete.athleteId) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AthleteCard(athlete: Athlete, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(sportAvatarBg(athlete.sport)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = athlete.initials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = sportAvatarFg(athlete.sport)
                )
            }

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = athlete.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sport chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = sportChipBg(athlete.sport)
                    ) {
                        Text(
                            text = athlete.sport,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = sportChipFg(athlete.sport),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = "Age: ${athlete.age}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Gender icon & School
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = athlete.gender.ifBlank { "—" },
                    style = MaterialTheme.typography.labelSmall,
                    color = (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                )
                if (athlete.schoolName.isNotBlank()) {
                    Text(
                        text = athlete.schoolName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Sport-specific color helpers */
@Composable
private fun sportChipBg(sport: String): Color = when (sport.lowercase()) {
    "athletics" -> Color(0xFFDCFCE7)
    "kabaddi" -> Orange100
    "kho-kho" -> Color(0xFFFFF3E0)
    "football" -> Blue50
    "cricket" -> Color(0xFFE8F5E9)
    "volleyball" -> Color(0xFFEDE7F6)
    "basketball" -> Color(0xFFFCE4EC)
    "hockey" -> Color(0xFFE0F7FA)
    "badminton" -> Color(0xFFFFF8E1)
    "swimming" -> Color(0xFFE3F2FD)
    else -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun sportChipFg(sport: String): Color = when (sport.lowercase()) {
    "athletics" -> Success
    "kabaddi" -> Orange500
    "kho-kho" -> Color(0xFFE65100)
    "football" -> Blue600
    "cricket" -> Color(0xFF2E7D32)
    "volleyball" -> Color(0xFF5E35B1)
    "basketball" -> Color(0xFFC62828)
    "hockey" -> Color(0xFF00838F)
    "badminton" -> Color(0xFFF9A825)
    "swimming" -> Color(0xFF1565C0)
    else -> MaterialTheme.colorScheme.onSurface
}

@Composable
private fun sportAvatarBg(sport: String): Color = when (sport.lowercase()) {
    "cricket" -> Color(0xFFE8F5E9)
    "football" -> Blue100
    "athletics" -> Color(0xFFDCFCE7)
    "kabaddi" -> Orange100
    "volleyball" -> Color(0xFFEDE7F6)
    "basketball" -> Color(0xFFFCE4EC)
    else -> Blue100
}

@Composable
private fun sportAvatarFg(sport: String): Color = when (sport.lowercase()) {
    "cricket" -> Color(0xFF2E7D32)
    "football" -> Blue600
    "athletics" -> Success
    "kabaddi" -> Orange500
    "volleyball" -> Color(0xFF5E35B1)
    "basketball" -> Color(0xFFC62828)
    else -> MaterialTheme.colorScheme.primary
}
