package com.redcrow.dragonsfirstflight

import javafx.embed.swing.JFXPanel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.nio.file.Files
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class DesktopIntroDecoderTest {
    @Test
    fun bundledVideoDecodesPausesResumesAndFinishes() =
        runBlocking {
            val source = Files.createTempFile("intro-decoder-test-", ".mp4")
            checkNotNull(javaClass.getResourceAsStream("/video/intro.mp4")).use { input ->
                Files.newOutputStream(source).use(input::copyTo)
            }
            val texture = IntroFrameTexture(1080, 720)
            val completed = CompletableDeferred<Unit>()
            val bridge = withContext(Dispatchers.Swing) { JFXPanel() }
            val decoder = DesktopIntroDecoder(bridge, source, texture) { completed.complete(Unit) }
            try {
                decoder.setPaused(true)
                decoder.start()
                withTimeout(15_000) {
                    while (decoder.positionSeconds == null) delay(25)
                }
                delay(250)
                assertTrue((decoder.positionSeconds ?: 0.0) < 0.15, "A background intro must not start playing")

                decoder.setPaused(false)
                withTimeout(10_000) {
                    while (!texture.hasVisiblePixels || (decoder.positionSeconds ?: 0.0) < 0.5) {
                        delay(25)
                    }
                }
                assertTrue(texture.applyLatest(), "Decoded pixels must reach the KorGE texture")
                decoder.setPaused(true)
                delay(250)
                val pausedAt = checkNotNull(decoder.positionSeconds)
                delay(600)
                assertTrue(abs(checkNotNull(decoder.positionSeconds) - pausedAt) < 0.15)
                assertTrue(!completed.isCompleted, "Pausing must not end the introduction")

                decoder.setPaused(false)
                withTimeout(20_000) { completed.await() }
                assertTrue(checkNotNull(decoder.positionSeconds) > 12.0, "The complete bundled video must play")
            } finally {
                withContext(NonCancellable) { decoder.close() }
                Files.deleteIfExists(source)
            }
        }
}
