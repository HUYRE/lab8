package com.example.lab8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch
import com.example.lab8.ui.theme.Lab8Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab8Theme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                ).build()

                val taskDao = db.taskDao()
                val viewModel = TaskViewModel(taskDao)

                TaskScreen(viewModel)
            }
        }
    }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var newTaskDescription by remember { mutableStateOf("") }

    // Variable para controlar si se muestra el diálogo de edición
    var isEditDialogOpen by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    // Variable para controlar si se muestran tareas completadas o pendientes
    var showCompleted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botones para filtrar tareas
        Row {
            Button(onClick = {
                showCompleted = false
                viewModel.filterTasks(showCompleted) // Asumiendo que hay un método para filtrar
            }) {
                Text("Pendientes")
            }
            Spacer(modifier = Modifier.width(8.dp)) // Espaciado entre botones
            Button(onClick = {
                showCompleted = true
                viewModel.filterTasks(showCompleted) // Asumiendo que hay un método para filtrar
            }) {
                Text("Completadas")
            }
        }

        // Campo de texto para agregar nueva tarea
        TextField(
            value = newTaskDescription,
            onValueChange = { newTaskDescription = it },
            label = { Text("Nueva tarea") },
            modifier = Modifier.fillMaxWidth()
        )

        // Botón para agregar nueva tarea
        Button(
            onClick = {
                if (newTaskDescription.isNotEmpty()) {
                    viewModel.addTask(newTaskDescription)
                    newTaskDescription = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Agregar tarea")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar lista de tareas según el filtro
        val filteredTasks = tasks.filter { it.isCompleted == showCompleted }
        filteredTasks.forEach { task ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = task.description)

                // Botón para alternar el estado de completado de la tarea
                Button(onClick = { viewModel.toggleTaskCompletion(task) }) {
                    Text(if (task.isCompleted) "Completada" else "Pendiente")
                }

                // Botón para abrir el diálogo de edición
                Button(onClick = {
                    taskToEdit = task
                    isEditDialogOpen = true
                }) {
                    Text("Editar")
                }

                // Botón para eliminar la tarea individual
                Button(onClick = { viewModel.deleteTask(task) }) {
                    Text("Eliminar")
                }
            }
        }

        // Botón para eliminar todas las tareas
        Button(
            onClick = { coroutineScope.launch { viewModel.deleteAllTasks() } },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Eliminar todas las tareas")
        }
    }

    // Mostrar el diálogo de edición si hay una tarea seleccionada
    if (isEditDialogOpen && taskToEdit != null) {
        EditTaskDialog(
            task = taskToEdit!!,
            viewModel = viewModel,
            onDismiss = { isEditDialogOpen = false }
        )
    }
}

@Composable
fun EditTaskDialog(task: Task, viewModel: TaskViewModel, onDismiss: () -> Unit) {
    var newDescription by remember { mutableStateOf(task.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar tarea") },
        text = {
            TextField(
                value = newDescription,
                onValueChange = { newDescription = it }
            )
        },
        confirmButton = {
            Button(onClick = {
                viewModel.editTask(task, newDescription)  // Actualizar la tarea con la nueva descripción
                onDismiss()
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
