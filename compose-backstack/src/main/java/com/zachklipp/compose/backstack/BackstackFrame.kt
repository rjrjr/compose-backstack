package com.zachklipp.compose.backstack

import androidx.compose.runtime.Composable

/**
 * Models a frame in a Backstack, with a unique [key] to identify it,
 * and a [Content] function to display it.
 *
 * Use [toBackstackModel] to associate any [List] of UI models with
 * with [Composable] code that can display them, suitable for use
 * with [Backstack].
 */
interface BackstackFrame<out K : Any> {
  val key: K
  @Composable fun Content()
}

inline fun <reified M : Any, K : Any> BackstackFrame(
  model: M,
  key: K,
  crossinline content: @Composable (M) -> Unit
): BackstackFrame<K> = object : BackstackFrame<K> {
  override val key = key

  @Composable override fun Content() {
    content(model)
  }
}

inline fun <reified M: Any> List<M>.toBackstackModel(
  crossinline content: @Composable (M) -> Unit
): List<BackstackFrame<M>>{
  return toBackstackModel(
    getKey =  { it },
    content = content
  )
}

inline fun <reified M : Any, K : Any> List<M>.toBackstackModel(
  getKey: (M) -> K,
  crossinline content: @Composable (M) -> Unit
): List<BackstackFrame<K>> {
  return map { BackstackFrame(it, getKey(it), content) }
}
