package com.upchiapas.todo_app.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.todo_app.domain.model.Task
import com.upchiapas.todo_app.domain.repository.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: TasksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        fetchTasks()
    }

    // -------------------------------------------------------------
    // OPERACIÓN 1: LISTAR (con soporte de filtros y reintento)
    // -------------------------------------------------------------
    fun fetchTasks() {
        val currentFilter = _uiState.value.filter
        val completedParam = when (currentFilter) {
            TaskFilter.ALL -> null
            TaskFilter.PENDING -> false
            TaskFilter.COMPLETED -> true
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.getTasks(completed = completedParam)
            result.fold(
                onSuccess = { tasks ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tasks = tasks
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Error al cargar las tareas"
                        )
                    }
                }
            )
        }
    }

    fun setFilter(filter: TaskFilter) {
        if (_uiState.value.filter != filter) {
            _uiState.update { it.copy(filter = filter) }
            fetchTasks()
        }
    }

    // -------------------------------------------------------------
    // OPERACIÓN 2: VER DETALLE
    // -------------------------------------------------------------
    fun loadTaskDetail(id: Int) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.getTaskById(id)
            result.fold(
                onSuccess = { task ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedTask = task
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Error al cargar detalle de la tarea"
                        )
                    }
                }
            )
        }
    }

    // -------------------------------------------------------------
    // FORMULARIO REUTILIZABLE: CREAR Y EDITAR
    // -------------------------------------------------------------
    fun initForm(task: Task? = null) {
        if (task != null) {
            _uiState.update {
                it.copy(
                    formTitle = task.title,
                    formDescription = task.description,
                    formPriority = task.priority,
                    formDueDate = task.dueDate,
                    formTitleError = null,
                    formPriorityError = null,
                    formDueDateError = null,
                    isSaveSuccess = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    formTitle = "",
                    formDescription = "",
                    formPriority = 2,
                    formDueDate = "",
                    formTitleError = null,
                    formPriorityError = null,
                    formDueDateError = null,
                    isSaveSuccess = false
                )
            }
        }
    }

    fun onFormTitleChanged(value: String) {
        _uiState.update { it.copy(formTitle = value, formTitleError = null) }
    }

    fun onFormDescriptionChanged(value: String) {
        _uiState.update { it.copy(formDescription = value) }
    }

    fun onFormPriorityChanged(value: Int) {
        _uiState.update { it.copy(formPriority = value, formPriorityError = null) }
    }

    fun onFormDueDateChanged(value: String) {
        _uiState.update { it.copy(formDueDate = value, formDueDateError = null) }
    }

    // -------------------------------------------------------------
    // OPERACIONES 3 & 4: GUARDAR (CREAR O ACTUALIZAR)
    // -------------------------------------------------------------
    fun saveTask(taskId: Int? = null) {
        val state = _uiState.value
        val title = state.formTitle.trim()
        val description = state.formDescription.trim()
        val priority = state.formPriority
        val dueDate = state.formDueDate.trim()

        var hasError = false

        if (title.isBlank()) {
            _uiState.update { it.copy(formTitleError = "El título es obligatorio.") }
            hasError = true
        } else if (title.length < 3) {
            _uiState.update { it.copy(formTitleError = "El título debe tener al menos 3 caracteres.") }
            hasError = true
        }

        if (priority !in 1..3) {
            _uiState.update { it.copy(formPriorityError = "La prioridad debe ser 1 (Baja), 2 (Media) o 3 (Alta).") }
            hasError = true
        }

        if (dueDate.isNotBlank() && !dueDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            _uiState.update { it.copy(formDueDateError = "Usa el formato AAAA-MM-DD (ej: 2026-12-31).") }
            hasError = true
        }

        if (hasError) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = if (taskId == null) {
                // Crear
                repository.createTask(
                    title = title,
                    description = description.ifBlank { null },
                    priority = priority,
                    dueDate = dueDate.ifBlank { null }
                )
            } else {
                // Editar
                val currentTask = state.selectedTask
                repository.updateTask(
                    id = taskId,
                    title = title,
                    description = description.ifBlank { null },
                    priority = priority,
                    completed = currentTask?.completed ?: false,
                    dueDate = dueDate.ifBlank { null }
                )
            }

            result.fold(
                onSuccess = { savedTask ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaveSuccess = true,
                            selectedTask = savedTask,
                            userMessage = if (taskId == null) "Tarea creada exitosamente" else "Tarea actualizada exitosamente"
                        )
                    }
                    fetchTasks()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            userMessage = "Error al guardar: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    // Alternar estado completado
    fun toggleTaskCompleted(task: Task) {
        val newStatus = !task.completed
        viewModelScope.launch {
            val result = repository.toggleTaskCompleted(task.id, newStatus)
            result.fold(
                onSuccess = { updated ->
                    val updatedList = _uiState.value.tasks.map {
                        if (it.id == updated.id) updated else it
                    }
                    _uiState.update {
                        it.copy(
                            tasks = updatedList,
                            selectedTask = if (it.selectedTask?.id == updated.id) updated else it.selectedTask,
                            userMessage = if (newStatus) "¡Tarea completada!" else "Tarea pendiente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(userMessage = "No se pudo actualizar: ${error.localizedMessage}")
                    }
                }
            )
        }
    }

    // -------------------------------------------------------------
    // OPERACIÓN 5: ELIMINAR CON CONFIRMACIÓN
    // -------------------------------------------------------------
    fun promptDelete(task: Task) {
        _uiState.update { it.copy(taskToDelete = task) }
    }

    fun dismissDelete() {
        _uiState.update { it.copy(taskToDelete = null) }
    }

    fun confirmDelete() {
        val task = _uiState.value.taskToDelete ?: return
        _uiState.update { it.copy(taskToDelete = null, isLoading = true) }
        viewModelScope.launch {
            val result = repository.deleteTask(task.id)
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            tasks = state.tasks.filterNot { it.id == task.id },
                            selectedTask = null,
                            userMessage = "Tarea eliminada correctamente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = "Error al eliminar: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(isSaveSuccess = false) }
    }
}
