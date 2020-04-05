package com.zachklipp.compose.backstack

import androidx.ui.unit.IntPxPosition
import androidx.ui.unit.ipx
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Subject.Factory
import com.google.common.truth.Truth.assertAbout

class IntPxPositionSubject(
    metadata: FailureMetadata,
    private val actual: IntPxPosition
) : Subject(metadata, actual) {

    fun isEqualTo(x: Int, y: Int) =
        check("IntPxPosition(x, y)").that(actual).isEqualTo(IntPxPosition(x.ipx, y.ipx))

    companion object {
        @JvmStatic
        fun assertThat(actual: IntPxPosition?) = assertAbout(intPxPositions()).that(actual)

        @JvmStatic
        fun intPxPositions(): Factory<IntPxPositionSubject, IntPxPosition> =
            Factory { metadata, actual -> IntPxPositionSubject(metadata, actual) }
    }
}
