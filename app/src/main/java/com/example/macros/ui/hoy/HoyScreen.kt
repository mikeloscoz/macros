package com.example.macros.ui.hoy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.macros.ui.components.CabeceraPantalla
import com.example.macros.ui.components.MargenLateral
import com.example.macros.ui.components.TarjetaMacros

@Composable
fun HoyScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = MargenLateral),
    ) {
        CabeceraPantalla("Hoy")
        TarjetaMacros {
            Text("Próximamente", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
