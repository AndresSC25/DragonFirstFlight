package com.redcrow.dragonsfirstflight

import javafx.application.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

class DesktopIntroRerunTest {
    @Test
    fun restartingPlaybackInTheSameProcessDecodesVideoAgain() =
        runBlocking {
            val source = Files.createTempFile("intro-rerun-test-", ".mp4")
            checkNotNull(javaClass.getResourceAsStream("/video/intro.mp4")).use { input ->
                Files.newOutputStream(source).use(input::copyTo)
            }
            try {
                repeat(3) { attempt ->
                    val texture = IntroFrameTexture(1080, 720)
                    val bridge = withContext(Dispatchers.Swing) { createDesktopIntroBridge() }
                    val decoder = DesktopIntroDecoder(bridge, source, texture) {}
                    try {
                        decoder.start()
                        withTimeout(10_000) {
                            while (!texture.hasVisiblePixels || (decoder.positionSeconds ?: 0.0) < 0.5) delay(25)
                        }
                        assertTrue(texture.applyLatest(), "Launch ${attempt + 1} must render video frames")
                    } finally {
                        withContext(NonCancellable) { decoder.close() }
                    }
                }
            } finally {
                Platform.exit()
                deleteIntroSource(source)
            }
        }
}
