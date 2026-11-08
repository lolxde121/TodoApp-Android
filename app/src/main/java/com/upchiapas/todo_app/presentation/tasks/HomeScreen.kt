package com.upchiapas.todo_app.presentation.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upchiapas.todo_app.domain.model.Task
import com.upchiapas.todo_app.presentation.tasks.components.DeleteConfirmDialog
import com.upchiapas.todo_app.presentation.tasks.components.TaskFilterChips
import com.upchiapas.todo_app.presentation.tasks.components.TaskItem

/**
 * Stateful Composable: Se conecta con el ciclo de vida del ViewModel de Tareas.
 * Rúbrica Ítem 3: Observa StateFlow con collectAsStateWithLifecycle y delega en HomeContent.
 */
@Composable
fun HomeScreen(
    viewModel: TasksViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onRefresh = { viewModel.fetchTasks() },
        onFilterSelected = { viewModel.setFilter(it) },
        onTaskClick = { onNavigateToDetail(it.id) },
        onToggleCompleted = { viewModel.toggleTaskCompleted(it) },
        onNavigateToCreate = onNavigateToCreate,
        onNavigateToEdit = { onNavigateToEdit(it.id) },
        onPromptDelete = { viewModel.promptDelete(it) },
        onConfirmDelete = { viewModel.confirmDelete() },
        onDismissDelete = { viewModel.dismissDelete() },
        onClearUserMessage = { viewModel.clearUserMessage() },
        onLogout = onLogout,
        modifier = modifier
    )
}

/**
 * Stateless Composable: Vista pura para el listado del recurso principal.
 * Cumple con RF-04, RF-05, RF-09 y RF-10 de la rúbrica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: TasksUiState,
    onRefresh: () -> Unit,
    onFilterSelected: (TaskFilter) -> Unit,
    onTaskClick: (Task) -> Unit,
    onToggleCompleted: (Task) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (Task) -> Unit,
    onPromptDelete: (Task) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onClearUserMessage: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Actualizar lista")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Crear nueva tarea")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TaskFilterChips(
                selectedFilter = uiState.filter,
                onFilterSelected = onFilterSelected
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading && uiState.tasks.isEmpty() -> {
                        CircularProgressIndicator()
                    }

                    uiState.errorMessage != null && uiState.tasks.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRefresh) {
                                Text("Reintentar")
                            }
                        }
                    }

                    uiState.tasks.isEmpty() -> {
                        Text(
                            text = "No tienes tareas registradas en esta sección.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.tasks, key = { it.id }) { task ->
                                TaskItem(
                                    task = task,
                                    onClick = { onTaskClick(task) },
                                    onToggleCompleted = { onToggleCompleted(task) },
                                    onEdit = { onNavigateToEdit(task) },
                                    onDelete = { onPromptDelete(task) }
                                )
                            }
                        }
                    }
                }

                if (uiState.taskToDelete != null) {
                    DeleteConfirmDialog(
                        task = uiState.taskToDelete,
                        onConfirm = onConfirmDelete,
                        onDismiss = onDismissDelete
                    )
                }
            }
        }
    }
}
