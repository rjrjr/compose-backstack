@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.compose.backstack.sample

import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.foundation.Box
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.Dialog
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.LayoutAspectRatio
import androidx.ui.layout.LayoutGravity
import androidx.ui.layout.LayoutWidth
import androidx.ui.layout.Row
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.ArrowDropDown
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.unit.dp

/**
 * Rough implementation of the Android Spinner widget.
 */
@Composable fun <T : Any> Spinner(
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
          Box(modifier = LayoutFlexible(1f)) {
            drawItem(selectedItem)
          }
          Box(modifier = LayoutWidth(48.dp) + LayoutAspectRatio(1f) + LayoutGravity.Center) {
            DrawVector(vectorImage = Icons.Default.ArrowDropDown)
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
