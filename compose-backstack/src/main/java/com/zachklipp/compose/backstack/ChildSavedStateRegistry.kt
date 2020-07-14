package com.zachklipp.compose.backstack

import androidx.compose.*
import androidx.ui.savedinstancestate.UiSavedStateRegistry
import androidx.ui.savedinstancestate.UiSavedStateRegistryAmbient

/**
 * Returns a [UiSavedStateRegistry] that will automatically save values from all its registered
 * providers whenever [childWillBeComposed] transitions from true to false, and make those values available
 * to be restored when [childWillBeComposed] transitions from false to true.
 */
@OptIn(ExperimentalComposeApi::class)
@Composable
fun ChildSavedStateRegistry(childWillBeComposed: Boolean): UiSavedStateRegistry {
    val parent = UiSavedStateRegistryAmbient.current
    val key = currentComposer.currentCompoundKeyHash.toString()
    val holder = remember { SavedStateHolder(key) }
    return holder.updateAndReturnRegistry(parent, childWillBeComposed)
}

internal class SavedStateHolder(private val key: String) : CompositionLifecycleObserver {
    private var parent: UiSavedStateRegistry? = null
    private var isScreenVisible = false
    private var values: Map<String, Any>? = null
    private var registry: UiSavedStateRegistry = createRegistry()

    /**
     * Manages the visibility of the screen and saves its state whenever [isVisible] transitions
     * from true to false, or whenever the Android OS triggers an onSaveInstanceState dispatch.
     *
     * Returns a [UiSavedStateRegistry] containing the most recently saved values.
     */
    @Suppress("UNCHECKED_CAST")
    fun updateAndReturnRegistry(
        parent: UiSavedStateRegistry?,
        isVisible: Boolean
    ): UiSavedStateRegistry {
        // When values is null, try restore any previously saved values (or fallback to an empty
        // map). Once values is non-null, it'll hold the all the latest saved values for the screen.
        values = values ?: parent?.consumeRestored(key) as Map<String, Any>? ?: emptyMap()

        val oldParent = this.parent
        this.parent = parent

        // Use an identity comparison here for safety because UiSavedStateRegistry is an interface
        // and custom implementations might have their own custom equals implementation. And if we
        // call unregisterProvider on an UiSavedStateRegistry where `key` isn't already registered,
        // then it'll crash.
        if (parent !== oldParent) {
            oldParent?.unregisterProvider(key)
            parent?.registerProvider(key) {
                if (isScreenVisible) {
                    // Save the screen if it is visible right now. If it is invisible, then it's
                    // values were already saved upon leaving the screen.
                    values = registry.performSave()
                }
                values
            }
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

    override fun onEnter() {
        // No-op
    }

    override fun onLeave() {
        parent?.unregisterProvider(key)
    }

    private fun createRegistry(): UiSavedStateRegistry {
        // If there's no registry available, then we won't be restored anyway so there are no
        // serializability restrictions on saved values.
        val canBeSaved: (Any) -> Boolean = parent?.let { it::canBeSaved } ?: { true }
        return UiSavedStateRegistry(values, canBeSaved)
    }
}
