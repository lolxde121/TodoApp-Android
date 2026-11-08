package com.upchiapas.todo_app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.upchiapas.todo_app.di.AppContainer
import com.upchiapas.todo_app.presentation.auth.AuthViewModel
import com.upchiapas.todo_app.presentation.auth.AuthViewModelFactory
import com.upchiapas.todo_app.presentation.auth.LoginPage
import com.upchiapas.todo_app.presentation.auth.RegisterPage
import com.upchiapas.todo_app.presentation.tasks.HomeScreen
import com.upchiapas.todo_app.presentation.tasks.TaskDetailScreen
import com.upchiapas.todo_app.presentation.tasks.TaskFormScreen
import com.upchiapas.todo_app.presentation.tasks.TasksViewModel
import com.upchiapas.todo_app.presentation.tasks.TasksViewModelFactory

/**
 * Grafo principal de Navegación con Navigation Compose.
 * 
 * Rúbrica:
 * - RF-03: Si hay sesión activa (token), se inicia directamente en Home.
 * - RF-04: Cerrar sesión limpia la pila de navegación evitando volver con el botón Atrás.
 */
@Composable
fun AppNavHost(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Inyección de ViewModels mediante sus Factories (Rúbrica Ítem 5)
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(appContainer.authRepository)
    )
    val tasksViewModel: TasksViewModel = viewModel(
        factory = TasksViewModelFactory(appContainer.tasksRepository)
    )

    // Decisión de destino inicial (persistencia de sesión)
    val startDestination = if (appContainer.sessionManager.hasActiveSession()) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // PANTALLA 1: LOGIN
        composable(Screen.Login.route) {
            LoginPage(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    tasksViewModel.fetchTasks()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // PANTALLA 2: REGISTRO
        composable(Screen.Register.route) {
            RegisterPage(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // PANTALLA 3: HOME (LISTADO DE TAREAS Y CRUD PRINCIPAL)
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = tasksViewModel,
                onNavigateToDetail = { taskId ->
                    navController.navigate(Screen.Detail.createRoute(taskId))
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.Form.createRoute(null))
                },
                onNavigateToEdit = { taskId ->
                    navController.navigate(Screen.Form.createRoute(taskId))
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // PANTALLA 4: DETALLE DE TAREA
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
            TaskDetailScreen(
                taskId = taskId,
                viewModel = tasksViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.Form.createRoute(id))
                }
            )
        }

        // PANTALLA 5: FORMULARIO REUTILIZABLE (CREAR / EDITAR)
        composable(
            route = Screen.Form.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val taskIdString = backStackEntry.arguments?.getString("taskId")
            val taskId = taskIdString?.toIntOrNull()

            TaskFormScreen(
                taskId = taskId,
                viewModel = tasksViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
