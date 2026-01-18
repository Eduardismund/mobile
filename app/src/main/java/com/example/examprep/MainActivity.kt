package com.example.examprep

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.example.examprep.model.Course
import com.example.examprep.ui.AddEditCourseActivity
import com.example.examprep.ui.AnalyticsActivity
import com.example.examprep.ui.CourseDetailActivity
import com.example.examprep.ui.StudentActivity
import com.example.examprep.ui.theme.*
import com.example.examprep.viewmodel.CourseViewModel
import com.example.examprep.websocket.CourseWebSocketClient
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: CourseViewModel
    private var webSocketClient: CourseWebSocketClient? = null
    private var hasLoadedSuccessfully = false

    private val addCourseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("MainActivity", "Returned from AddEditCourseActivity with success")
            // No need to reload - WebSocket will notify and update the list automatically
            // If offline, the course is already cached and will appear on next app restart
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate")

        viewModel = ViewModelProvider(this)[CourseViewModel::class.java]

        // Load courses on initial load
        viewModel.loadCourses()

        setupWebSocket()

        setContent {
            CourseManagerTheme {
                MainScreen(
                    viewModel = viewModel,
                    onAddCourse = {
                        addCourseLauncher.launch(Intent(this, AddEditCourseActivity::class.java))
                    },
                    onNavigateToStudent = {
                        startActivity(Intent(this, StudentActivity::class.java))
                    },
                    onNavigateToAnalytics = {
                        startActivity(Intent(this, AnalyticsActivity::class.java))
                    },
                    onCourseClick = { course ->
                        val intent = Intent(this, CourseDetailActivity::class.java)
                        intent.putExtra("COURSE_ID", course.id)
                        startActivity(intent)
                    },
                    onDeleteCourse = { course ->
                        viewModel.deleteCourse(course.id)
                    },
                    onRetry = {
                        viewModel.loadCourses()
                    }
                )
            }
        }

        // Collect StateFlows using lifecycleScope
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect courses to track successful loads
                launch {
                    viewModel.courses.collect { courses ->
                        if (courses.isNotEmpty()) {
                            hasLoadedSuccessfully = true
                        }
                    }
                }

                // Collect error messages
                launch {
                    viewModel.errorMessage.collect { errorMessage ->
                        errorMessage?.let {
                            // Only show error toast if we have no cached data to display
                            val currentCourses = viewModel.courses.value
                            if (currentCourses.isEmpty()) {
                                Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                            } else {
                                // We have cached data, just log the error
                                Log.d("MainActivity", "Error loading from network, showing cached data: $it")
                            }
                            viewModel.clearError()
                        }
                    }
                }

                // Collect operation success
                launch {
                    viewModel.operationSuccess.collect { success ->
                        if (success) {
                            Toast.makeText(this@MainActivity, "Course deleted successfully", Toast.LENGTH_SHORT).show()
                            viewModel.resetOperationSuccess()
                        }
                    }
                }
            }
        }
    }

    private fun setupWebSocket() {
        Log.d("MainActivity", "Setting up WebSocket connection...")

        webSocketClient = CourseWebSocketClient { newCourse ->
            runOnUiThread {
                Log.d("MainActivity", "New course received via WebSocket: ${newCourse.name}")
                Toast.makeText(
                    this,
                    "New course added: ${newCourse.name} by ${newCourse.instructor}\n${newCourse.description}",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.addCourseToList(newCourse)
            }
        }

        val wsUrl = "ws://192.168.1.194:2506"
        Log.d("MainActivity", "Connecting to WebSocket at: $wsUrl")
        webSocketClient?.connect(wsUrl)
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient?.disconnect()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CourseViewModel,
    onAddCourse: () -> Unit,
    onNavigateToStudent: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onCourseClick: (Course) -> Unit,
    onDeleteCourse: (Course) -> Unit,
    onRetry: () -> Unit
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf<Course?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Instructor - Course Management") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCourse,
                containerColor = Primary
            ) {
                Icon(Icons.Filled.Add, "Add Course", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToStudent,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent
                    )
                ) {
                    Text("Student View")
                }
                Button(
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent
                    )
                ) {
                    Text("Analytics")
                }
            }

            // Content area
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading && courses.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                } else if (courses.isEmpty()) {
                    EmptyState(onRetry = onRetry)
                } else {
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(isLoading),
                        onRefresh = { viewModel.loadCourses() }
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(courses) { course ->
                                CourseItem(
                                    course = course,
                                    onClick = { onCourseClick(course) },
                                    onDeleteClick = { showDeleteDialog = course }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { course ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Course") },
            text = { Text("Are you sure you want to delete '${course.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCourse(course)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CourseItem(
    course: Course,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = course.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    StatusBadge(status = course.status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Instructor: ${course.instructor}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${course.duration}hrs • ${course.students} students",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = ErrorRed
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val backgroundColor = when (status.lowercase()) {
        "ongoing" -> StatusOngoing
        "upcoming" -> StatusUpcoming
        "completed" -> StatusCompleted
        else -> Primary
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun EmptyState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📚",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No courses available",
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

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            )
        ) {
            Text("Retry")
        }
    }
}
