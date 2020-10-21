package androidx.compose.runtime.savedinstancestate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.key
import androidx.compose.runtime.onActive
import androidx.compose.runtime.remember

// COPIED FROM https://android-review.googlesource.com/c/platform/frameworks/support/+/1425674

/**
 * Allows to save the state defined with [savedInstanceState] and [rememberSavedInstanceState]
 * for the subtree before disposing it to make it possible to compose it back next time with the
 * restored state. It allows different navigation patterns to keep the ui state like scroll
 * position for the currently not composed screens from the backstack.
 *
 * @sample androidx.compose.runtime.savedinstancestate.samples.SimpleNavigationWithRestorableStateSample
 *
 * The content should be composed using [withRestorableState] while providing a key representing
 * this content. Next time [withRestorableState] will be used with the same key its state will be
 * restored.
 *
 * @param T type of the keys. Note that on Android you can only use types which can be stored
 * inside the Bundle.
 */
interface RestorableStateHolder<T : Any> {
  /**
   * Put your content associated with a [key] inside the [content]. This will automatically
   * save all the states defined with [savedInstanceState] and [rememberSavedInstanceState]
   * before disposing the content and will restore the states when you compose with this key
   * again.
   *
   * @param key to be used for saving and restoring the states for the subtree. Note that on
   * Android you can only use types which can be stored inside the Bundle.
   */
  @Composable
  fun withRestorableState(key: T, content: @Composable () -> Unit)
}

/**
 * Creates and remembers the instance of [RestorableStateHolder].
 *
 * @param T type of the keys. Note that on Android you can only use types which can be stored
 * inside the Bundle.
 */
@Composable
fun <T : Any> rememberRestorableStateHolder(): RestorableStateHolder<T> =
  rememberSavedInstanceState(
    saver = DisposedCompositionsStateHolderImpl.Saver()
  ) {
    DisposedCompositionsStateHolderImpl<T>()
  }.apply {
    parentSavedStateRegistry = UiSavedStateRegistryAmbient.current
  }

private class DisposedCompositionsStateHolderImpl<T : Any>(
  private val savedStates: MutableMap<T, Map<String, List<Any?>>> = mutableMapOf()
) : RestorableStateHolder<T> {
  private var registries = mutableMapOf<T, UiSavedStateRegistry>()
  var parentSavedStateRegistry: UiSavedStateRegistry? = null

  @Composable
  override fun withRestorableState(key: T, content: @Composable () -> Unit) {
    key(key) {
      val registry = remember {
        require(parentSavedStateRegistry?.canBeSaved(key) ?: true) {
          "Type of the key used for withRestorableState is not supported. On Android " +
              "you can only use types which can be stored inside the Bundle."
        }
        UiSavedStateRegistry(savedStates[key]) {
          parentSavedStateRegistry?.canBeSaved(it) ?: true
        }
      }
      Providers(UiSavedStateRegistryAmbient provides registry, children = content)
      onActive {
        require(!registries.contains(key))
        savedStates.remove(key)
        registries[key] = registry
        onDispose {
          savedStates[key] = registry.performSave()
          registries.remove(key)
        }
      }
    }
  }

  private fun saveAll(): MutableMap<T, Map<String, List<Any?>>> {
    val map = savedStates.toMutableMap()
    registries.forEach { (key, registry) ->
      map[key] = registry.performSave()
    }
    return map
  }

  companion object {
    private val Saver: Saver<DisposedCompositionsStateHolderImpl<*>, *> = Saver(
      save = { it.saveAll() },
      restore = { DisposedCompositionsStateHolderImpl(it) }
    )

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> Saver() = Saver as Saver<DisposedCompositionsStateHolderImpl<T>, *>
  }
}
