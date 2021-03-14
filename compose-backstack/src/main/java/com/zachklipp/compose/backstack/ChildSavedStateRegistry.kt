package com.zachklipp.compose.backstack

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry.Entry

/**
 * Returns a [SaveableStateRegistry] that will automatically save values from all its registered
 * providers whenever [childWillBeComposed] transitions from true to false, and make those values available
 * to be restored when [childWillBeComposed] transitions from false to true.
 */
@Suppress("ComposableNaming")
@OptIn(ExperimentalComposeApi::class)
@Composable
fun ChildSavedStateRegistry(childWillBeComposed: Boolean): SaveableStateRegistry {
  val parent = LocalSaveableStateRegistry.current
  val key = currentCompositeKeyHash.toString()
  val holder = remember { SavedStateHolder(key) }
  return holder.updateAndReturnRegistry(parent, childWillBeComposed)
}

internal class SavedStateHolder(private val key: String) : RememberObserver {
  private var parent: SaveableStateRegistry? = null
  private var isScreenVisible = false
  private var values: Map<String, List<Any?>>? = null
  private var registry: SaveableStateRegistry = createRegistry()
  private var valueProvider: () -> Any? = {
    if (isScreenVisible) {
      // Save the screen if it is visible right now. If it is invisible, then it's
      // values were already saved upon leaving the screen.
      values = registry.performSave()
    }
    values
  }

  private var entryInParent: Entry? = null

  /**
   * Manages the visibility of the screen and saves its state whenever [isVisible] transitions
   * from true to false, or whenever the Android OS triggers an onSaveInstanceState dispatch.
   *
   * Returns a [UiSavedStateRegistry] containing the most recently saved values.
   */
  @Suppress("UNCHECKED_CAST")
  fun updateAndReturnRegistry(
    parent: SaveableStateRegistry?,
    isVisible: Boolean
  ): SaveableStateRegistry {
    // When values is null, try restore any previously saved values (or fallback to an empty
    // map). Once values is non-null, it'll hold the all the latest saved values for the screen.
    values = values ?: parent?.consumeRestored(key) as Map<String, List<Any?>>? ?: emptyMap()

    val oldParent = this.parent
    this.parent = parent

    // Use an identity comparison here for safety because UiSavedStateRegistry is an interface
    // and custom implementations might have their own custom equals implementation. And if we
    // call unregisterProvider on an UiSavedStateRegistry where `key` isn't already registered,
    // then it'll crash.
    if (parent !== oldParent) {
      entryInParent?.unregister()
      entryInParent = parent?.registerProvider(key, valueProvider)
    }

    if (isVisible == this.isScreenVisible) return registry
    this.isScreenVisible = isVisible

    if (!isVisible) {
      // Perform save on this screen just before it leaves the composition.
      values = registry.performSave()
    } else {
      // Recreate the registry so the most recently-saved values will be used to restore.
      // The UiSavedStateRegistry function makes a defensive copy of the passed-in map, so
      // it needs to be recreated on every restoration.
      registry = createRegistry()
    }

    return registry
  }

  override fun onAbandoned() {
    entryInParent?.unregister()
  }

  override fun onRemembered() {
    // No-op
  }

  override fun onForgotten() {
    entryInParent?.unregister()
  }

  private fun createRegistry(): SaveableStateRegistry {
    // If there's no registry available, then we won't be restored anyway so there are no
    // serializability restrictions on saved values.
    val canBeSaved: (Any) -> Boolean = parent?.let { it::canBeSaved } ?: { true }
    return SaveableStateRegistry(values, canBeSaved)
  }
}
