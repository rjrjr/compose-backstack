@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry", "FunctionName")

package com.zachklipp.compose.backstack.sample

import androidx.animation.TweenBuilder
import androidx.compose.Composable
import androidx.compose.remember
import androidx.compose.state
import androidx.ui.core.DrawModifier
import androidx.ui.core.Modifier
import androidx.ui.core.Text
import androidx.ui.foundation.DrawBorder
import androidx.ui.graphics.Canvas
import androidx.ui.graphics.Color
import androidx.ui.graphics.withSave
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Density
import androidx.ui.unit.PxSize
import androidx.ui.unit.dp
import com.zachklipp.compose.backstack.Backstack
import com.zachklipp.compose.backstack.BackstackTransition
import com.zachklipp.compose.backstack.BackstackTransition.Crossfade
import com.zachklipp.compose.backstack.BackstackTransition.Slide
import kotlin.math.pow

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

//@Composable
//fun App() {
//    Center {
//        Text("Hi")
//    }
//}

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

private object FancyTransition : BackstackTransition {
    @Composable
    override fun modifierForScreen(
        visibility: Float,
        isTop: Boolean
    ): Modifier {
        return if (isTop) {
            Slide.modifierForScreen(visibility.pow(1.1f), isTop) +
                    Crossfade.modifierForScreen(visibility.pow(.1f), isTop)
        } else {
            ScaleModifier(visibility.pow(.1f)) +
                    Crossfade.modifierForScreen(visibility.pow(.5f), isTop)
        }
    }

    private class ScaleModifier(private val factor: Float) : DrawModifier {
        override fun draw(
            density: Density,
            drawContent: () -> Unit,
            canvas: Canvas,
            size: PxSize
        ) {
            val halfWidth = size.width.value / 2
            val halfHeight = size.height.value / 2

            canvas.withSave {
                canvas.translate(halfWidth, halfHeight)
                canvas.scale(factor)
                canvas.translate(-halfWidth, -halfHeight)
                drawContent()
            }
        }
    }
}
