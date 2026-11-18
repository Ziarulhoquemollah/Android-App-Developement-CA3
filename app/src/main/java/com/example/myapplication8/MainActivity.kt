package com.example.myapplication8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// 1. DATA MODEL
data class StudentEntity(
    val id: Int = 0,
    val name: String,
    val email: String,
    val course: String
)

// 2. VIEWMODEL (In-Memory State Management)
class StudentViewModel : ViewModel() {

    private val _students = MutableStateFlow<List<StudentEntity>>(
        listOf(
            StudentEntity(id = 1, name = "Rahul Sharma", email = "rahul@gmail.com", course = "B.Tech CSE"),
            StudentEntity(id = 2, name = "Amit Kumar", email = "amit@gmail.com", course = "B.Tech IT")
        )
    )
    val students = _students.asStateFlow()

    private var nextId = 3

    fun addStudent(name: String, email: String, course: String) {
        val newStudent = StudentEntity(
            id = nextId++,
            name = name,
            email = email,
            course = course
        )
        _students.value = _students.value + newStudent
    }

    fun deleteStudent(student: StudentEntity) {
        _students.value = _students.value.filter { it.id != student.id }
    }
}

// 3. MAIN ACTIVITY
class MainActivity : ComponentActivity() {

    private val viewModel: StudentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val students by viewModel.students.collectAsState()

                StudentScreen(
                    students = students,
                    onAddStudent = { name, email, course ->
                        viewModel.addStudent(name, email, course)
                    },
                    onDeleteStudent = {
                        viewModel.deleteStudent(it)
                    }
                )
            }
        }
    }
}

// 4. COMPOSE UI
@Composable
fun StudentScreen(
    students: List<StudentEntity>,
    onAddStudent: (String, String, String) -> Unit,
    onDeleteStudent: (StudentEntity) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Student Directory",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Student Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Student Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = course,
            onValueChange = { course = it },
            label = { Text("Student Course") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (
                    name.isNotBlank() &&
                    email.isNotBlank() &&
                    course.isNotBlank()
                ) {
                    onAddStudent(name, email, course)
                    name = ""
                    email = ""
                    course = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Student")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Student List (${students.size})",
            style = MaterialTheme.typography.titleLarge
        )

        LazyColumn {
            items(
                items = students,
                key = { it.id }
            ) { student ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ID: ${student.id}")
                            Text("Name: ${student.name}")
                            Text("Email: ${student.email}")
                            Text("Course: ${student.course}")
                        }

                        TextButton(
                            onClick = {
                                onDeleteStudent(student)
                            }
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

// 5. PREVIEW
@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Student Directory Preview"
)
@Composable
fun StudentScreenPreview() {
    MaterialTheme {
        StudentScreen(
            students = listOf(
                StudentEntity(
                    id = 1,
                    name = "Rahul Sharma",
                    email = "rahul@gmail.com",
                    course = "B.Tech CSE"
                ),
                StudentEntity(
                    id = 2,
                    name = "Amit Kumar",
                    email = "amit@gmail.com",
                    course = "B.Tech IT"
                )
            ),
            onAddStudent = { _, _, _ -> },
            onDeleteStudent = {}
        )
    }
}
