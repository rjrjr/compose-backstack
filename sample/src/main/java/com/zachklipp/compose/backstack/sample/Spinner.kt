@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.compose.backstack.sample

import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.foundation.Box
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.Dialog
import androidx.ui.graphics.vector.drawVector
import androidx.ui.layout.*
import androidx.ui.material.Surface
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.ArrowDropDown
import androidx.ui.material.ripple.Ripple
import androidx.ui.unit.dp

/**
 * Rough implementation of the Android Spinner widget.
 */
@Composable
fun <T : Any> Spinner(
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
                    Box(modifier = LayoutWeight(1f)) {
                        drawItem(selectedItem)
                    }
                    Box(
                        modifier = LayoutWidth(48.dp) +
                                LayoutAspectRatio(1f) +
                                LayoutGravity.Center +
                                drawVector(vectorImage = Icons.Default.ArrowDropDown)
                    ) {
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
