@file:Suppress("FunctionName")

package com.zachklipp.compose.backstack.viewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
            Icon(navigationIcon, contentDescription = "Back")
          }
        },
        title = { Text("Screen $name") })
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onAdd, modifier = Modifier.testTag(addTestTag(name))) {
        Icon(Icons.Default.Add, contentDescription = "Add screen")
      }
    }
  ) {
    Text(
      text = "Counter: ${rememberCounter(200)}",
      modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)
    )
  }
}

@Suppress("SameParameterValue")
@Composable
private fun rememberCounter(periodMs: Long): Int = key(periodMs) {
  // If the screen is temporarily removed from the composition, the counter will effectively
  // be "paused": it will stop incrementing, but will resume from its last value when restored to
  // the composition.
  var value by rememberSaveable { mutableStateOf(0) }
  LaunchedEffect(periodMs) {
    while (isActive) {
      delay(periodMs)
      value++
    }
  }
  return@key value
}
