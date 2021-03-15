package com.zachklipp.compose.backstack

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import com.zachklipp.compose.backstack.FrameController.BackstackFrame
import com.zachklipp.compose.backstack.TransitionDirection.Backward
import com.zachklipp.compose.backstack.TransitionDirection.Forward
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

/**
 * Returns the default [AnimationSpec] used for [rememberTransitionController].
 */
@Composable fun defaultBackstackAnimation(): AnimationSpec<Float> {
  val context = LocalContext.current
  return TweenSpec(
    durationMillis = context.resources.getInteger(android.R.integer.config_shortAnimTime)
  )
}

/**
 * Returns a [FrameController] that will animate transitions between screens.
 *
 * @param transition The [BackstackTransition] that defines how to animate between screens when
 * the backstack changes. [BackstackTransition] contains a few simple pre-fab transitions.
 * @param animationSpec Defines the curve and speed of transition animations.
 * @param onTransitionStarting Callback that will be invoked before starting each transition.
 * @param onTransitionFinished Callback that will be invoked after each transition finishes.
 */
@Composable fun <T : Any> rememberTransitionController(
  transition: BackstackTransition = BackstackTransition.Slide,
  animationSpec: AnimationSpec<Float> = defaultBackstackAnimation(),
  onTransitionStarting: (from: List<T>, to: List<T>, TransitionDirection) -> Unit = { _, _, _ -> },
  onTransitionFinished: () -> Unit = {},
): FrameController<T> {
  val scope = rememberCoroutineScope()
  return remember { TransitionController<T>(scope) }.also {
    it.transition = transition
    it.animationSpec = animationSpec
    it.onTransitionStarting = onTransitionStarting
    it.onTransitionFinished = onTransitionFinished
  }
}

/**
 * A [FrameController] that implements transition modifiers specified by [BackstackTransition]s.
 *
 * @param scope The [CoroutineScope] used for animations.
 */
@VisibleForTesting(otherwise = PRIVATE)
internal class TransitionController<T : Any>(
  private val scope: CoroutineScope
) : FrameController<T> {

  /**
   * Holds information about an in-progress transition.
   */
  @Immutable
  private data class ActiveTransition<T : Any>(
    val fromFrame: BackstackFrame<T>,
    val toFrame: BackstackFrame<T>,
    val popping: Boolean
  )

  // These aren't MutableStates because they're only read when a backstack change happens. They
  // don't need to trigger anything when they're changed.
  lateinit var transition: BackstackTransition
  lateinit var animationSpec: AnimationSpec<Float>
  lateinit var onTransitionStarting: (from: List<T>, to: List<T>, TransitionDirection) -> Unit
  lateinit var onTransitionFinished: () -> Unit

  /**
   * A snapshot of the backstack that will remain unchanged during transitions, even if
   * [updateBackstack] is called with a different stack. Just before
   * [starting a transition][startTransition], this list will be used to determine if we should use
   * a forwards or backwards animation. It's a [MutableState] because it is used to derive the value
   * for [activeFrames], and so it needs to be observable.
   */
  private var activeKeys: List<T> by mutableStateOf(emptyList())

  /** The latest list of keys seen by [updateBackstack]. */
  private var targetKeys = emptyList<T>()

  /**
   * Set to a non-null value only when actively animating between screens as the result of a call
   * to [updateBackstack]. This is a [MutableState] because it's used to derive the value of
   * [activeFrames], and so it needs to be observable.
   */
  private var activeTransition: ActiveTransition<T>? by mutableStateOf(null)

  override val activeFrames: List<BackstackFrame<T>>
    get() = activeTransition?.let { transition ->
      if (transition.popping) {
        listOf(transition.toFrame, transition.fromFrame)
      } else {
        listOf(transition.fromFrame, transition.toFrame)
      }
    } ?: listOf(BackstackFrame(activeKeys.last()))

  override fun updateBackstack(keys: List<T>) {
    // Always remember the latest stack, so if this call is happening during a transition we can
    // detect that when the transition finishes and start the next transition.
    targetKeys = keys

    // We're in the middle of a transition, don't do anything.
    if (activeTransition != null) return

    // Either this is the first call or the visible screen didn't change but we need to update our
    // active list for the next time we check for navigation direction.
    if (activeKeys.isEmpty() || keys.last() == activeKeys.last()) {
      activeKeys = keys
      return
    }

    // We're idle and the visible frame changed so we need to start animating.
    startTransition()
  }

  /**
   * Called when [updateBackstack] gets a new backstack with a new top frame while idle, or after a
   * transition if the [targetKeys]' top is not [activeKeys]' top.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun startTransition() {
    check(activeTransition == null) { "Can only start transitioning while idle." }

    val fromKey = activeKeys.last()
    val toKey = targetKeys.last()
    val popping = toKey in activeKeys
    val progress = Animatable(0f)

    val fromVisibility = derivedStateOf { 1f - progress.value }
    val toVisibility = progress.asState()

    // Wrap modifier functions in each their own recompose scope so that if they read the visibility
    // (or any other state) directly, the modified node will actually be updated.
    val fromModifier = Modifier.composed {
      with(transition) {
        modifierForScreen(fromVisibility, isTop = popping)
      }
    }
    val toModifier = Modifier.composed {
      with(transition) {
        modifierForScreen(toVisibility, isTop = !popping)
      }
    }

    activeTransition = ActiveTransition(
      fromFrame = BackstackFrame(fromKey, fromModifier),
      toFrame = BackstackFrame(toKey, toModifier),
      popping = popping
    )

    val oldActiveKeys = activeKeys
    activeKeys = targetKeys

    scope.launch {
      onTransitionStarting(oldActiveKeys, activeKeys, if (popping) Backward else Forward)
      progress.animateTo(1f, animationSpec)
      activeTransition = null
      onTransitionFinished()

      if (targetKeys.last() != activeKeys.last()) {
        // We got a new top while we were transitioning, so do that transition now.
        startTransition()
      }
    }
  }
}
