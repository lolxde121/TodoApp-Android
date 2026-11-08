package com.upchiapas.todo_app.presentation.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Stateful Composable para el formulario reutilizable de Crear y Editar tarea.
 * Rúbrica RF-07 y RF-08: Formulario único reutilizable y validado.
 */
@Composable
fun TaskFormScreen(
    taskId: Int?,
    viewModel: TasksViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) {
        if (taskId != null) {
            val task = uiState.tasks.find { it.id == taskId } ?: uiState.selectedTask
            viewModel.initForm(task)
        } else {
            viewModel.initForm(null)
        }
    }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            viewModel.resetSaveSuccess()
            onNavigateBack()
        }
    }

    TaskFormContent(
        isEditing = taskId != null,
        uiState = uiState,
        onTitleChanged = { viewModel.onFormTitleChanged(it) },
        onDescriptionChanged = { viewModel.onFormDescriptionChanged(it) },
        onPriorityChanged = { viewModel.onFormPriorityChanged(it) },
        onDueDateChanged = { viewModel.onFormDueDateChanged(it) },
        onSave = { viewModel.saveTask(taskId) },
        onNavigateBack = onNavigateBack,
        onClearUserMessage = { viewModel.clearUserMessage() },
        modifier = modifier
    )
}

/**
 * Stateless Composable con el formulario y validaciones visuales.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormContent(
    isEditing: Boolean,
    uiState: TasksUiState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onPriorityChanged: (Int) -> Unit,
    onDueDateChanged: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearUserMessage: () -> Unit,
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
                title = { Text(if (isEditing) "Editar Tarea" else "Nueva Tarea") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditing) "Actualiza los datos de tu tarea" else "Ingresa los atributos de tu nueva tarea",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Campo: Título
                OutlinedTextField(
                    value = uiState.formTitle,
                    onValueChange = onTitleChanged,
                    label = { Text("Título de la tarea *") },
                    isError = uiState.formTitleError != null,
                    supportingText = {
                        uiState.formTitleError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Title, contentDescription = null)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo: Descripción
                OutlinedTextField(
                    value = uiState.formDescription,
                    onValueChange = onDescriptionChanged,
                    label = { Text("Descripción (opcional)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null)
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo: Fecha límite (AAAA-MM-DD)
                OutlinedTextField(
                    value = uiState.formDueDate,
                    onValueChange = onDueDateChanged,
                    label = { Text("Fecha límite (ej: 2026-12-31)") },
                    placeholder = { Text("AAAA-MM-DD") },
                    isError = uiState.formDueDateError != null,
                    supportingText = {
                        uiState.formDueDateError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSave() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Prioridad (Numérico 1..3)
                Column {
                    Text(
                        text = "Nivel de Prioridad *",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val priorityOptions = listOf(
                            1 to "Baja",
                            2 to "Media",
                            3 to "Alta"
                        )

                        priorityOptions.forEach { (value, label) ->
                            Row(
                                modifier = Modifier
                                    .selectable(
                                        selected = uiState.formPriority == value,
                                        onClick = { onPriorityChanged(value) },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.formPriority == value,
                                    onClick = null
                                )
                                Text(
                                    text = " $label",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    uiState.formPriorityError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSave,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (isEditing) "Guardar Cambios" else "Crear Tarea")
                    }
                }
            }
        }
    }
}
