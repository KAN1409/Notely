package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiAiService
import com.example.ai.IntelligenceEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NexNote", appName)
  }

  @Test
  fun `test intelligence engine local extraction`() {
    val engine = IntelligenceEngine(GeminiAiService())
    val raw = "Site visit with Ahmed regarding Negma Project reception lighting. We discussed Galala cream marble quotation of 2,450 EGP / m2."
    val result = engine.runLocalSemanticAnalysis(raw, "TEXT")

    assertTrue(result.people.contains("Ahmed"))
    assertTrue(result.projects.contains("Negma Project"))
    assertTrue(result.topics.contains("Lighting"))
    assertTrue(result.topics.contains("Marble"))
    assertTrue(result.monetaryValues.isNotEmpty())
  }
}

