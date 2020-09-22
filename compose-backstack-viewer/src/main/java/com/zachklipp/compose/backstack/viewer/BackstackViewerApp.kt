package com.zachklipp.compose.backstack.viewer

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.zachklipp.compose.backstack.Backstack
import com.zachklipp.compose.backstack.BackstackTransition
import com.zachklipp.compose.backstack.BackstackTransition.Crossfade
import com.zachklipp.compose.backstack.BackstackTransition.Slide
import com.zachklipp.compose.backstack.InspectionGestureDetector

private val DEFAULT_BACKSTACKS = listOf(
    listOf("one"),
    listOf("one", "two"),
    listOf("one", "two", "three")
)

private val BUILTIN_BACKSTACK_TRANSITIONS = listOf(Slide, Crossfade)
    .map { Pair(it::class.java.simpleName, it) }

@Preview
@Composable
private fun BackstackViewerAppPreview() {
    BackstackViewerApp()
}

/**
 * Pre-fab Composable application that can be used to view various [BackstackTransition]s. Intended
 * to be the root content view of an activity.
 *
 * This composable can be used to interact with your own custom transitions. It lets the user select
 * from the available transitions, and then toggle between various pre-defined backstacks. The
 * backstacks are simply strings, and each string will be rendered as a Material [Scaffold] with
 * that string, and a counter value that will automatically increase over time, to demonstrate
 * state retention. The demo screens also allow the user to push additional copies of the current
 * screen to the backstack, or pop the current screen.
 *
 * The `sample` module contains a sample app that uses this composable to demonstrate a custom
 * transition.
 *
 * @param namedCustomTransitions An optional list of [BackstackTransition]s to make available,
 * paired with the name to show them as in the UI.
 * @param prefabBackstacks An optional list of backstacks to allow the user to switch between. If
 * unspecified, defaults to a small list of backstacks containing screens "one", "two", and "three".
 */
@Composable
fun BackstackViewerApp(
    namedCustomTransitions: List<Pair<String, BackstackTransition>> = emptyList(),
    prefabBackstacks: List<List<String>>? = null
) = key(namedCustomTransitions, prefabBackstacks) {
    val model = AppModel.create(
        namedTransitions = namedCustomTransitions + BUILTIN_BACKSTACK_TRANSITIONS,
        prefabBackstacks = (prefabBackstacks?.takeUnless { it.isEmpty() } ?: DEFAULT_BACKSTACKS)
    )

    // When we're on the first screen, let the activity handle the back press.
    if (model.currentBackstack.size > 1) {
        OnBackPressed { model.popScreen() }
    }

    MaterialTheme(colors = darkColors()) {
        Surface {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                AppControls(model)
                Spacer(Modifier.preferredHeight(24.dp))
                AppScreens(model)
            }
        }
    }
}

@Composable
private fun AppControls(model: AppModel) {
    Spinner(
        items = model.namedTransitions.map { it.first },
        selectedItem = model.selectedTransition.first,
        onSelected = { model.selectTransition(it) }
    ) {
        ListItem(text = { Text("$it Transition") })
    }

    Row {
        Text("Slow animations: ", modifier = Modifier.align(Alignment.CenterVertically))
        Switch(model.slowAnimations, onCheckedChange = { model.slowAnimations = it })
    }

    Row {
        Text("Inspect (pinch + drag): ", modifier = Modifier.align(Alignment.CenterVertically))
        Switch(model.inspectionEnabled, onCheckedChange = { model.inspectionEnabled = it })
    }

    Column {
        model.prefabBackstacks.forEach { backstack ->
            RadioButton(
                text = backstack.joinToString(", "),
                selected = backstack == model.currentBackstack,
                onSelect = { model.currentBackstack = backstack }
            )
        }
    }
}

@Composable
private fun AppScreens(model: AppModel) {
    val animation = if (model.slowAnimations) {
        remember {
            TweenSpec<Float>(durationMillis = 2000)
        }
    } else null

    MaterialTheme(colors = lightColors()) {
        InspectionGestureDetector(enabled = model.inspectionEnabled) { inspectionParams ->
            Backstack(
                backstack = model.currentBackstack,
                transition = model.selectedTransition.second,
                animationBuilder = animation,
                modifier = Modifier.fillMaxSize().border(width = 3.dp, color = Color.Red),
                inspectionParams = inspectionParams,
                onTransitionStarting = { from, to, direction ->
                    println(
                        """
                          Transitioning $direction:
                            from: $from
                              to: $to
                        """.trimIndent()
                    )
                },
                onTransitionFinished = { println("Transition finished.") }
            ) { screen ->
                AppScreen(
                    name = screen,
                    showBack = screen != model.bottomScreen,
                    onAdd = { model.pushScreen("$screen+") },
                    onBack = model::popScreen
                )
            }
        }
    }
}

@Composable
private fun RadioButton(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier.selectable(
            selected = selected,
            onClick = { if (!selected) onSelect() }
        ),
        children = {
            Box {
                Row(Modifier.fillMaxWidth().padding(16.dp)) {
                    RadioButton(selected = selected, onClick = onSelect)
                    Text(
                        text = text,
                        style = MaterialTheme.typography.body1.merge(other = currentTextStyle()),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    )
}

@Composable
private fun OnBackPressed(onPressed: () -> Unit) {
    val context = ContextAmbient.current
    onCommit(context, onPressed) {
        val activity = context.findComponentActivity() ?: return@onCommit
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onPressed()
            }
        }
        activity.onBackPressedDispatcher.addCallback(callback)
        onDispose { callback.remove() }
    }
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? {
    return when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findComponentActivity()
        else -> null
    }
}
