@file:Suppress("FunctionName")

package com.zachklipp.compose.backstack.viewer

import android.os.Handler
import androidx.compose.Composable
import androidx.compose.Pivotal
import androidx.compose.onActive
import androidx.compose.state
import androidx.ui.core.Text
import androidx.ui.foundation.Icon
import androidx.ui.layout.Center
import androidx.ui.material.FloatingActionButton
import androidx.ui.material.IconButton
import androidx.ui.material.Scaffold
import androidx.ui.material.TopAppBar
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.Add
import androidx.ui.material.icons.filled.ArrowBack
import androidx.ui.material.icons.filled.Menu
import androidx.ui.tooling.preview.Preview

@Preview
@Composable
private fun AppScreenPreview() {
    AppScreen(name = "preview", showBack = false, onBack = {}, onAdd = {})
}

@Composable
internal fun AppScreen(
    name: String,
    showBack: Boolean,
    onBack: () -> Unit,
    onAdd: () -> Unit
) {
    Scaffold(
        topAppBar = {
            val navigationIcon = if (showBack) Icons.Default.ArrowBack else Icons.Default.Menu
            TopAppBar(
                navigationIcon = { IconButton(onBack) { Icon(navigationIcon) } },
                title = { Text(name) })
        },
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add) } }
    ) {
        Center {
            Text(text = "Counter: ${Counter(200)}")
        }
    }
}

@Suppress("SameParameterValue")
@Composable
private fun Counter(@Pivotal periodMs: Long): Int {
    var value by state { 0 }
    onActive {
        val mainHandler = Handler()
        var disposed = false
        onDispose { disposed = true }
        fun schedule() {
            mainHandler.postDelayed({
                value++
                if (!disposed) schedule()
            }, periodMs)
        }
        schedule()
    }
    return value
}
