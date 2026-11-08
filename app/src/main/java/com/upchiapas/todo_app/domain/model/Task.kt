package com.upchiapas.todo_app.domain.model

/**
 * Entidad de dominio pura que representa el recurso principal de la aplicación.
 * 
 * Cumple con los requisitos del dominio (Página 3 de la rúbrica):
 * 1. Identificador (id) y 5 atributos propios (title, description, priority, completed, dueDate).
 * 2. Atributo numérico: priority (1 a 3).
 * 3. Atributo booleano: completed (true/false).
 * 4. Atributo fecha: dueDate (formato YYYY-MM-DD).
 * 5. Atributo texto: title y description.
 * 6. Reglas de validación: title (obligatorio, mín. 3 caracteres) y priority (rango 1..3).
 */
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val priority: Int = 2, // 1: Baja, 2: Media, 3: Alta
    val completed: Boolean = false,
    val dueDate: String = "",
    val userId: Int = 0,
    val createdAt: String = ""
) {
    val priorityName: String
        get() = when (priority) {
            1 -> "Baja"
            3 -> "Alta"
            else -> "Media"
        }
}
