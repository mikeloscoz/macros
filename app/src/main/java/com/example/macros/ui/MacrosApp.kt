package com.example.macros.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.macros.R
import com.example.macros.ui.ajustes.AjustesScreen
import com.example.macros.ui.alimentos.AlimentosScreen
import com.example.macros.ui.hoy.HoyScreen
import com.example.macros.ui.progreso.ProgresoScreen
import com.example.macros.ui.theme.Borde
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.TextoSecundario
import com.example.macros.ui.theme.VerdeClaro

private enum class Pestana(val ruta: String, val titulo: String, @param:DrawableRes val icono: Int) {
    Hoy("hoy", "Hoy", R.drawable.ic_hoy),
    Alimentos("alimentos", "Alimentos", R.drawable.ic_alimentos),
    Progreso("progreso", "Progreso", R.drawable.ic_progreso),
    Ajustes("ajustes", "Ajustes", R.drawable.ic_ajustes),
}

@Composable
fun MacrosApp() {
    val navController = rememberNavController()
    val entrada by navController.currentBackStackEntryAsState()
    val destino = entrada?.destination

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = Borde)
                NavigationBar(containerColor = Tarjeta, tonalElevation = 0.dp) {
                    Pestana.entries.forEach { pestana ->
                        val seleccionada = destino?.hierarchy?.any { it.route == pestana.ruta } == true
                        NavigationBarItem(
                            selected = seleccionada,
                            onClick = {
                                navController.navigate(pestana.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(painterResource(pestana.icono), contentDescription = null) },
                            label = {
                                Text(
                                    pestana.titulo,
                                    fontWeight = if (seleccionada) FontWeight.SemiBold else FontWeight.Medium,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primario,
                                selectedTextColor = Primario,
                                indicatorColor = VerdeClaro,
                                unselectedIconColor = TextoSecundario,
                                unselectedTextColor = TextoSecundario,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Pestana.Hoy.ruta,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) {
            composable(Pestana.Hoy.ruta) { HoyScreen() }
            composable(Pestana.Alimentos.ruta) { AlimentosScreen() }
            composable(Pestana.Progreso.ruta) { ProgresoScreen() }
            composable(Pestana.Ajustes.ruta) { AjustesScreen() }
        }
    }
}
