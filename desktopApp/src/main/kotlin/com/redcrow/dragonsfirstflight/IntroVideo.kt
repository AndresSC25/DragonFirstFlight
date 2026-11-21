package com.redcrow.dragonsfirstflight

import com.redcrow.dragonsfirstflight.game.intro.IntroPlaybackWatchdog
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.layout.StackPane
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.media.MediaView
import javafx.scene.paint.Color
import javafx.util.Duration
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.view.Views
import korlibs.korge.view.addUpdater
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.render.awt.AwtGameWindow
import korlibs.render.awt.NewAwtGameWindow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.awt.Frame
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.min
import kotlin.time.DurationUnit

private const val VIDEO_WIDTH = 1080
private const val VIDEO_HEIGHT = 720

internal suspend fun playIntroVideo(views: Views) {
    val source =
        withContext(Dispatchers.IO) {
            checkNotNull(DesktopIntroDecoder::class.java.getResourceAsStream("/video/intro.mp4")).use { input ->
                val file = Files.createTempFile("dragons-first-flight-intro-", ".mp4")
                try {
                    Files.newOutputStream(file).use(input::copyTo)
                    file
                } catch (error: Exception) {
                    Files.deleteIfExists(file)
                    throw error
                }
            }
        }
    val texture = IntroFrameTexture(VIDEO_WIDTH, VIDEO_HEIGHT)
    val overlay = views.stage.container()
    val background = overlay.solidRect(1.0, 1.0, Colors.BLACK)
    val video = overlay.image(texture.bitmap) { smoothing = false }
    val finished = CompletableDeferred<Unit>()
    val skip = overlay.container()
    skip
        .solidRect(156.0, 54.0, Colors["#101827"]) { alpha = 0.92 }
        .onClick { finished.complete(Unit) }
    skip
        .text("Skip  [Esc]", textSize = 22.0, color = Colors["#F8D66D"]) {
            x = 15.0
            y = 13.0
        }.onClick { finished.complete(Unit) }
    skip.onClick { finished.complete(Unit) }
    val frame =
        when (val window = views.gameWindow) {
            is AwtGameWindow -> window.frame
            is NewAwtGameWindow -> window.frame
            else -> null
        }
    val watchdog = IntroPlaybackWatchdog()
    var decoder: DesktopIntroDecoder? = null
    var displayedFrames = 0
    val updater =
        overlay.addUpdater {
            val left = views.actualVirtualLeft.toDouble()
            val top = views.actualVirtualTop.toDouble()
            val width = views.actualVirtualWidth.toDouble()
            val height = views.actualVirtualHeight.toDouble()
            background.x = left
            background.y = top
            background.width = width
            background.height = height
            val scale = min(width / VIDEO_WIDTH, height / VIDEO_HEIGHT)
            video.scaleX = scale
            video.scaleY = scale
            video.x = left + (width - VIDEO_WIDTH * scale) / 2.0
            video.y = top + (height - VIDEO_HEIGHT * scale) / 2.0
            skip.x = left + width - 180.0
            skip.y = top + 24.0
            if (texture.applyLatest()) displayedFrames++
            val currentDecoder = decoder
            if (watchdog.hasTimedOut(
                    elapsedSeconds = it.toDouble(DurationUnit.SECONDS).coerceAtMost(0.25),
                    isPaused = currentDecoder?.isPaused ?: false,
                    positionSeconds = currentDecoder?.positionSeconds,
                )
            ) {
                System.err.println("Intro stopped making progress; continuing to the menu.")
                finished.complete(Unit)
            }
        }

    val windowListener =
        object : WindowAdapter() {
            private fun refreshPlayback() {
                val active = frame?.let { it.isFocused && (it.extendedState and Frame.ICONIFIED) == 0 } ?: true
                decoder?.setPaused(!active)
            }

            override fun windowGainedFocus(event: WindowEvent) = refreshPlayback()

            override fun windowLostFocus(event: WindowEvent) = refreshPlayback()

            override fun windowIconified(event: WindowEvent) = refreshPlayback()

            override fun windowDeiconified(event: WindowEvent) = refreshPlayback()

            override fun windowClosing(event: WindowEvent) {
                finished.completeExceptionally(CancellationException("Game window closed"))
            }
        }
    val keyListener =
        object : KeyAdapter() {
            override fun keyPressed(event: KeyEvent) {
                if (event.keyCode in listOf(KeyEvent.VK_ESCAPE, KeyEvent.VK_SPACE, KeyEvent.VK_ENTER)) {
                    event.consume()
                    finished.complete(Unit)
                }
            }
        }
    try {
        val bridge =
            withContext(Dispatchers.Swing) {
                frame?.addWindowListener(windowListener)
                frame?.addWindowFocusListener(windowListener)
                frame?.addKeyListener(keyListener)
                try {
                    JFXPanel()
                } catch (error: LinkageError) {
                    throw IllegalStateException("The native video decoder is unavailable", error)
                }
            }
        val intro = DesktopIntroDecoder(bridge, source, texture) { finished.complete(Unit) }
        decoder = intro
        withContext(Dispatchers.Swing) {
            intro.setPaused(frame?.let { !it.isFocused || (it.extendedState and Frame.ICONIFIED) != 0 } ?: false)
        }
        intro.start()
        finished.await()
        intro.setPaused(true)
        skip.visible = false
        // Drain the release event before creating the menu underneath the intro.
        repeat(10) {
            overlay.alpha = 1.0 - (it + 1) / 10.0
            delay(16)
        }
        println("Game intro: displayed $displayedFrames frames in KorGE.")
    } finally {
        updater.close()
        overlay.removeFromParent()
        withContext(NonCancellable + Dispatchers.Swing) {
            frame?.removeWindowListener(windowListener)
            frame?.removeWindowFocusListener(windowListener)
            frame?.removeKeyListener(keyListener)
        }
        withContext(NonCancellable) { decoder?.close() }
        withContext(NonCancellable + Dispatchers.IO) { Files.deleteIfExists(source) }
    }
}

internal class DesktopIntroDecoder(
    private val bridge: JFXPanel,
    private val source: Path,
    private val texture: IntroFrameTexture,
    private val onFinished: () -> Unit,
) {
    private var player: MediaPlayer? = null
    private var sampler: Timeline? = null

    @Volatile
    var isPaused = false
        private set

    @Volatile
    var positionSeconds: Double? = null
        private set

    @Volatile
    private var closed = false

    fun setPaused(paused: Boolean) {
        isPaused = paused
        Platform.runLater { updatePlayback() }
    }

    private fun updatePlayback() {
        if (closed) return
        if (isPaused) {
            sampler?.pause()
            player?.pause()
        } else {
            sampler?.play()
            player?.play()
        }
    }

    fun start() {
        Platform.runLater {
            if (closed) return@runLater
            try {
                val media = Media(source.toUri().toString())
                val mediaPlayer = MediaPlayer(media)
                player = mediaPlayer
                mediaPlayer.currentTimeProperty().addListener { _, _, time ->
                    positionSeconds = time.toSeconds()
                }
                val mediaView = MediaView(mediaPlayer)
                mediaView.isPreserveRatio = true
                mediaView.isSmooth = false
                mediaView.fitWidth = VIDEO_WIDTH.toDouble()
                mediaView.fitHeight = VIDEO_HEIGHT.toDouble()
                val root = StackPane(mediaView)
                bridge.scene = Scene(root, VIDEO_WIDTH.toDouble(), VIDEO_HEIGHT.toDouble(), Color.BLACK)
                root.resize(VIDEO_WIDTH.toDouble(), VIDEO_HEIGHT.toDouble())
                root.applyCss()
                root.layout()

                // The KorGE window bypasses Swing painting. Upload decoded frames
                // to its OpenGL texture instead of overlaying a Swing component.
                val snapshot = WritableImage(VIDEO_WIDTH, VIDEO_HEIGHT)
                val timeline =
                    Timeline(
                        KeyFrame(Duration.millis(1000.0 / 24.0), {
                            try {
                                if (closed || isPaused) return@KeyFrame
                                positionSeconds = mediaPlayer.currentTime.toSeconds()
                                root.snapshot(null, snapshot)
                                val pixels = IntArray(VIDEO_WIDTH * VIDEO_HEIGHT)
                                snapshot.pixelReader.getPixels(
                                    0,
                                    0,
                                    VIDEO_WIDTH,
                                    VIDEO_HEIGHT,
                                    PixelFormat.getIntArgbPreInstance(),
                                    pixels,
                                    0,
                                    VIDEO_WIDTH,
                                )
                                texture.publishArgb(pixels)
                            } catch (error: Exception) {
                                sampler?.stop()
                                fail(error)
                            }
                        }),
                    )
                timeline.cycleCount = Timeline.INDEFINITE
                sampler = timeline
                media.onError = Runnable { fail(media.error) }
                mediaPlayer.onError = Runnable { fail(mediaPlayer.error) }
                mediaPlayer.onEndOfMedia =
                    Runnable {
                        timeline.stop()
                        onFinished()
                    }
                mediaPlayer.onReady =
                    Runnable {
                        println("Game intro: decoding ${media.width}x${media.height} into KorGE textures.")
                        positionSeconds = 0.0
                        updatePlayback()
                    }
            } catch (error: Exception) {
                fail(error)
            } catch (error: LinkageError) {
                fail(error)
            }
        }
    }

    private fun fail(error: Throwable?) {
        System.err.println("Intro playback failed: $error")
        onFinished()
    }

    suspend fun close() {
        closed = true
        val disposed = CompletableDeferred<Unit>()
        Platform.runLater {
            try {
                sampler?.stop()
                player?.dispose()
                bridge.scene = null
            } finally {
                disposed.complete(Unit)
                Platform.exit()
            }
        }
        try {
            withTimeout(2000) { disposed.await() }
        } catch (error: TimeoutCancellationException) {
            System.err.println("Intro decoder disposal exceeded its deadline.")
        }
    }
}
