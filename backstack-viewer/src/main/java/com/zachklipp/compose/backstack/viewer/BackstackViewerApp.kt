@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry", "FunctionName")

package com.zachklipp.compose.backstack.viewer

import androidx.animation.TweenBuilder
import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.remember
import androidx.ui.core.Text
import androidx.ui.foundation.Box
import androidx.ui.foundation.DrawBorder
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
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

private val BUILTIN_BACKSTACK_TRANSITIONS = listOf(
    "Slide" to Slide,
    "Crossfade" to Crossfade
)

//@Preview
//@Composable
//private fun BackstackViewerAppPreview() {
//    BackstackViewerApp()
//}

@Model
private class AppModel(
    var namedTransitions: List<Pair<String, BackstackTransition>>,
    var backstacks: List<Pair<String, List<String>>>,
    var selectedTransition: Pair<String, BackstackTransition> = namedTransitions.first(),
    var selectedBackstack: Pair<String, List<String>> = backstacks.first(),
    var slowAnimations: Boolean = false,
    var inspectionEnabled: Boolean = false
) {
    val bottomScreen get() = selectedBackstack.second.first()

    fun pushScreen(screen: String) {
        val newBackstack = selectedBackstack.second + screen
        selectedBackstack = newBackstack.joinToString() to newBackstack
    }

    fun popScreen() {
        if (selectedBackstack.second.size > 1) {
            val newBackstack = selectedBackstack.second.dropLast(1)
            selectedBackstack = newBackstack.joinToString() to newBackstack
        }
    }
}

/**
 * Pre-fab Composable application that can be used to view various [BackstackTransition]s. Intended
 * to be the root content view of an activity.
 *
 * This composable can be used to interact with your own custom transitions. It lets the user select
 * from the available transitions, and then toggle between various pre-defined backstacks. The
 * backstacks are simply strings, and each string will be rendered as a Materialy [Scaffold] with
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
) {
    MaterialTheme(colors = darkColorPalette()) {
        Surface {
            Box(padding = 16.dp) {
                val model = remember(namedCustomTransitions, prefabBackstacks) {
                    AppModel(
                        namedTransitions = namedCustomTransitions + BUILTIN_BACKSTACK_TRANSITIONS,
                        backstacks = (prefabBackstacks?.takeUnless { it.isEmpty() }
                            ?: DEFAULT_BACKSTACKS)
                            .map { it.joinToString() to it }
                    )
                }

                Column(modifier = LayoutSize.Fill) {
                    AppControls(model)
                    Spacer(LayoutHeight(24.dp))
                    AppScreens(model)
                }
            }
        }
    }
}

@Composable
private fun AppControls(model: AppModel) {
    Spinner(
        items = model.namedTransitions,
        selectedItem = model.selectedTransition,
        onSelected = { model.selectedTransition = it }
    ) {
        ListItem(text = "${it.first} Transition")
    }

    Row {
        Text("Slow animations: ", modifier = LayoutGravity.Center)
        Switch(model.slowAnimations, onCheckedChange = { model.slowAnimations = it })
    }

    Row {
        Text("Inspect (pinch + drag): ", modifier = LayoutGravity.Center)
        Switch(model.inspectionEnabled, onCheckedChange = { model.inspectionEnabled = it })
    }

    RadioGroup {
        model.backstacks.forEach { backstack ->
            RadioGroupTextItem(
                text = backstack.first,
                textStyle = MaterialTheme.typography().body1,
                selected = backstack == model.selectedBackstack,
                onSelect = { model.selectedBackstack = backstack }
            )
        }
    }
}

@Composable
private fun AppScreens(model: AppModel) {
    val animation = if (model.slowAnimations) {
        remember {
            TweenBuilder<Float>().apply {
                duration = 2000
            }
        }
    } else null

    MaterialTheme(colors = lightColorPalette()) {
        InspectionGestureDetector(enabled = model.inspectionEnabled) {
            Backstack(
                backstack = model.selectedBackstack.second,
                transition = model.selectedTransition.second,
                animationBuilder = animation,
                modifier = LayoutSize.Fill + DrawBorder(size = 3.dp, color = Color.Red),
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
