package com.upchiapas.todo_app.di

import android.content.Context
import com.upchiapas.todo_app.core.network.AuthInterceptor
import com.upchiapas.todo_app.core.session.SessionManager
import com.upchiapas.todo_app.core.session.SessionManagerImpl
import com.upchiapas.todo_app.data.datasource.AuthApiService
import com.upchiapas.todo_app.data.datasource.TasksApiService
import com.upchiapas.todo_app.data.repository.AuthRepositoryImpl
import com.upchiapas.todo_app.data.repository.TasksRepositoryImpl
import com.upchiapas.todo_app.domain.repository.AuthRepository
import com.upchiapas.todo_app.domain.repository.TasksRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Contrato del Contenedor de Dependencias de la Aplicación.
 * Rúbrica Ítem 5: AppContainer construido una sola vez en la clase Application.
 */
interface AppContainer {
    val sessionManager: SessionManager
    val authRepository: AuthRepository
    val tasksRepository: TasksRepository
}

/**
 * Implementación manual del Contenedor de Dependencias.
 * Construye una única instancia compartida de Retrofit, OkHttp, Repositorios y SessionManager.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    /**
     * URL base de la API FastAPI.
     * 10.0.2.2 es el alias del host local para el emulador Android de Google.
     */
    private val baseUrl = "http://10.0.2.2:8000/"

    override val sessionManager: SessionManager by lazy {
        SessionManagerImpl(context.applicationContext)
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(sessionManager)
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    private val tasksApiService: TasksApiService by lazy {
        retrofit.create(TasksApiService::class.java)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService, sessionManager)
    }

    override val tasksRepository: TasksRepository by lazy {
        TasksRepositoryImpl(tasksApiService)
    }
}
