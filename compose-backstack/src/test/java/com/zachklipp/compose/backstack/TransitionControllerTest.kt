package com.zachklipp.compose.backstack

import androidx.compose.animation.core.TweenSpec
import com.google.common.truth.Truth.assertThat
import com.zachklipp.compose.backstack.BackstackTransition.Crossfade
import com.zachklipp.compose.backstack.FrameController.FrameAndModifier
import kotlinx.coroutines.CoroutineScope
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

class TransitionControllerTest {

  private val scope = CoroutineScope(EmptyCoroutineContext)
  private val transition = Crossfade
  private val animationSpec = TweenSpec<Float>(durationMillis = 1000)
  private val controller = TransitionController<String>(scope).also {
    it.transition = transition
    it.animationSpec = animationSpec
    it.onTransitionStarting = { _, _, _ -> }
    it.onTransitionFinished = { }

  }

  @Test fun `initial update sets activeFrames`() {
    val frame = BackstackFrame("hello", "hello") {}
    controller.updateBackstack(listOf(frame))
    assertThat(controller.activeFrames).containsExactly(FrameAndModifier(frame))
  }
}
