package com.zachklipp.compose.backstack

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.zachklipp.compose.backstack.FrameController.FrameAndModifier

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
interface FrameController<K : Any> {

  /**
   * The frames that are currently being active. All active frames will be composed. When a frame
   * that is in the backstack stops appearing in this list, its state will be saved.
   *
   * Should be backed by either a [MutableState] or a [SnapshotStateList]. This property
   * will not be read until after [updateBackstack] is called at least once.
   */
  val activeFrames: List<FrameAndModifier<K>>

  /**
   * Notifies the controller that a new backstack was passed in. This method must initialize
   * [activeFrames] first time it's called, and subsequently should probably result in
   * [activeFrames] being updated to show new keys or hide old ones, although the controller may
   * choose to do that later (e.g. if one of the active frames is currently being animated).
   *
   * This method will be called _directly from the composition_ – it must not perform side effects
   * or update any state that is not backed by snapshot state objects (such as [MutableState]s,
   * lists created by [mutableStateListOf], etc.).
   *
   * @param frames The latest backstack passed to [Backstack]. Will always contain at least one
   * element.
   */
  fun updateBackstack(frames: List<BackstackFrame<K>>)

  /**
   * A frame controlled by a [FrameController], to be shown by [Backstack].
   */
  @Immutable
  data class FrameAndModifier<out K : Any>(
    val frame: BackstackFrame<K>,
    val modifier: Modifier = Modifier
  )
}

/**
 * Returns a [FrameController] that always just shows the top frame without any special effects.
 */
@Suppress("UNCHECKED_CAST")
fun <K : Any> NoopFrameController(): FrameController<K> = NoopFrameController as FrameController<K>

private object NoopFrameController : FrameController<Any> {
  private var topFrame by mutableStateOf<FrameAndModifier<Any>?>(null)

  override val activeFrames: List<FrameAndModifier<Any>>
    get() = topFrame?.let { listOf(it) } ?: emptyList()

  override fun updateBackstack(frames: List<BackstackFrame<Any>>) {
    topFrame = FrameAndModifier(frames.last())
  }
}
