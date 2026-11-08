package com.upchiapas.todo_app.data.mapper

import com.upchiapas.todo_app.data.dto.TaskDto
import com.upchiapas.todo_app.domain.model.Task

/**
 * Mapper dedicado para convertir TaskDto a modelo de dominio Task.
 * Inspirado directamente en PostMapper.kt de kt_p4a y conforme a la rúbrica (Ítem 4).
 */
fun TaskDto.toDomain(): Task {
    return Task(
        id = this.id,
        title = this.title,
        description = this.description ?: "",
        priority = this.priority,
        completed = this.completed,
        dueDate = this.dueDate ?: "",
        userId = this.userId,
        createdAt = this.createdAt
    )
}

fun List<TaskDto>.toDomainList(): List<Task> {
    return this.map { it.toDomain() }
}
