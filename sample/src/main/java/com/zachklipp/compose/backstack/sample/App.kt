@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry", "FunctionName")

package com.zachklipp.compose.backstack.sample

import androidx.animation.TweenBuilder
import androidx.compose.Composable
import androidx.compose.remember
import androidx.compose.state
import androidx.ui.core.Text
import androidx.ui.foundation.DrawBorder
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.zachklipp.compose.backstack.Backstack
import com.zachklipp.compose.backstack.BackstackTransition.Crossfade
import com.zachklipp.compose.backstack.BackstackTransition.Slide

private val backstacks = listOf(
    listOf("one"),
    listOf("one", "two"),
    listOf("one", "two", "three"),
    listOf("two", "one")
).map { it.joinToString() to it }

private val backstackTransitions = listOf(
    "Slide" to Slide,
    "Crossfade" to Crossfade,
    "Fancy" to FancyTransition
)

@Composable
fun App() {
    MaterialTheme(colors = darkColorPalette()) {
        Surface {
            var selectedTransition by state { backstackTransitions.first() }
            var selectedBackstack by state { backstacks.first() }
            var slowAnimations by state { false }
            val animation = remember(slowAnimations) {
                if (slowAnimations) TweenBuilder<Float>().apply { duration = 2000 } else null
            }

            Column(modifier = LayoutSize.Fill) {
                Text("Backstack transition:")
                Spinner(
                    items = backstackTransitions,
                    selectedItem = selectedTransition,
                    onSelected = { selectedTransition = it }
                ) {
                    ListItem(text = it.first)
                }

                Row {
                    Text("Slow animations:", modifier = LayoutGravity.Center)
                    Switch(slowAnimations, onCheckedChange = { slowAnimations = it })
                }

                Text("Backstack:")
                RadioGroup {
                    backstacks.forEach { backstack ->
                        RadioGroupTextItem(
                            text = backstack.first,
                            textStyle = MaterialTheme.typography().body1,
                            selected = backstack == selectedBackstack,
                            onSelect = { selectedBackstack = backstack }
                        )
                    }
                }

                MaterialTheme(colors = lightColorPalette()) {
                    Backstack(
                        backstack = selectedBackstack.second,
                        transition = selectedTransition.second,
                        animationBuilder = animation,
                        modifier = LayoutSize.Fill +
                                LayoutPadding(24.dp) +
                                DrawBorder(size = 3.dp, color = Color.Red),
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
                            isLastScreen = screen == selectedBackstack.second.first(),
                            onAdd = {
                                val newBackstack = selectedBackstack.second + "$screen+"
                                selectedBackstack = newBackstack.joinToString() to newBackstack
                            },
                            onBack = {
                                if (selectedBackstack.second.size > 1) {
                                    val newBackstack = selectedBackstack.second.dropLast(1)
                                    selectedBackstack = newBackstack.joinToString() to newBackstack
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
