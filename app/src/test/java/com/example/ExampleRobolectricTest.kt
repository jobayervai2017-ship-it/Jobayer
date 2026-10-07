package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AnimationPreset
import com.example.model.ProjectAspectRatio
import com.example.model.ProjectMetadata
import com.example.model.TextSyncItem
import com.example.model.VideoProject
import com.example.util.AssSubtitleGenerator
import com.example.util.DualJsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SyncCut", appName)
    }

    @Test
    fun testColonTimecodeParsing() {
        // Test parsing colon timecodes
        val t1 = DualJsonParser.parseTimeToSeconds("00:01:23.50")
        assertEquals(83.5, t1, 0.01)

        val t2 = DualJsonParser.parseTimeToSeconds("01:15")
        assertEquals(75.0, t2, 0.01)

        val t3 = DualJsonParser.parseTimeToSeconds(4.25)
        assertEquals(4.25, t3, 0.01)

        val t4 = DualJsonParser.parseTimeToSeconds("3.8")
        assertEquals(3.8, t4, 0.01)
    }

    @Test
    fun testFlexibleDualJsonParsing() {
        val sampleTextJson = """
            [
              {
                "text": "VIRAL HOOK",
                "start": "00:00:01.00",
                "end": "00:00:03.50",
                "animation": "POP_UP"
              },
              {
                "text": "STEP TWO",
                "start": 3.8,
                "end": 6.2
              }
            ]
        """.trimIndent()

        val parsed = DualJsonParser.parseTextSyncJson(sampleTextJson)
        assertEquals(2, parsed.size)
        assertEquals("VIRAL HOOK", parsed[0].text)
        assertEquals(1.0, parsed[0].startSeconds, 0.01)
        assertEquals(3.5, parsed[0].endSeconds, 0.01)
        assertEquals(AnimationPreset.POP_UP, parsed[0].animationPreset)

        assertEquals("STEP TWO", parsed[1].text)
        assertEquals(3.8, parsed[1].startSeconds, 0.01)
        assertEquals(6.2, parsed[1].endSeconds, 0.01)
    }

    @Test
    fun testAssSubtitleGeneration() {
        val items = listOf(
            TextSyncItem(
                id = "sub_1",
                text = "TEST SUBTITLE",
                startSeconds = 1.0,
                endSeconds = 3.5,
                textColorHex = "#FFE600",
                outlineColorHex = "#000000"
            )
        )
        val script = AssSubtitleGenerator.generateAssScript(items, ProjectAspectRatio.SHORTS_9_16)
        assertTrue(script.contains("[Script Info]"))
        assertTrue(script.contains("PlayResX: 1080"))
        assertTrue(script.contains("PlayResY: 1920"))
        assertTrue(script.contains("Dialogue: 0,0:00:01.00,0:00:03.50"))
        assertTrue(script.contains("TEST SUBTITLE"))
    }

    @Test
    fun testVideoProjectDurationSynchronization() {
        val project = VideoProject(
            metadata = ProjectMetadata(id = "p1", title = "Test"),
            images = emptyList(),
            subtitles = listOf(
                TextSyncItem(id = "s1", text = "Caption", startSeconds = 0.0, endSeconds = 8.5)
            )
        )
        // Even with no images, total duration extends to fit captions
        assertEquals(8.5, project.totalDurationSeconds, 0.01)
    }
}
