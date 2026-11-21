package com.redcrow.dragonsfirstflight

import com.sun.media.jfxmedia.MediaManager
import com.sun.media.jfxmedia.MediaPlayer
import com.sun.media.jfxmedia.control.VideoFormat
import com.sun.media.jfxmedia.events.MediaErrorListener
import com.sun.media.jfxmedia.events.NewFrameEvent
import com.sun.media.jfxmedia.events.PlayerStateEvent
import com.sun.media.jfxmedia.events.PlayerStateListener
import com.sun.media.jfxmedia.events.VideoRendererListener
import com.sun.media.jfxmedia.locator.Locator
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/** Receives decoded pixels directly; no MediaView, snapshots or animation pulses are involved. */
internal class DesktopIntroDecoder(
    private val bridge: JFXPanel,
    private val source: Path,
    private val texture: IntroFrameTexture,
    private val onFinished: () -> Unit,
) {
    private var player: MediaPlayer? = null
    private val completionSent = AtomicBoolean(false)
    private val decodedFrames = AtomicInteger()
    private var prepared = false

    @Volatile
    var isPaused = false
        private set

    @Volatile
    var positionSeconds: Double? = null
        private set

    @Volatile
    var capturedPositionSeconds: Double? = null
        private set

    @Volatile
    private var closed = false

    private val errorListener =
        MediaErrorListener { _, code, message ->
            Platform.runLater { fail(IllegalStateException("Native video error $code: $message")) }
        }

    private val frameListener =
        object : VideoRendererListener {
            override fun videoFrameUpdated(event: NewFrameEvent) {
                if (closed || isPaused) return
                val frame = event.frameData
                frame.holdFrame()
                try {
                    val packed = checkNotNull(frame.convertToFormat(VideoFormat.BGRA_PRE))
                    try {
                        texture.publishBgra(
                            packed.getBufferForPlane(0),
                            packed.width,
                            packed.height,
                            packed.getStrideForPlane(0),
                        )
                        positionSeconds = frame.timestamp
                        if (texture.hasVisiblePixels) capturedPositionSeconds = frame.timestamp
                        if (decodedFrames.incrementAndGet() == 1) {
                            println("Game intro: receiving native video frames ${frame.width}x${frame.height}.")
                        }
                    } finally {
                        packed.releaseFrame()
                    }
                } catch (error: Exception) {
                    Platform.runLater { fail(error) }
                } catch (error: LinkageError) {
                    Platform.runLater { fail(error) }
                } finally {
                    frame.releaseFrame()
                }
            }

            override fun releaseVideoFrames() = Unit
        }

    private val stateListener =
        object : PlayerStateListener {
            override fun onReady(event: PlayerStateEvent) {
                Platform.runLater {
                    if (closed) return@runLater
                    prepared = true
                    positionSeconds = 0.0
                    println("Game intro: native decoder ready.")
                    updatePlayback()
                }
            }

            override fun onPlaying(event: PlayerStateEvent) = Unit

            override fun onPause(event: PlayerStateEvent) = Unit

            override fun onStop(event: PlayerStateEvent) = Unit

            override fun onStall(event: PlayerStateEvent) = Unit

            override fun onFinish(event: PlayerStateEvent) {
                Platform.runLater {
                    if (closed) return@runLater
                    positionSeconds = player?.duration?.takeIf { it.isFinite() && it >= 0.0 } ?: capturedPositionSeconds
                    val count = decodedFrames.get()
                    println("Game intro: native media ended at ${positionSeconds}s, decoded=$count.")
                    if (count < 2 || capturedPositionSeconds == null) {
                        fail(IllegalStateException("The video ended without a moving picture"))
                    } else if (completionSent.compareAndSet(false, true)) {
                        onFinished()
                    }
                }
            }

            override fun onHalt(event: PlayerStateEvent) {
                Platform.runLater { fail(IllegalStateException("Native video halted: ${event.message}")) }
            }
        }

    fun setPaused(paused: Boolean) {
        isPaused = paused
        Platform.runLater { updatePlayback() }
    }

    private fun updatePlayback() {
        if (closed || !prepared) return
        if (isPaused) player?.pause() else player?.play()
    }

    fun start() {
        Platform.runLater {
            if (closed) return@runLater
            try {
                // JavaFX media is pinned in Gradle; its native frame API bypasses the scene renderer.
                // Reject incomplete MP4 headers before the native backend allocates a file reader.
                Files.newInputStream(source).use { input ->
                    val header = input.readNBytes(16)
                    require(header.size == 16 && String(header, 4, 4, Charsets.US_ASCII) == "ftyp") {
                        "Invalid introductory MP4 header"
                    }
                    val boxSize = Integer.toUnsignedLong(ByteBuffer.wrap(header).int)
                    require(boxSize >= 16 && boxSize <= Files.size(source)) { "Truncated introductory MP4 header" }
                }
                val locator =
                    Locator(source.toUri()).apply {
                        init()
                    }
                val nativePlayer = MediaManager.getPlayer(locator)
                player = nativePlayer
                nativePlayer.addMediaErrorListener(errorListener)
                nativePlayer.videoRenderControl?.addVideoRendererListener(frameListener)
                nativePlayer.addMediaPlayerListener(stateListener)
            } catch (error: Exception) {
                fail(error)
            } catch (error: LinkageError) {
                fail(error)
            }
        }
    }

    private fun fail(error: Throwable) {
        if (closed || !completionSent.compareAndSet(false, true)) return
        System.err.println("Intro playback failed: $error")
        onFinished()
    }

    suspend fun close() {
        closed = true
        val disposed = CompletableDeferred<Unit>()
        Platform.runLater {
            try {
                player?.let { nativePlayer ->
                    try {
                        nativePlayer.videoRenderControl?.removeVideoRendererListener(frameListener)
                        nativePlayer.removeMediaPlayerListener(stateListener)
                        nativePlayer.removeMediaErrorListener(errorListener)
                    } finally {
                        nativePlayer.dispose()
                    }
                }
                player = null
                bridge.scene = null
                disposed.complete(Unit)
            } catch (error: Exception) {
                disposed.completeExceptionally(error)
            }
        }
        try {
            withTimeout(2000) { disposed.await() }
        } catch (error: TimeoutCancellationException) {
            System.err.println("Intro decoder disposal exceeded its deadline.")
        }
    }
}
