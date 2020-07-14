@file:Suppress("FunctionName")

package com.zachklipp.compose.backstack.viewer

import android.os.Handler
import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.testTag
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.wrapContentSize
import androidx.ui.material.FloatingActionButton
import androidx.ui.material.IconButton
import androidx.ui.material.Scaffold
import androidx.ui.material.TopAppBar
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.Add
import androidx.ui.material.icons.filled.ArrowBack
import androidx.ui.material.icons.filled.Menu
import androidx.ui.savedinstancestate.savedInstanceState
import androidx.ui.tooling.preview.Preview

internal fun addTestTag(screen: String) = "add screen to $screen"
internal fun backTestTag(screen: String) = "go back from $screen"

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
        topBar = {
            val navigationIcon = if (showBack) Icons.Default.ArrowBack else Icons.Default.Menu
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag(backTestTag(name))) {
                        Icon(navigationIcon)
                    }
                },
                title = { Text("Screen $name") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, modifier = Modifier.testTag(addTestTag(name))) {
                Icon(Icons.Default.Add)
            }
        }
    ) {
        Text(
            text = "Counter: ${Counter(200)}",
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
        )
    }
}

@Suppress("SameParameterValue")
@Composable
private fun Counter(periodMs: Long): Int = key(periodMs) {
    // If the screen is temporarily removed from the composition, the counter will effectively
    // be "paused": it will stop incrementing, but will resume from its last value when restored to
    // the composition.
    var value by savedInstanceState { 0 }
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
    return@key value
}
