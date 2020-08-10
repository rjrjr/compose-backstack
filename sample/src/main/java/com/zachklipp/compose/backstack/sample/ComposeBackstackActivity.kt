package com.zachklipp.compose.backstack.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent
import com.zachklipp.compose.backstack.viewer.BackstackViewerApp

class ComposeBackstackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BackstackViewerApp(
                namedCustomTransitions = listOf("Fancy" to FancyTransition),
                prefabBackstacks = listOf(
                    listOf("one"),
                    listOf("one", "two"),
                    listOf("one", "two", "three"),
                    listOf("two", "one")
                )
            )
        }
    }
}
