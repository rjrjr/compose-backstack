package com.zachklipp.compose.backstack

import androidx.compose.runtime.savedinstancestate.UiSavedStateRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SavedStateHolderTest {

    @Test
    fun `saves and restores`() {
        val parent = UiSavedStateRegistry(restoredValues = null, canBeSaved = { true })
        val holder = SavedStateHolder("pk")

        var registry = holder.updateAndReturnRegistry(parent, isVisible = true)
        registry.registerProvider("ck") { "value" }
        registry = holder.updateAndReturnRegistry(parent, isVisible = false)
        registry.unregisterProvider("ck")
        registry = holder.updateAndReturnRegistry(parent = parent, isVisible = true)

        assertThat(registry.consumeRestored("ck")).isEqualTo("value")
    }

    @Test
    fun `restores from initial values`() {
        val restoredValues = mutableMapOf("pk" to mapOf("ck" to "value"))
        val parent = UiSavedStateRegistry(restoredValues = restoredValues, canBeSaved = { true })
        val holder = SavedStateHolder("pk")

        val registry = holder.updateAndReturnRegistry(parent = parent, isVisible = true)

        assertThat(registry.consumeRestored("ck")).isEqualTo("value")
    }

    @Test
    fun `doesn't save unregistered providers`() {
        val parent = UiSavedStateRegistry(restoredValues = null, canBeSaved = { true })
        val holder = SavedStateHolder("pk")

        var registry = holder.updateAndReturnRegistry(parent, isVisible = true)
        registry.registerProvider("key") { "value" }
        registry.unregisterProvider("key")
        holder.updateAndReturnRegistry(parent, isVisible = false)
        registry = holder.updateAndReturnRegistry(parent, isVisible = true)

        assertThat(registry.consumeRestored("key")).isNull()
    }

    @Test
    fun `preserves unrestored values from previous save`() {
        val restoredValues = mutableMapOf("pk" to mapOf("old key" to "old value"))
        val parent = UiSavedStateRegistry(restoredValues = restoredValues, canBeSaved = { true })
        val holder = SavedStateHolder("pk")

        holder.updateAndReturnRegistry(parent, isVisible = true)
        // Performs the save without having consumed "old key".
        holder.updateAndReturnRegistry(parent, isVisible = false)
        val registry = holder.updateAndReturnRegistry(parent, isVisible = true)

        assertThat(registry.consumeRestored("old key")).isEqualTo("old value")
    }

    @Test
    fun `cleans up restored values from previous save`() {
        val restoredValues = mutableMapOf("pk" to mapOf("old key" to "old value"))
        val parent = UiSavedStateRegistry(restoredValues = restoredValues, canBeSaved = { true })
        val holder = SavedStateHolder("pk")

        var registry = holder.updateAndReturnRegistry(parent, isVisible = true)
        val oldValue = registry.consumeRestored("old key")
        holder.updateAndReturnRegistry(parent, isVisible = false)
        registry = holder.updateAndReturnRegistry(parent, isVisible = true)

        assertThat(oldValue).isEqualTo("old value")
        assertThat(registry.consumeRestored("old key")).isNull()
    }

    @Test
    fun `parent saves contain values from the currently visible screen`() {
        val parent = UiSavedStateRegistry(restoredValues = null, canBeSaved = { true })
        val holder = SavedStateHolder("pk")

        val registry = holder.updateAndReturnRegistry(parent, isVisible = true)
        registry.registerProvider("ck") { "value" }

        val values = parent.performSave()
        assertThat(values["pk"]).isEqualTo(mapOf("ck" to "value"))
    }

    @Test
    fun `parent saves contain values from non-visible screens`() {
        val parent = UiSavedStateRegistry(restoredValues = null, canBeSaved = { true })
        val holder = SavedStateHolder("pk")

        var registry = holder.updateAndReturnRegistry(parent, isVisible = true)
        registry.registerProvider("ck") { "value" }
        registry = holder.updateAndReturnRegistry(parent, isVisible = false)
        registry.unregisterProvider("ck")

        val values = parent.performSave()
        assertThat(values["pk"]).isEqualTo(mapOf("ck" to "value"))
    }

    @Test
    fun `parent saves contain values from nested children`() {
        val topStateHolder = SavedStateHolder("pk1")
        val middleStateHolder = SavedStateHolder("pk2")

        val topRegistry = UiSavedStateRegistry(restoredValues = null, canBeSaved = { true })
        val middleRegistry = topStateHolder.updateAndReturnRegistry(topRegistry, isVisible = true)
        val bottomRegistry = middleStateHolder.updateAndReturnRegistry(middleRegistry, isVisible = true)

        middleRegistry.registerProvider("ck1") { "middle value" }
        bottomRegistry.registerProvider("ck2") { "bottom value" }

        val values = topRegistry.performSave()
        assertThat(values["pk1"]).isEqualTo(mapOf(
            "ck1" to "middle value",
            "pk2" to mapOf("ck2" to "bottom value")
        ))
    }
}
