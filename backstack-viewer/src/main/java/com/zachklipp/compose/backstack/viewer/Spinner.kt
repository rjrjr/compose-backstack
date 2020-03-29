@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.compose.backstack.viewer

import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.foundation.Box
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.Dialog
import androidx.ui.foundation.Icon
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.darkColorPalette
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.ArrowDropDown
import androidx.ui.material.lightColorPalette
import androidx.ui.material.ripple.Ripple
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp

@Preview
@Composable
private fun SpinnerPreviewLight() {
    MaterialTheme(colors = lightColorPalette()) {
        Surface {
            Spinner(listOf("foo"), selectedItem = "foo", onSelected = {}) { Text(it) }
        }
    }
}

@Preview
@Composable
private fun SpinnerPreviewDark() {
    MaterialTheme(colors = darkColorPalette()) {
        Surface {
            Spinner(listOf("foo"), selectedItem = "foo", onSelected = {}) { Text(it) }
        }
    }
}

/**
 * Rough implementation of the Android Spinner widget.
 */
@Composable
internal fun <T : Any> Spinner(
    items: List<T>,
    selectedItem: T,
    onSelected: (item: T) -> Unit,
    drawItem: @Composable() (T) -> Unit
) {
    if (items.isEmpty()) return

    var isOpen by state { false }

    // Always draw the selected item.
    Container {
        Ripple(bounded = true) {
            Clickable(onClick = { isOpen = !isOpen }) {
                Row {
                    Box(modifier = LayoutWeight(1f) + LayoutGravity.Center) {
                        drawItem(selectedItem)
                    }
                    Box(
                        modifier = LayoutWidth(48.dp) +
                                LayoutAspectRatio(1f),
                        gravity = Alignment.Center
                    ) {
                        Icon(icon = Icons.Default.ArrowDropDown)
                    }
                }
            }
        }

        if (isOpen) {
            // TODO use DropdownPopup.
            Dialog(onCloseRequest = { isOpen = false }) {
                Surface(elevation = 1.dp) {
                    Column {
                        for (item in items) {
                            Ripple(bounded = true) {
                                Clickable(
                                    onClick = {
                                        isOpen = false
                                        if (item != selectedItem) onSelected(item)
                                    }) {
                                    drawItem(item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
