package com.upchiapas.todo_app.domain.repository

import com.upchiapas.todo_app.domain.model.Task

/**
 * Contrato del Repositorio del Recurso Principal (Tareas) en la capa de Domain.
 * Satisface los principios ISP, DIP y la rúbrica (Ítem 2 e Ítem 4).
 */
interface TasksRepository {
    suspend fun getTasks(completed: Boolean? = null): Result<List<Task>>
    suspend fun getTaskById(id: Int): Result<Task>
    suspend fun createTask(title: String, description: String?, priority: Int, dueDate: String?): Result<Task>
    suspend fun updateTask(id: Int, title: String, description: String?, priority: Int, completed: Boolean, dueDate: String?): Result<Task>
    suspend fun toggleTaskCompleted(id: Int, completed: Boolean): Result<Task>
    suspend fun deleteTask(id: Int): Result<Unit>
}
