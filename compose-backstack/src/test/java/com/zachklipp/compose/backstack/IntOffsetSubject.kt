package com.zachklipp.compose.backstack

import androidx.compose.ui.unit.IntOffset
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Subject.Factory
import com.google.common.truth.Truth.assertAbout

class IntOffsetSubject(
  metadata: FailureMetadata,
  private val actual: IntOffset
) : Subject(metadata, actual) {

  fun isEqualTo(
    x: Int,
    y: Int
  ) = check("IntPxPosition(x, y)").that(actual).isEqualTo(IntOffset(x, y))

  companion object {
    @JvmStatic
    fun assertThat(actual: IntOffset?): IntOffsetSubject = assertAbout(intPxPositions()).that(actual)

    @JvmStatic
    fun intPxPositions(): Factory<IntOffsetSubject, IntOffset> =
      Factory { metadata, actual -> IntOffsetSubject(metadata, actual) }
  }
}
