package com.redcrow.dragonsfirstflight

import com.redcrow.dragonsfirstflight.game.audio.IntroMusicPlayback
import com.redcrow.dragonsfirstflight.game.intro.IntroPlaybackWatchdog
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.onClick
import korlibs.korge.service.storage.storage
import korlibs.korge.view.Views
import korlibs.korge.view.addUpdater
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.render.awt.AwtGameWindow
import korlibs.render.awt.NewAwtGameWindow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import java.awt.Frame
import java.awt.KeyboardFocusManager
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.time.DurationUnit

private const val VIDEO_WIDTH = 1080
private const val VIDEO_HEIGHT = 720
private const val SKIP_BUTTON_WIDTH = 160.0
private const val SKIP_BUTTON_HEIGHT = 60.0
private const val SKIP_BUTTON_MARGIN = 24.0

internal fun createDesktopIntroBridge(): JFXPanel = JFXPanel().also { Platform.setImplicitExit(false) }

internal suspend fun deleteIntroSource(source: Path) {
    withContext(Dispatchers.IO) {
        // Give the native backend a bounded interval to release its Windows file handle.
        repeat(40) { attempt ->
            try {
                Files.deleteIfExists(source)
                return@withContext
            } catch (error: FileSystemException) {
                if (attempt == 39) throw error
                delay(25)
            }
        }
    }
}

internal suspend fun playIntroVideo(views: Views) {
    val buttonFrame = createIntroSkipTexture(resourcesVfs["ui/buttons/button_normal.png"].readBitmap())
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
    val music =
        IntroMusicPlayback.load(
            views.coroutineContext,
            views.storage.getOrNull("audio.music") != "false",
            effectsEnabled = views.storage.getOrNull("audio.sound") != "false",
        )

    fun finishIntro(reason: String) {
        views.gameWindow.queue { music.close() }
        if (finished.complete(Unit)) println("Game intro: $reason")
    }
    val skip = overlay.container()
    val skipFocusOutline =
        skip.roundRect(
            size = Size(SKIP_BUTTON_WIDTH + 12.0, SKIP_BUTTON_HEIGHT + 12.0),
            radius = RectCorners(16.0),
            fill = Colors["#101827"],
            stroke = Colors.WHITE,
            strokeThickness = 2.0,
        ) {
            x = -6.0
            y = -6.0
            visible = false
        }
    skip
        .image(buttonFrame) {
            scaleX = SKIP_BUTTON_WIDTH / buttonFrame.width
            scaleY = SKIP_BUTTON_HEIGHT / buttonFrame.height
            smoothing = false
        }.onClick { finishIntro("skipped with the Skip button.") }
    skip
        .text("Skip", textSize = 24.0, color = Colors["#101827"]) {
            x = (SKIP_BUTTON_WIDTH - width) / 2.0
            y = (SKIP_BUTTON_HEIGHT - height) / 2.0
        }.onClick { finishIntro("skipped with the Skip button.") }
    skip.onClick { finishIntro("skipped with the Skip button.") }
    val frame =
        when (val window = views.gameWindow) {
            is AwtGameWindow -> window.frame
            is NewAwtGameWindow -> window.frame
            else -> null
        }
    val watchdog = IntroPlaybackWatchdog()
    val foreground = AtomicBoolean(false)
    val rendererReady = AtomicBoolean(false)
    println("Game intro: window=${views.gameWindow.javaClass.simpleName}, focused=${frame?.isFocused}.")
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
            val scale = max(width / VIDEO_WIDTH, height / VIDEO_HEIGHT)
            video.scaleX = scale
            video.scaleY = scale
            video.x = left + (width - VIDEO_WIDTH * scale) / 2.0
            video.y = top + (height - VIDEO_HEIGHT * scale) / 2.0
            skip.x = left + width - SKIP_BUTTON_WIDTH - SKIP_BUTTON_MARGIN
            skip.y = top + SKIP_BUTTON_MARGIN
            if (rendererReady.compareAndSet(false, true)) decoder?.setPaused(!foreground.get())
            if (texture.applyLatest()) displayedFrames++
            val currentDecoder = decoder
            music.update(currentDecoder?.capturedPositionSeconds, !foreground.get() || !texture.hasVisiblePixels)
            if (watchdog.hasTimedOut(
                    elapsedSeconds = it.toDouble(DurationUnit.SECONDS).coerceAtMost(0.25),
                    isPaused = !foreground.get(),
                    positionSeconds = currentDecoder?.capturedPositionSeconds,
                )
            ) {
                System.err.println("Intro stopped making progress; continuing to the menu.")
                finishIntro(
                    "playback timed out at ${currentDecoder?.positionSeconds}s after $displayedFrames displayed frames.",
                )
            }
        }

    val windowListener =
        object : WindowAdapter() {
            private fun setForeground(active: Boolean) {
                if (foreground.get() != active) println("Game intro: foreground=$active.")
                foreground.set(active)
                if (!active) music.pause()
                decoder?.setPaused(!active || !rendererReady.get())
            }

            private fun refreshPlayback() {
                setForeground(frame?.let { it.isFocused && (it.extendedState and Frame.ICONIFIED) == 0 } ?: true)
            }

            override fun windowGainedFocus(event: WindowEvent) = refreshPlayback()

            override fun windowLostFocus(event: WindowEvent) = setForeground(false)

            override fun windowDeactivated(event: WindowEvent) = setForeground(false)

            override fun windowActivated(event: WindowEvent) = refreshPlayback()

            override fun windowIconified(event: WindowEvent) = setForeground(false)

            override fun windowDeiconified(event: WindowEvent) = refreshPlayback()

            override fun windowClosing(event: WindowEvent) {
                finished.completeExceptionally(CancellationException("Game window closed"))
            }
        }
    val keyboardManager = KeyboardFocusManager.getCurrentKeyboardFocusManager()
    val skipKeyboard =
        IntroSkipKeyboard(
            isActive = { foreground.get() && frame?.isFocused == true && !finished.isCompleted },
            canSkip = { texture.hasVisiblePixels },
            onFocus = { views.gameWindow.queue { skipFocusOutline.visible = true } },
            onSkip = { finishIntro("skipped with keyboard key $it.") },
        )
    try {
        val bridge =
            withContext(Dispatchers.Swing) {
                frame?.addWindowListener(windowListener)
                frame?.addWindowFocusListener(windowListener)
                keyboardManager.addKeyEventDispatcher(skipKeyboard)
                foreground.set(frame?.let { it.isFocused && (it.extendedState and Frame.ICONIFIED) == 0 } ?: true)
                try {
                    createDesktopIntroBridge()
                } catch (error: LinkageError) {
                    throw IllegalStateException("The native video decoder is unavailable", error)
                }
            }
        val intro = DesktopIntroDecoder(bridge, source, texture) { finishIntro("decoder finished.") }
        decoder = intro
        withContext(Dispatchers.Swing) {
            // Do not let the native player run ahead of KorGE's first rendered frame.
            intro.setPaused(!foreground.get() || !rendererReady.get())
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
        music.close()
        updater.close()
        overlay.removeFromParent()
        withContext(NonCancellable + Dispatchers.Swing) {
            frame?.removeWindowListener(windowListener)
            frame?.removeWindowFocusListener(windowListener)
            keyboardManager.removeKeyEventDispatcher(skipKeyboard)
        }
        withContext(NonCancellable) { decoder?.close() }
        withContext(NonCancellable) { deleteIntroSource(source) }
    }
}
