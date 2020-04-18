@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry", "FunctionName")

package com.zachklipp.compose.backstack

import androidx.animation.AnimationBuilder
import androidx.animation.TweenBuilder
import androidx.compose.*
import androidx.ui.core.*
import androidx.ui.foundation.Box
import androidx.ui.foundation.shape.RectangleShape
import androidx.ui.layout.Stack
import androidx.ui.savedinstancestate.UiSavedStateRegistryAmbient
import androidx.ui.semantics.Semantics

/** Used to hide screens when not transitioning. */
internal val HIDDEN_MODIFIER = Modifier.drawOpacity(0f)

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
@Immutable
private data class ScreenWrapper<T : Any>(
    val key: T,
    val transition: @Composable() (content: @Composable() () -> Unit) -> Unit
)

internal data class ScreenProperties(
    val modifier: Modifier,
    val isVisible: Boolean
)

@Composable
// TODO move to transition transformer file
internal val DefaultBackstackAnimation: AnimationBuilder<Float>
    get() {
        val context = ContextAmbient.current
        return TweenBuilder<Float>().apply {
            duration = context.resources.getInteger(android.R.integer.config_shortAnimTime)
        }
    }

/**
 * Renders the top of a stack of screens (as [T]s) and animates between screens when the top
 * value changes. Any state used by a screen will be preserved as long as it remains in the stack
 * (i.e. result of [remember] or [state] calls).
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
 * [savedInstanceState][androidx.ui.savedinstancestate.savedInstanceState]) will automatically have
 * that state saved when they are hidden, and restored the next time they're shown.
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
@Composable
fun <T : Any> Backstack(
    backstack: List<T>,
    modifier: Modifier = Modifier,
    transition: BackstackTransition = BackstackTransition.Slide,
    animationBuilder: AnimationBuilder<Float>? = null,
    onTransitionStarting: ((from: List<T>, to: List<T>, TransitionDirection) -> Unit)? = null,
    onTransitionFinished: (() -> Unit)? = null,
    inspectionParams: InspectionParams? = null,
    drawScreen: @Composable() (T) -> Unit
) {
    require(backstack.isNotEmpty()) { "Backstack must contain at least 1 screen." }

    val screensToDraw = mutableListOf<Pair<T, Modifier?>>()
    val backstackSet = mutableSetOf<T>()
    backstackSet.addAll(backstack)
    require(backstackSet.size == backstack.size) {
        "Backstack must not contain duplicates: $backstack (${backstackSet.size} ≠ ${backstack.size})"
    }

    val transitionProcessor = TransitionProperties(
        transition,
        animationBuilder,
        onTransitionStarting,
        onTransitionFinished
    )

    val clock = AnimationClockAmbient.current
    val inspectionProcessor = remember { InspectionProcessor(clock) }
    inspectionProcessor.delegate = transitionProcessor
    inspectionProcessor.params = inspectionParams

    val newScreens = inspectionProcessor.processStack(backstack)

    // Append in reverse order, the final list will be reversed again when drawing.
    newScreens.asReversed().forEach { modifiedScreen ->
        @Suppress("UNCHECKED_CAST")
        screensToDraw.add(modifiedScreen as ModifiedScreen<T>)
        backstackSet.remove(modifiedScreen.first)
    }

    // Add the remaining, hidden screens to the list. Order doesn't matter, they just need to be
    // kept around so their saved state stays alive.
    backstackSet.workaroundPivotalMoveAndDeleteBug().mapTo(screensToDraw) { Pair(it, null) }

    // Actually draw the screens.
    Stack(modifier = modifier.clip(RectangleShape)) {
        screensToDraw.asReversed().forEach { (item, modifier) ->
            // Key is a convenience helper that treats its arguments as @Pivotal. This is how state
            // preservation is implemented. Even if screens are moved around within the list, as long
            // as they're invoked through the exact same sequence of source locations from within this
            // key lambda, they will keep their state.
            key(item) {
                modifyScreen(screenModifier = modifier) {
                    drawScreen(item)
                }
            }
        }
    }
}

/**
 * There's a bug in Compose that seems to occur when re-ordering a keyed (pivotal) list and also
 * removing things from the composition at the same time. This happens when leaving inspection mode
 * with a backstack > 1: on the next compose pass, the hidden screens get added to the
 * list in reverse order, but their children are also removed in the same pass.
 *
 * This function reverses the order of the backstack set before adding it to screensToDraw so that
 * the ordering will be consistent when the inspector stops handling the stack.
 *
 * Bug is tracked at https://issuetracker.google.com/issues/154411181.
 */
private fun <T> Iterable<T>.workaroundPivotalMoveAndDeleteBug(): List<T> = toList().asReversed()

/**
 * This wrapper composable will remain in the composition as long as its key is
 * in the backstack. So we can use remember here to hold state that should persist
 * even when the screen is hidden.
 */
@Composable
private fun modifyScreen(screenModifier: Modifier?, children: @Composable() () -> Unit) {
    // This must be called even if the screen is not visible, so the screen's state gets
    // cached before it's removed from the composition.
    val savedStateRegistry = ChildSavedStateRegistry(childWillBeComposed = screenModifier != null)

    // Null modifier means the processor doesn't want to show the screen.
    if (screenModifier == null) return

    // Without an explicit semantics container, all screens will be merged into a single
    // semantics group.
    Semantics(container = true) {
        Providers(UiSavedStateRegistryAmbient provides savedStateRegistry) {
            Box(modifier = screenModifier, children = children)
        }
    }
}
