package com.example.examprep.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.examprep.R
import com.example.examprep.model.Course
import com.example.examprep.ui.theme.*
import com.example.examprep.viewmodel.CourseViewModel

class AnalyticsActivity : ComponentActivity() {

    private lateinit var viewModel: CourseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check network connectivity - ONLINE ONLY feature
        if (!isNetworkAvailable()) {
            Toast.makeText(this, R.string.online_only, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[CourseViewModel::class.java]

        // Trigger the load once in onCreate
        viewModel.loadAllCoursesForAnalysis()

        setContent {
            CourseManagerTheme {
                AnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }

        // Collect errors
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { error ->
                    error?.let {
                        Toast.makeText(this@AnalyticsActivity, it, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: CourseViewModel,
    onNavigateBack: () -> Unit
) {
    val allCourses by viewModel.allCoursesForAnalysis.collectAsStateWithLifecycle()

    val top5Courses = remember(allCourses) {
        allCourses
            .sortedWith(compareBy<Course> { it.status } // alphabetical: completed < ongoing < upcoming
                .thenByDescending { it.students })      // most students first
            .take(5)
    }

    LaunchedEffect(allCourses) {
        Log.d("AnalyticsActivity", "Received ${allCourses.size} courses for analysis")
        Log.d("AnalyticsActivity", "Top 5 courses calculated: ${top5Courses.size}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics - Top Courses") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (allCourses.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary
                )
            } else if (top5Courses.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📊",
                        fontSize = 64.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No courses found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Check your connection and try again",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(top5Courses) { index, course ->
                        TopCourseItem(
                            course = course,
                            rank = index + 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopCourseItem(course: Course, rank: Int) {
    val rankText = when (rank) {
        1 -> "1st"
        2 -> "2nd"
        3 -> "3rd"
        4 -> "4th"
        5 -> "5th"
        else -> "${rank}th"
    }

    val rankColor = when (rank) {
        1 -> RankGold // Gold
        2 -> RankSilver // Silver
        3 -> RankBronze // Bronze
        else -> Primary
    }

    val statusColor = when (course.status.lowercase()) {
        "ongoing" -> StatusOngoing
        "upcoming" -> StatusUpcoming
        "completed" -> StatusCompleted
        else -> Primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Surface(
                shape = CircleShape,
                color = rankColor,
                modifier = Modifier.size(52.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = rankText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Course Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = course.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = course.instructor,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusColor
            ) {
                Text(
                    text = course.status.uppercase(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Students and Duration row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${course.students} students",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Text(
                text = "${course.duration} hours",
                fontSize = 16.sp,
                color = TextPrimary
            )
        }
    }
}
