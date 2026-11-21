package com.redcrow.dragonsfirstflight

import javafx.application.Platform
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse

class DesktopIntroFailureTest {
    @Test
    fun corruptVideoEndsPlaybackInsteadOfBlockingStartup() =
        runBlocking {
            val source = Files.createTempFile("invalid-game-intro-", ".mp4")
            Files.write(source, byteArrayOf(1, 2, 3, 4))
            val bridge = withContext(Dispatchers.Swing) { createDesktopIntroBridge() }
            val finished = CompletableDeferred<Unit>()
            val texture = IntroFrameTexture(1080, 720)
            val decoder = DesktopIntroDecoder(bridge, source, texture) { finished.complete(Unit) }
            try {
                decoder.start()
                withTimeout(10_000) { finished.await() }
                assertFalse(texture.hasVisiblePixels)
            } finally {
                withContext(NonCancellable) { decoder.close() }
                Platform.exit()
                deleteIntroSource(source)
            }
        }
}
