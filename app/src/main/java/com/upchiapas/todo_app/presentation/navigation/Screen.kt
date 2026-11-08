package com.upchiapas.todo_app.presentation.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Detail : Screen("detail/{taskId}") {
        fun createRoute(taskId: Int) = "detail/$taskId"
    }
    data object Form : Screen("form?taskId={taskId}") {
        fun createRoute(taskId: Int? = null) = if (taskId != null) "form?taskId=$taskId" else "form"
    }
}
