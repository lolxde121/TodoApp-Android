package com.upchiapas.todo_app.presentation.tasks

import com.upchiapas.todo_app.domain.model.Task

enum class TaskFilter(val displayName: String) {
    ALL("Todas"),
    PENDING("Pendientes"),
    COMPLETED("Completadas")
}

/**
 * Estado inmutable de la UI para la gestión del recurso principal (Tareas).
 * Rúbrica Ítem 2 e Ítem 3: maneja estados de carga, lista vacía, error con reintento y formulario.
 */
data class TasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val selectedTask: Task? = null,
    val filter: TaskFilter = TaskFilter.ALL,
    val errorMessage: String? = null,
    val userMessage: String? = null,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    // Estados de validación del formulario
    val formTitle: String = "",
    val formDescription: String = "",
    val formPriority: Int = 2,
    val formDueDate: String = "",
    val formTitleError: String? = null,
    val formPriorityError: String? = null,
    val formDueDateError: String? = null,
    // Diálogo de confirmación para eliminar
    val taskToDelete: Task? = null
)
