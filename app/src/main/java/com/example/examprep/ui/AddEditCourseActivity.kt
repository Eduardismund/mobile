package com.example.examprep.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.examprep.model.Course
import com.example.examprep.ui.theme.*
import com.example.examprep.viewmodel.CourseViewModel

class AddEditCourseActivity : ComponentActivity() {

    private lateinit var viewModel: CourseViewModel
    private var courseId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        courseId = intent.getIntExtra("COURSE_ID", -1).takeIf { it != -1 }
        viewModel = ViewModelProvider(this)[CourseViewModel::class.java]

        setContent {
            CourseManagerTheme {
                AddEditCourseScreen(
                    viewModel = viewModel,
                    isEdit = courseId != null,
                    onNavigateBack = { finish() }
                )
            }
        }

        // Observe errors
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()

                if (it.contains("saved locally") || it.contains("Will sync")) {
                    Toast.makeText(
                        this,
                        "Course saved locally. Will sync when online.",
                        Toast.LENGTH_LONG
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }

                viewModel.clearError()
            }
        }

        // Observe operation success
        viewModel.operationSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Course saved successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCourseScreen(
    viewModel: CourseViewModel,
    isEdit: Boolean,
    onNavigateBack: () -> Unit
) {
    val isLoading by viewModel.isLoading.observeAsState(false)

    var name by remember { mutableStateOf("Mobile App Development") }
    var instructor by remember { mutableStateOf("Dr. John Smith") }
    var description by remember { mutableStateOf("Learn to build Android and iOS applications") }
    var selectedStatus by remember { mutableStateOf(0) }
    var students by remember { mutableStateOf("25") }
    var duration by remember { mutableStateOf("40") }

    val statuses = listOf("ongoing", "upcoming", "completed")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Course" else "Add Course") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Course Name
                        Column {
                            Text(
                                text = "Course Name *",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter course name") },
                                singleLine = true,
                                enabled = !isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    disabledTextColor = Color.Black
                                )
                            )
                        }

                        // Instructor
                        Column {
                            Text(
                                text = "Instructor *",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = instructor,
                                onValueChange = { instructor = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter instructor name") },
                                singleLine = true,
                                enabled = !isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    disabledTextColor = Color.Black
                                )
                            )
                        }

                        // Description
                        Column {
                            Text(
                                text = "Description *",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                placeholder = { Text("Enter course description") },
                                maxLines = 4,
                                enabled = !isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    disabledTextColor = Color.Black
                                )
                            )
                        }

                        // Status
                        Column {
                            Text(
                                text = "Status *",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            var expanded by remember { mutableStateOf(false) }

                            Box {
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(statuses[selectedStatus].capitalize(), color = Color.Black)
                                        Text("▼", color = Color.Black)
                                    }
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    statuses.forEachIndexed { index, status ->
                                        DropdownMenuItem(
                                            text = { Text(status.capitalize()) },
                                            onClick = {
                                                selectedStatus = index
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Students
                        Column {
                            Text(
                                text = "Students *",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = students,
                                onValueChange = { students = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter number of students") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                enabled = !isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    disabledTextColor = Color.Black
                                )
                            )
                        }

                        // Duration
                        Column {
                            Text(
                                text = "Duration (hours) *",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = duration,
                                onValueChange = { duration = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter duration in hours") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                enabled = !isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    disabledTextColor = Color.Black
                                )
                            )
                        }

                        // Save Button
                        Button(
                            onClick = {
                                if (name.isBlank()) {
                                    return@Button
                                }
                                if (instructor.isBlank()) {
                                    return@Button
                                }
                                if (description.isBlank()) {
                                    return@Button
                                }

                                val studentsInt = students.toIntOrNull()
                                if (studentsInt == null || studentsInt < 0) {
                                    return@Button
                                }

                                val durationInt = duration.toIntOrNull()
                                if (durationInt == null || durationInt <= 0) {
                                    return@Button
                                }

                                val course = Course(
                                    id = 0,
                                    name = name.trim(),
                                    instructor = instructor.trim(),
                                    description = description.trim(),
                                    status = statuses[selectedStatus],
                                    students = studentsInt,
                                    duration = durationInt
                                )

                                Log.d("AddEditCourseActivity", "Creating course: ${course.name}")
                                viewModel.createCourse(course)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Save Course",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        }
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
