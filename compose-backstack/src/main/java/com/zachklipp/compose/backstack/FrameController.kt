package com.zachklipp.compose.backstack

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.zachklipp.compose.backstack.FrameController.BackstackFrame

/**
 * A stable object that processes changes to a [Backstack]'s list of screen keys, determining which
 * screens should be actively composed at any given time, and tweaking their appearance by applying
 * [Modifier]s.
 *
 * The [Backstack] composable will notify its controller whenever the backstack changes by calling
 * [updateBackstack], but the controller is in full control of when those changes actually get
 * reflected in the composition. For example, a controller may choose to keep some screens around
 * for a while, even after they're removed from the backstack, in order to animate their removal.
 */
@Stable
interface FrameController<T : Any> {

  /**
   * The frames that are currently being active. All active frames will be composed. When a frame
   * that is in the backstack stops appearing in this list, its state will be saved.
   *
   * Should be backed by either a `MutableState<List<T>>` or a `MutableStateList<T>`. This property
   * will not be read until after [updateBackstack] is called at least once.
   */
  val activeFrames: List<BackstackFrame<T>>

  /**
   * Notifies the controller that a new backstack was passed in. This should probably result in the
   * [activeFrames] being updated to show new keys or hide old ones, although the controller may
   * choose to do that later (e.g. if one of the active frames is currently being animated).
   *
   * [keys] will always contain at least one element.
   */
  fun updateBackstack(keys: List<T>)

  /**
   * A frame controlled by a [FrameController], to be shown by [Backstack].
   */
  @Immutable
  data class BackstackFrame<out T : Any>(
    val key: T,
    val modifier: Modifier = Modifier
  )
}

/**
 * Returns a [FrameController] that always just shows the top frame without any special effects.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> NoopFrameController(): FrameController<T> = NoopFrameController as FrameController<T>

private object NoopFrameController : FrameController<Any> {
  private var topFrame by mutableStateOf<BackstackFrame<Any>?>(null)

  override val activeFrames: List<BackstackFrame<Any>>
    get() = topFrame?.let { listOf(it) } ?: emptyList()

  override fun updateBackstack(keys: List<Any>) {
    topFrame = BackstackFrame(keys.last())
  }
}
