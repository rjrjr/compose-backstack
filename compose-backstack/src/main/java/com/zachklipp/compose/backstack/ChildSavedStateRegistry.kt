package com.zachklipp.compose.backstack

import androidx.compose.Composable
import androidx.compose.remember
import androidx.ui.savedinstancestate.UiSavedStateRegistry
import androidx.ui.savedinstancestate.UiSavedStateRegistryAmbient
import androidx.ui.savedinstancestate.rememberSavedInstanceState

/**
 * Returns a [UiSavedStateRegistry] that will automatically save values from all its registered
 * providers whenever [childWillBeComposed] transitions from true to false, and make those values available
 * to be restored when [childWillBeComposed] transitions from false to true.
 */
@Composable
@Suppress("RemoveExplicitTypeArguments")
fun ChildSavedStateRegistry(childWillBeComposed: Boolean): UiSavedStateRegistry {
    val parentRegistry = UiSavedStateRegistryAmbient.current

    // This map holds all the savedInstanceState for this screen as long as it exists
    // in the backstack. When the screen is hidden, we will cache its state providers
    // into this map before removing it from the composition. This cache will in turn
    // be persisted into and restored from the parent UiSavedStateRegistry.
    val values = rememberSavedInstanceState<MutableMap<String, Any>> { mutableMapOf() }
    val holder = remember {
        // If there's no registry available, then we won't be restored anyway so there are no
        // serializability restrictions on saved values.
        val canBeSaved: (Any) -> Boolean = parentRegistry?.let { it::canBeSaved } ?: { true }
        SavedStateHolder(canBeSaved, values)
    }
    holder.setScreenVisibility(childWillBeComposed)
    return holder.registry
}

internal class SavedStateHolder(
    private val canBeSaved: (Any) -> Boolean,
    private var values: Map<String, Any>
) {
    var registry: UiSavedStateRegistry = createRegistry()
        private set
    private var isScreenVisible = false

    /**
     * Tracks the visibility of the screen this class holds the state for and returns either the
     * [UiSavedStateRegistry] if visible, or null if not visible.
     *
     * When [isVisible] transitions from false to true, a new registry will be created that can will
     * restore from previously-saved values.
     *
     * When [isVisible] transitions from true to false, the existing registry will be used to save
     * all values.
     */
    fun setScreenVisibility(isVisible: Boolean) {
        if (isVisible == this.isScreenVisible) return
        this.isScreenVisible = isVisible

        if (!isVisible) {
            // This will automatically preserve any values that were passed into the factory
            // function but not consumed.
            values = registry.performSave()
        } else {
            // Recreate the registry so the most recently-saved values will be used to restore.
            // The UiSavedStateRegistry function makes a defensive copy of the passed-in map, so
            // it needs to be recreated on every restoration.
            registry = createRegistry()
        }
    }

    private fun createRegistry() = UiSavedStateRegistry(values, canBeSaved)
}
