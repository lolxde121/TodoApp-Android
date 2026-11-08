package com.upchiapas.todo_app.data.dto

import com.google.gson.annotations.SerializedName

data class TaskDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("priority")
    val priority: Int = 2,
    @SerializedName("completed")
    val completed: Boolean = false,
    @SerializedName("due_date")
    val dueDate: String?,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("created_at")
    val createdAt: String
)

data class CreateTaskDto(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("priority")
    val priority: Int = 2,
    @SerializedName("completed")
    val completed: Boolean = false,
    @SerializedName("due_date")
    val dueDate: String? = null
)

data class UpdateTaskDto(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("priority")
    val priority: Int? = null,
    @SerializedName("completed")
    val completed: Boolean? = null,
    @SerializedName("due_date")
    val dueDate: String? = null
)
