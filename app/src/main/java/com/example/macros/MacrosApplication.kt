package com.example.macros

import android.app.Application
import android.util.Log
import com.example.macros.data.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MacrosApplication : Application() {
    lateinit var container: AppContainer
        private set

    /** Para tareas que deben terminar aunque se cierre una pantalla. */
    val scope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        scope.launch {
            runCatching { container.cargarAlimentosIniciales() }
                .onFailure { Log.e("Macros", "No se pudieron cargar los alimentos iniciales", it) }
        }
    }
}
