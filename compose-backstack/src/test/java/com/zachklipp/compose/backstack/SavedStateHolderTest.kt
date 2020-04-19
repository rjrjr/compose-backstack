package com.zachklipp.compose.backstack

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SavedStateHolderTest {

    @Test
    fun `saves and restores`() {
        val holder = SavedStateHolder(canBeSaved = { true }, values = mutableMapOf())

        holder.setScreenVisibility(true)
        holder.registry.registerProvider("key") { "value" }
        holder.setScreenVisibility(false)
        holder.registry.unregisterProvider("key")
        holder.setScreenVisibility(true)

        assertThat(holder.registry.consumeRestored("key")).isEqualTo("value")
    }

    @Test
    fun `restores from initial values`() {
        val holder =
            SavedStateHolder(canBeSaved = { true }, values = mutableMapOf("key" to "value"))

        holder.setScreenVisibility(true)

        assertThat(holder.registry.consumeRestored("key")).isEqualTo("value")
    }

    @Test
    fun `doesn't save unregistered providers`() {
        val holder = SavedStateHolder(canBeSaved = { true }, values = mutableMapOf())

        holder.setScreenVisibility(true)
        holder.registry.registerProvider("key") { "value" }
        holder.registry.unregisterProvider("key")
        holder.setScreenVisibility(false)
        holder.setScreenVisibility(true)

        assertThat(holder.registry.consumeRestored("key")).isNull()
    }

    @Test
    fun `preserves unrestored values from previous save`() {
        val holder =
            SavedStateHolder(canBeSaved = { true }, values = mutableMapOf("old key" to "old value"))

        holder.setScreenVisibility(true)
        // Performs the save without having consumed "old key".
        holder.setScreenVisibility(false)
        holder.setScreenVisibility(true)

        assertThat(holder.registry.consumeRestored("old key")).isEqualTo("old value")
    }
}
