@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.compose.backstack

import androidx.compose.Composable
import androidx.ui.core.Modifier

typealias ModifiedScreen<T> = Pair<T, Modifier>

/**
 * Controls how a stack of screens is displayed by [mapping][processStack] a list of keys (the
 * backstack) to a list of [Modifier]s paired with the keys to apply them to. The returned list may
 * contain keys that are not present in the input list, but must not contain duplicate keys. Any
 * keys present in the returned list will have their screens removed from the composition.
 */
interface BackstackProcessor {

    /**
     * Given a list of all the current screen keys, as passed to [Backstack], returns a filtered
     * list of only the keys for the screens to actually show, paired with the composable function
     * to draw that screen.
     */
    @Composable
    fun processStack(keys: List<Any>): List<ModifiedScreen<Any>>
}
