package com.upchiapas.todo_app.data.repository

import com.upchiapas.todo_app.core.network.NetworkErrorMapper
import com.upchiapas.todo_app.core.network.mapFailure
import com.upchiapas.todo_app.data.datasource.TasksApiService
import com.upchiapas.todo_app.data.dto.CreateTaskDto
import com.upchiapas.todo_app.data.dto.UpdateTaskDto
import com.upchiapas.todo_app.data.mapper.toDomain
import com.upchiapas.todo_app.data.mapper.toDomainList
import com.upchiapas.todo_app.domain.model.Task
import com.upchiapas.todo_app.domain.repository.TasksRepository

class TasksRepositoryImpl(
    private val apiService: TasksApiService
) : TasksRepository {

    override suspend fun getTasks(completed: Boolean?): Result<List<Task>> {
        return runCatching {
            apiService.getTasks(completed = completed).toDomainList()
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override suspend fun getTaskById(id: Int): Result<Task> {
        return runCatching {
            apiService.getTaskById(id).toDomain()
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override suspend fun createTask(
        title: String,
        description: String?,
        priority: Int,
        dueDate: String?
    ): Result<Task> {
        return runCatching {
            val dto = CreateTaskDto(
                title = title,
                description = description,
                priority = priority,
                completed = false,
                dueDate = dueDate
            )
            apiService.createTask(dto).toDomain()
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override suspend fun updateTask(
        id: Int,
        title: String,
        description: String?,
        priority: Int,
        completed: Boolean,
        dueDate: String?
    ): Result<Task> {
        return runCatching {
            val dto = UpdateTaskDto(
                title = title,
                description = description,
                priority = priority,
                completed = completed,
                dueDate = dueDate
            )
            apiService.updateTask(id, dto).toDomain()
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override suspend fun toggleTaskCompleted(id: Int, completed: Boolean): Result<Task> {
        return runCatching {
            val dto = UpdateTaskDto(completed = completed)
            apiService.updateTask(id, dto).toDomain()
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override suspend fun deleteTask(id: Int): Result<Unit> {
        return runCatching {
            val response = apiService.deleteTask(id)
            if (!response.isSuccessful) {
                throw retrofit2.HttpException(response)
            }
            Unit
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }
}
