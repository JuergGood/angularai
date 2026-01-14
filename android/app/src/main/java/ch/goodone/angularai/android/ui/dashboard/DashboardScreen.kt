package ch.goodone.angularai.android.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.goodone.angularai.android.data.remote.dto.DashboardDTO
import ch.goodone.angularai.android.data.remote.dto.SummaryStatsDTO

@Composable
fun DashboardScreen(
    onNavigateToTasks: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToUsers: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.error != null) {
            Text(text = state.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
        } else {
            state.dashboard?.let { dashboard ->
                DashboardContent(dashboard, onNavigateToTasks, onNavigateToLogs, onNavigateToUsers)
            }
        }
    }
}

@Composable
fun DashboardContent(
    dashboard: DashboardDTO,
    onNavigateToTasks: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToUsers: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Summary Stats (Horizontal scroll)
        SummaryStatsRow(dashboard.summary, onNavigateToTasks, onNavigateToUsers, onNavigateToLogs)
        Spacer(modifier = Modifier.height(24.dp))

        // Task Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToTasks
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Task Overview", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                TaskOverviewChart(dashboard.taskDistribution)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Recent Activity
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToLogs
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Activity", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onNavigateToLogs) {
                        Text("Show All")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                dashboard.recentActivity.forEach { log ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(text = log.timestamp, fontSize = 12.sp, color = Color.Gray)
                        Text(text = "${log.login}: ${log.action}")
                        Divider(modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Priority Tasks
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToTasks
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Priority Tasks", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onNavigateToTasks) {
                        Text("Show All")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                dashboard.priorityTasks.forEach { task ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = task.title, fontWeight = FontWeight.Bold)
                            Text(text = "Due: ${task.dueDate ?: "N/A"}", fontSize = 12.sp)
                        }
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
fun SummaryStatsRow(
    summary: SummaryStatsDTO,
    onNavigateToTasks: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToLogs: () -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        item { StatCard("Open Tasks", summary.openTasks.toString(), "+${summary.openTasksDelta}", onNavigateToTasks) }
        item { StatCard("Active Users", summary.activeUsers.toString(), "+${summary.activeUsersDelta}", onNavigateToUsers) }
        item { StatCard("Completed", summary.completedTasks.toString(), "+${summary.completedTasksDelta}", onNavigateToTasks) }
        item { StatCard("Today Logs", summary.todayLogs.toString(), "+${summary.todayLogsDelta}", onNavigateToLogs) }
    }
}

@Composable
fun StatCard(label: String, value: String, delta: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(140.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 12.sp)
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(delta, color = Color(0xFF4CAF50), fontSize = 12.sp)
        }
    }
}

@Composable
fun TaskOverviewChart(distribution: ch.goodone.angularai.android.data.remote.dto.TaskStatusDistributionDTO) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
        // Simple circle to represent donut
        Box(contentAlignment = Alignment.Center, modifier = Modifier
            .size(100.dp)
            .background(Color(0xFF3F51B5), CircleShape)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(distribution.total.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Total", color = Color.White, fontSize = 10.sp)
            }
        }
        
        Column {
            LegendItem(Color(0xFF3F51B5), "Open: ${distribution.open}")
            LegendItem(Color(0xFF2196F3), "In Progress: ${distribution.inProgress}")
            LegendItem(Color(0xFF4CAF50), "Completed: ${distribution.completed}")
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp)
    }
}
