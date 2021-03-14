@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry", "FunctionName")

package com.zachklipp.compose.backstack

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationEndReason.Finished
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import com.zachklipp.compose.backstack.TransitionDirection.Backward
import com.zachklipp.compose.backstack.TransitionDirection.Forward
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

/** Used to hide screens when not transitioning. */
internal val HIDDEN_MODIFIER = Modifier.alpha(0f)

/**
 * Identifies which direction a transition is being performed in.
 */
enum class TransitionDirection {
  Forward,
  Backward
}

/**
 * Wraps each screen composable with the transition modifier derived from the current animation
 * progress.
 */
private data class ScreenWrapper<T : Any>(
  val key: T,
  val transition: @Composable (progress: Float, @Composable () -> Unit) -> Unit
)

internal data class ScreenProperties(
  val modifier: Modifier,
  val isVisible: Boolean
)

private val DefaultBackstackAnimation: AnimationSpec<Float>
  @Composable get() {
    val context = LocalContext.current
    return TweenSpec(
      durationMillis = context.resources.getInteger(android.R.integer.config_shortAnimTime)
    )
  }

/**
 * Renders the top of a stack of screens (as [T]s) and animates between screens when the top
 * value changes. Any state used by a screen will be preserved as long as it remains in the stack
 * (i.e. result of [remember] calls).
 *
 * The [backstack] must follow some rules:
 *  - Must always contain at least one item.
 *  - Elements in the stack must implement `equals` and not change over the lifetime of the screen.
 *    If the key changes, it will be considered a new screen and any state held by the screen will
 *    be lost.
 *  - If items in the stack are reordered between compositions, the stack should not contain
 *    duplicates. If it does, due to how `@Pivotal` works, the states of those screens will be
 *    lost if they are moved around. If the list contains duplicates, an [IllegalArgumentException]
 *    will be thrown.
 *
 * This composable does not actually provide any navigation functionality – it just renders
 * transitions between stacks of screens. It can be plugged into your navigation library of choice,
 * or just used on its own with a simple list of screens.
 *
 * ## Instance state caching
 *
 * Screens that contain persistable state using the (i.e. via
 * [savedInstanceState][androidx.compose.runtime.savedinstancestate.savedInstanceState]) will
 * automatically have that state saved when they are hidden, and restored the next time they're
 * shown.
 *
 * ## Example
 *
 * ```
 * sealed class Screen {
 *   object ContactList: Screen()
 *   data class ContactDetails(val id: String): Screen()
 *   data class EditContact(val id: String): Screen()
 * }
 *
 * data class Navigator(
 *   val push: (Screen) -> Unit,
 *   val pop: () -> Unit
 * )
 *
 * @Composable fun App() {
 *   var backstack by state { listOf(Screen.ContactList) }
 *   val navigator = remember {
 *     Navigator(
 *       push = { backstack += it },
 *       pop = { backstack = backstack.dropLast(1) }
 *     )
 *   }
 *
 *   Backstack(backstack) { screen ->
 *     when(screen) {
 *       Screen.ContactList -> ShowContactList(navigator)
 *       is Screen.ContactDetails -> ShowContact(screen.id, navigator)
 *       is Screen.EditContact -> ShowEditContact(screen.id, navigator)
 *     }
 *   }
 * }
 * ```
 *
 * @param backstack The stack of screen values.
 * @param modifier [Modifier] that will be applied to the container of screens. Neither affects nor
 * is affected by transition animations.
 * @param transition The [BackstackTransition] that defines how to animate between screens when
 * [backstack] changes. [BackstackTransition] contains a few simple pre-fab transitions.
 * @param animationBuilder Defines the curve and speed of transition animations.
 * @param onTransitionStarting Callback that will be invoked before starting each transition.
 * @param onTransitionFinished Callback that will be invoked after each transition finishes.
 * @param inspectionParams Optional [InspectionParams] that, when not null, enables inspection mode,
 * which will draw all the screens in the backstack as a translucent 3D stack. You can wrap your
 * backstack with [InspectionGestureDetector] to automatically generate [InspectionParams]
 * controlled by touch gestures.
 * @param drawScreen Called with each element of [backstack] to render it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun <T : Any> Backstack(
  backstack: List<T>,
  modifier: Modifier = Modifier,
  transition: BackstackTransition = BackstackTransition.Slide,
  animationBuilder: AnimationSpec<Float>? = null,
  onTransitionStarting: ((from: List<T>, to: List<T>, TransitionDirection) -> Unit)? = null,
  onTransitionFinished: (() -> Unit)? = null,
  inspectionParams: InspectionParams? = null,
  drawScreen: @Composable (T) -> Unit
) {
  require(backstack.isNotEmpty()) { "Backstack must contain at least 1 screen." }
  require(backstack.distinct().size == backstack.size) {
    "Backstack must not contain duplicates: $backstack"
  }
  val scope = rememberCoroutineScope()

  // When transitioning, contains a stable cache of the screens actually being displayed. Will not
  // change even if backstack changes during the transition.
  var activeKeys by remember { mutableStateOf(backstack) }
  // The "top" screen being transitioned to. Used at the end of the transition to detect if the
  // backstack changed and needs another transition immediately.
  var targetTop by remember { mutableStateOf(backstack.last()) }
  // Wrap all items to draw in a list, so that they will all share a constant "compositional
  // position", which allows us to use @Pivotal machinery to preserve state.
  var activeStackDrawers by remember { mutableStateOf(emptyList<ScreenWrapper<T>>()) }
  // Defines the progress of the current transition animation in terms of visibility of the top
  // screen. 1 means top screen is visible, 0 means top screen is entirely hidden. Must be 1 when
  // no transition in progress.
  val transitionProgress = remember { Animatable(1f) }
  // Null means not transitioning.
  var direction by remember { mutableStateOf<TransitionDirection?>(null) }
  // Callback passed to animations to cleanup after the transition is done.
  val onTransitionEnd = remember {
    { reason: AnimationEndReason, _: Float ->
      if (reason == Finished) {
        direction = null
        scope.launch {
          transitionProgress.snapTo(1f)
          onTransitionFinished?.invoke()
        }
      }
    }
  }
  val animation = animationBuilder ?: DefaultBackstackAnimation
  val inspector = remember { BackstackInspector(scope) }
  inspector.params = inspectionParams

  if (direction == null && activeKeys != backstack) {
    // Not in the middle of a transition and we got a new backstack.
    // This will also run after a transition, to clean up old keys out of the temporary backstack.

    if (backstack.last() == targetTop) {
      // Don't need to transition, but some hidden keys changed to so we need to update the active
      // list to ensure hidden screens that no longer exist are torn down.
      activeKeys = backstack
    } else {
      // Remember the top we're transitioning to so we don't re-transition afterwards if we're
      // showing the same top.
      targetTop = backstack.last()

      // If the new top is in the old backstack, then it has probably already been seen, so the
      // navigation is logically backwards, even if the new backstack actually contains more
      // screens.
      direction = if (targetTop in activeKeys) Backward else Forward

      // Mutate the stack for the transition so the keys that need to be temporarily shown are in
      // the right place.
      val oldTop = activeKeys.last()
      val newKeys = backstack.toMutableList()
      if (direction == Backward) {
        // We need to put the current screen on the top of the new active stack so it will animate
        // out.
        newKeys += oldTop

        // When going back the top screen needs to start off as visible.
        // Need to start the coroutine undispatched so the snap happens before the frame is drawn.
        scope.launch(start = UNDISPATCHED) {
          transitionProgress.snapTo(1f)
          val result = transitionProgress.animateTo(0f, animationSpec = animation)
          onTransitionEnd(result.endReason, result.endState.value)
        }
      } else {
        // If the current screen is not the new second-last screen, we need to move it to that
        // position so it animates out when going forward. This is true whether or not the current
        // screen is actually in the new backstack at all.
        newKeys -= targetTop
        newKeys -= oldTop
        newKeys += oldTop
        newKeys += targetTop

        // When going forward, the top screen needs to start off as invisible.
        scope.launch(start = UNDISPATCHED) {
          transitionProgress.snapTo(0f)
          val result = transitionProgress.animateTo(1f, animationSpec = animation)
          onTransitionEnd(result.endReason, result.endState.value)
        }
      }
      onTransitionStarting?.invoke(activeKeys, backstack, direction!!)
      activeKeys = newKeys
    }
  }

  // Only refresh the wrappers when the keys or opacity actually change.
  // We need to regenerate these if the keys in the backstack change even if the top doesn't change
  // because we need to dispose of old screens that are no longer rendered.
  //
  // Note: This block must not contain any control flow logic that causes the screen composables
  // to be invoked from different source locations. If it does, those screens will lose all their
  // state as soon as a different branch is taken. See @Pivotal for more information.
  activeStackDrawers = remember(activeKeys, transition) {
    activeKeys.mapIndexed { index, key ->
      // This wrapper composable will remain in the composition as long as its key is
      // in the backstack. So we can use remember here to hold state that should persist
      // even when the screen is hidden.
      ScreenWrapper(key) { progress, children ->
        // Inspector and transition are mutually exclusive.
        val screenProperties = if (inspector.isInspectionActive) {
          calculateInspectionModifier(inspector, index, activeKeys.size, progress)
        } else {
          calculateRegularModifier(transition, index, activeKeys.size, progress)
        }

        // This must be called even if the screen is not visible, so the screen's state gets
        // cached before it's removed from the composition.
        val savedStateRegistry = ChildSavedStateRegistry(screenProperties.isVisible)

        if (!screenProperties.isVisible) {
          // Remove the screen from the composition.
          // This must be done after updating the savedState visibility so it has a chance
          // to query providers before they're unregistered.
          return@ScreenWrapper
        }

        Providers(LocalSaveableStateRegistry provides savedStateRegistry) {
          Box(screenProperties.modifier) { children() }
        }
      }
    }
  }

  // Actually draw the screens.
  Box(modifier = modifier.clip(RectangleShape)) {
    activeStackDrawers.forEach { (item, transition) ->
      // Key is a convenience helper that treats its arguments as @Pivotal. This is how state
      // preservation is implemented. Even if screens are moved around within the list, as long
      // as they're invoked through the exact same sequence of source locations from within this
      // key lambda, they will keep their state.
      key(item) {
        transition(transitionProgress.value) { drawScreen(item) }
      }
    }
  }
}

internal fun calculateRegularModifier(
  transition: BackstackTransition,
  index: Int,
  count: Int,
  progress: Float
): ScreenProperties {
  val visibility = when (index) {
    // transitionProgress always corresponds directly to visibility of the top screen.
    count - 1 -> progress
    // The second-to-top screen has the inverse visibility of the top screen.
    count - 2 -> 1f - progress
    // All other screens should not be drawn at all. They're only kept around to maintain
    // their composable state.
    else -> 0f
  }

  val screenModifier = when (visibility) {
    0f -> HIDDEN_MODIFIER
    1f -> Modifier
    else -> transition.modifierForScreen(visibility, index == count - 1)
  }
  return ScreenProperties(
    modifier = screenModifier,
    isVisible = visibility != 0f
  )
}

@Composable
private fun calculateInspectionModifier(
  inspector: BackstackInspector,
  index: Int,
  count: Int,
  progress: Float
): ScreenProperties {
  val visibility = when (index) {
    count - 1 -> progress
    // All previous screens are always visible in inspection mode.
    else -> 1f
  }
  return ScreenProperties(
      modifier = inspector.inspectScreen(index, count, visibility),
      isVisible = true
  )
}
