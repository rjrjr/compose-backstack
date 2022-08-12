package com.zachklipp.compose.backstack

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import com.zachklipp.compose.backstack.FrameController.BackstackFrame
import com.zachklipp.compose.backstack.TransitionDirection.Backward
import com.zachklipp.compose.backstack.TransitionDirection.Forward
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

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

    LaunchedEffect(it) {
      it.runTransitionAnimations()
    }
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

  var transition: BackstackTransition? by mutableStateOf(null)
  var animationSpec: AnimationSpec<Float>? by mutableStateOf(null)
  var onTransitionStarting: ((from: List<T>, to: List<T>, TransitionDirection) -> Unit)?
      by mutableStateOf(null)
  var onTransitionFinished: (() -> Unit)? by mutableStateOf(null)

  /**
   * A snapshot of the backstack that will remain unchanged during transitions, even if
   * [updateBackstack] is called with a different stack. Just before
   * [starting a transition][animateTransition], this list will be used to determine if we should use
   * a forwards or backwards animation. It's a [MutableState] because it is used to derive the value
   * for [activeFrames], and so it needs to be observable.
   */
  private var displayedKeys: List<T> by mutableStateOf(emptyList())

  /** The latest list of keys seen by [updateBackstack]. */
  private var targetKeys by mutableStateOf(emptyList<T>())

  /**
   * Set to a non-null value only when actively animating between screens as the result of a call
   * to [updateBackstack]. This is a [MutableState] because it's used to derive the value of
   * [activeFrames], and so it needs to be observable.
   */
  private var activeTransition: ActiveTransition<T>? by mutableStateOf(null)

  override val activeFrames: List<BackstackFrame<T>> by derivedStateOf {
    activeTransition?.let { transition ->
      if (transition.popping) {
        listOf(transition.toFrame, transition.fromFrame)
      } else {
        listOf(transition.fromFrame, transition.toFrame)
      }
    } ?: listOf(BackstackFrame(displayedKeys.last()))
  }

  /**
   * Should be called from a coroutine that has access to a frame clock (i.e. from a
   * [rememberCoroutineScope] or in a [LaunchedEffect]), and must be allowed to run until this
   * [TransitionController] leaves the composition. It will never return unless cancelled.
   */
  suspend fun runTransitionAnimations() {
    // This flow handles backpressure by conflating: if targetKeys is changed multiple times while
    // an animation is running, we'll only get a single emission when it finishes.
    snapshotFlow { targetKeys }.collect { targetKeys ->
      if (displayedKeys.last() == targetKeys.last()) {
        // The visible screen didn't change, so we don't need to animate, but we need to update our
        // active list for the next time we check for navigation direction.
        displayedKeys = targetKeys
        return@collect
      }

      // The top of the stack was changed, so animate to the new top.
      animateTransition(fromKeys = displayedKeys, toKeys = targetKeys)
    }
  }

  override fun updateBackstack(keys: List<T>) {
    // Always remember the latest stack, so if this call is happening during a transition we can
    // detect that when the transition finishes and start the next transition.
    targetKeys = keys

    // This is the first update, so we don't animate, and need to show the backstack as-is
    // immediately.
    if (displayedKeys.isEmpty()) {
      displayedKeys = keys
    }
  }

  /**
   * Called when [updateBackstack] gets a new backstack with a new top frame while idle, or after a
   * transition if the [targetKeys]' top is not [displayedKeys]' top.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private suspend fun animateTransition(fromKeys: List<T>, toKeys: List<T>) {
    check(activeTransition == null) { "Can only start transitioning while idle." }

    val fromKey = fromKeys.last()
    val toKey = toKeys.last()
    val popping = toKey in fromKeys
    val progress = Animatable(0f)

    val fromVisibility = derivedStateOf { 1f - progress.value }
    val toVisibility = progress.asState()

    // Wrap modifier functions in each their own recompose scope so that if they read the visibility
    // (or any other state) directly, the modified node will actually be updated.
    val fromModifier = Modifier.composed {
      with(transition!!) {
        modifierForScreen(fromVisibility, isTop = popping)
      }
    }
    val toModifier = Modifier.composed {
      with(transition!!) {
        modifierForScreen(toVisibility, isTop = !popping)
      }
    }

    activeTransition = ActiveTransition(
      fromFrame = BackstackFrame(fromKey, fromModifier),
      toFrame = BackstackFrame(toKey, toModifier),
      popping = popping
    )

    val oldActiveKeys = displayedKeys
    displayedKeys = targetKeys

    onTransitionStarting!!(oldActiveKeys, displayedKeys, if (popping) Backward else Forward)
    progress.animateTo(1f, animationSpec!!)
    activeTransition = null
    onTransitionFinished!!()
  }
}
