# To-Do Mobile App (Android · Kotlin · Jetpack Compose)

Aplicación móvil nativa para Android desarrollada en **Kotlin** con **Jetpack Compose (Material 3)** y arquitectura limpia **MVVM**, diseñada para el **Proyecto de Unidad 3 - App CRUD con autenticación** siguiendo estrictamente los criterios de evaluación de la **Rúbrica Analítica**.

Toma como referencia la arquitectura modular y las mejores prácticas del repositorio de referencia [`kt_p4a`](https://github.com/alilopez37/kt_p4a.git) (módulo `jsonplaceholder`).

---

## 1. Cumplimiento de la Rúbrica de Evaluación (Puntaje Máximo: 40 pts)

### Ítem 1: Autenticación: Register y Login (Peso: 20% · 20 pts)
- **Registro (`RF-01`)**: Formulario con nombre, correo, contraseña y confirmación. Validaciones en `AuthViewModel`: campos vacíos, expresión regular de correo, longitud mínima de 8 caracteres y validación de coincidencia de contraseñas.
- **Login (`RF-02`)**: Autenticación contra el endpoint de la API con manejo de errores claros (credenciales incorrectas y problemas de conectividad).
- **Persistencia de sesión (`RF-03`)**: El token JWT se almacena de forma persistente y segura tras la abstracción `SessionManager`. Si hay sesión activa al abrir la app, navega directamente a Home.
- **Interceptor de Red**: Todas las peticiones al recurso incluyen la cabecera `Authorization: Bearer <token>` inyectada automáticamente mediante `AuthInterceptor` de OkHttp.
- **Cierre de sesión (`RF-04`)**: Botón visible en la barra superior de Home. Borra el token y limpia la pila de navegación con Navigation Compose (`popUpTo(0) { inclusive = true }`) para impedir volver atrás.

### Ítem 2: Home: CRUD del recurso principal (Peso: 25% · 25 pts)
Implementación completa de las 5 operaciones sobre el recurso principal:
1. **Listar (`RF-05`)**: Listado reactivo con `LazyColumn` filtrable por estado (*Todas, Pendientes, Completadas*).
2. **Ver detalle (`RF-06`)**: Pantalla dedicada `TaskDetailScreen` donde se muestran todos los atributos del recurso (título, descripción, prioridad con badge coloreado, fecha límite, estado completado y fecha de registro).
3. **Crear (`RF-07`)**: Formulario con validaciones en vivo (título obligatorio mín. 3 caracteres, fecha límite válida y prioridad 1..3).
4. **Editar (`RF-08`)**: El mismo formulario reutilizable `TaskFormScreen` precargado con los datos actuales de la tarea.
5. **Eliminar (`RF-09`)**: Diálogo modal de confirmación `DeleteConfirmDialog` antes de eliminar permanentemente la tarea.
- **Estados de Interfaz**: Animación de carga (`CircularProgressIndicator`), estado de lista vacía amigable y pantalla de error con botón de reintento.
- **Retroalimentación (`RF-10`)**: Notificaciones emergentes con `SnackbarHost` tras cada operación.

### Ítem 3: Patrón MVVM (Peso: 20% · 20 pts)
- **Composables sin lógica**: Separación estricta entre *Stateful Composables* (orquestan ciclo de vida y navegación) y *Stateless Composables* (vistas puras que solo reciben `uiState` y emiten lambdas de eventos).
- **Flujo Unidireccional de Datos (UDF)**: ViewModels con `MutableStateFlow` privado y `StateFlow<UiState>` inmutable observado con `collectAsStateWithLifecycle()`.
- **Persistencia ante Rotación**: Los estados sobreviven a la rotación de pantalla mediante `ViewModel`.

### Ítem 4: Capa de datos: Repository, DTO y Mapper (Peso: 20% · 20 pts)
- **Domain**: Interfaces de repositorio (`AuthRepository`, `TasksRepository`) que exponen exclusivamente modelos de dominio inmutables (`User`, `Task`) y la mónada `Result<T>`.
- **Data**: DTOs independientes (`AuthDtos.kt`, `TaskDtos.kt`) para el transporte de red.
- **Mappers**: Transformadores dedicados (`UserMapper.kt`, `TaskMapper.kt`) para desacoplar el contrato de red de los modelos de negocio.
- Los ViewModels dependen de las interfaces de dominio y los DTOs jamás escapan de la capa `data`.

### Ítem 5: Inyección de dependencias manual (Peso: 15% · 15 pts)
- Contenedor de dependencias `AppContainer` (`DefaultAppContainer`) inicializado una única vez en `TodoApplication`.
- Comparte una única instancia de Retrofit, OkHttp, `SessionManager` y los Repositorios.
- Las clases reciben sus dependencias tipadas como interfaces en su constructor.
- ViewModels creados mediante `AuthViewModelFactory` y `TasksViewModelFactory`. Sin uso de Hilt, Dagger ni Koin.

---

## 2. Dominio del Problema

- **Recurso Principal**: `Task` (Tarea)
- **Identificador**: `id: Int`
- **Atributos Propios (5+)**:
  1. `title: String` (Texto · Obligatorio, mín. 3 caracteres)
  2. `description: String` (Texto · Descripción detallada opcional)
  3. `priority: Int` (Numérico · 1=Baja, 2=Media, 3=Alta con validación de rango)
  4. `completed: Boolean` (Booleano · Estado de completado)
  5. `dueDate: String` (Fecha · Formato `AAAA-MM-DD`)
  6. `createdAt: String` (Fecha de registro de la tarea)

---

## 3. Estructura del Proyecto

```text
app/src/main/java/com/upchiapas/todo_app/
├── TodoApplication.kt                 # Contenedor de dependencias AppContainer
├── MainActivity.kt                    # Punto de entrada de la actividad
├── core/
│   ├── network/
│   │   ├── AuthInterceptor.kt         # Inyección de Bearer Token
│   │   └── NetworkErrorMapper.kt      # Mapeo de errores y Result.mapFailure
│   └── session/
│       └── SessionManager.kt          # Abstracción para persistencia del token
├── di/
│   └── AppContainer.kt                # AppContainer y DefaultAppContainer
├── domain/
│   ├── model/
│   │   ├── Task.kt                    # Modelo de Dominio de Tarea
│   │   └── User.kt                    # Modelo de Dominio de Usuario
│   └── repository/
│       ├── AuthRepository.kt          # Interfaz de repositorio de autenticación
│       └── TasksRepository.kt         # Interfaz de repositorio de tareas (CRUD)
├── data/
│   ├── datasource/
│   │   ├── AuthApiService.kt          # Endpoints Retrofit /auth
│   │   └── TasksApiService.kt         # Endpoints Retrofit /tareas
│   ├── dto/
│   │   ├── AuthDtos.kt                # DTOs de autenticación
│   │   └── TaskDtos.kt                # DTOs de tareas
│   ├── mapper/
│   │   ├── TaskMapper.kt              # Mapeo DTO ↔ Dominio
│   │   └── UserMapper.kt              # Mapeo DTO ↔ Dominio
│   └── repository/
│       ├── AuthRepositoryImpl.kt      # Implementación del repositorio de auth
│       └── TasksRepositoryImpl.kt     # Implementación del repositorio de tareas
├── presentation/
│   ├── navigation/
│   │   ├── AppNavHost.kt              # Grafo Navigation Compose
│   │   └── Screen.kt                  # Definición de rutas
│   ├── auth/
│   │   ├── AuthUiState.kt             # Estado inmutable de auth
│   │   ├── AuthViewModel.kt           # ViewModel con validaciones RF-01 y RF-02
│   │   ├── AuthViewModelFactory.kt
│   │   ├── LoginPage.kt               # Pantalla de Login
│   │   └── RegisterPage.kt            # Pantalla de Registro
│   └── tasks/
│       ├── TasksUiState.kt            # Estado inmutable del CRUD
│       ├── TasksViewModel.kt          # ViewModel de las 5 operaciones CRUD
│       ├── TasksViewModelFactory.kt
│       ├── HomeScreen.kt              # Pantalla principal (Lista, Filtros, FAB)
│       ├── TaskDetailScreen.kt        # Pantalla de Detalle (Todos los atributos)
│       ├── TaskFormScreen.kt          # Formulario reutilizable (Crear / Editar)
│       └── components/
│           ├── DeleteConfirmDialog.kt # Diálogo modal de confirmación
│           ├── TaskFilterChips.kt     # Chips de filtrado
│           └── TaskItem.kt            # Tarjeta de tarea
└── ui/theme/                          # Sistema de diseño Material 3
```

---

## 4. Configuración y Ejecución

### Prerrequisitos
- Android Studio Ladybug o superior.
- SDK de Android (mínimo API 26, compilado con API 35).
- Servidor de la API REST ejecutándose:
  ```bash
  cd ../Api-moviles
  uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
  ```

### Conexión de Red en [`DefaultAppContainer.kt`](file:///home/edgar/Descargas/todo-app-android/app/src/main/java/com/upchiapas/todo_app/di/AppContainer.kt)
- **Emulador de Android Studio**: Utiliza `http://10.0.2.2:8000/` (preconfigurado por defecto).
- **Dispositivo Físico por Wi-Fi**: Cambia la URL base a la IP de tu red local (ejemplo: `http://192.168.1.100:8000/`).
