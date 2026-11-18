package com.example.macros.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.macros.MacrosApplication
import com.example.macros.data.AppContainer

/** Crea ViewModels pasándoles el contenedor de datos de la app. */
inline fun <reified VM : androidx.lifecycle.ViewModel> fabrica(
    crossinline crear: (AppContainer) -> VM,
): ViewModelProvider.Factory = viewModelFactory {
    initializer { crear(contenedor()) }
}

fun CreationExtras.contenedor(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MacrosApplication).container
