@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.compose.backstack.viewer

import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.aspectRatio
import androidx.ui.layout.preferredWidth
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.darkColorPalette
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.ArrowDropDown
import androidx.ui.material.lightColorPalette
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
    Row(Modifier.clickable(onClick = { isOpen = !isOpen })) {
        Box(modifier = Modifier.weight(1f).gravity(Alignment.CenterVertically)) {
            drawItem(selectedItem)
        }
        Icon(
            Icons.Default.ArrowDropDown,
            modifier = Modifier.preferredWidth(48.dp).aspectRatio(1f)
        )
    }

    if (isOpen) {
        // TODO use DropdownPopup.
        Dialog(onCloseRequest = { isOpen = false }) {
            Surface(elevation = 1.dp) {
                Column {
                    for (item in items) {
                        Box(Modifier.clickable(onClick = {
                            isOpen = false
                            if (item != selectedItem) onSelected(item)
                        })) {
                            drawItem(item)
                        }
                    }
                }
            }
        }
    }
}
