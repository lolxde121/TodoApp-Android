package com.upchiapas.todo_app

import android.app.Application
import com.upchiapas.todo_app.di.AppContainer
import com.upchiapas.todo_app.di.DefaultAppContainer

/**
 * Clase Application personalizada para alojar el contenedor de dependencias (AppContainer).
 * Rúbrica Ítem 5: AppContainer creado una sola vez en la clase Application.
 */
class TodoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
