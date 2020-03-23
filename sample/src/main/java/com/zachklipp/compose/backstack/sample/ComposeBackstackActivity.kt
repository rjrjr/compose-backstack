package com.zachklipp.compose.backstack.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.setViewContent
import androidx.ui.core.setContent
import com.zachklipp.compose.backstack.sample.App

class ComposeBackstackActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { App() }
  }
}
