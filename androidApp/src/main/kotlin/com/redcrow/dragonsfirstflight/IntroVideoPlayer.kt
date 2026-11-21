package com.redcrow.dragonsfirstflight

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import android.widget.FrameLayout
import android.widget.VideoView
import com.redcrow.dragonsfirstflight.game.intro.IntroPlaybackWatchdog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

internal class IntroVideoPlayer(
    private val activity: Activity,
    initiallyForeground: Boolean,
    initialPositionMillis: Int = 0,
) {
    private var videoView: VideoView? = null
    private val finished = CompletableDeferred<Unit>()
    private var foreground = initiallyForeground
    private var prepared = false
    private var savedPositionMillis = initialPositionMillis

    val positionMillis: Int
        get() = videoView?.currentPosition?.takeIf { prepared && it > 0 } ?: savedPositionMillis

    fun pause() {
        foreground = false
        savedPositionMillis = positionMillis
        videoView?.suspend()
        prepared = false
    }

    fun resume() {
        foreground = true
        if (!finished.isCompleted) videoView?.resume()
    }

    fun cancel() {
        finished.completeExceptionally(CancellationException("Intro activity destroyed"))
    }

    suspend fun play() {
        val source =
            withContext(Dispatchers.IO) {
                val file = File.createTempFile("game-intro-", ".mp4", activity.cacheDir)
                try {
                    activity.assets.open("video/intro.mp4").use { input ->
                        file.outputStream().use(input::copyTo)
                    }
                    file
                } catch (error: Exception) {
                    file.delete()
                    throw error
                }
            }
        val handler = Handler(Looper.getMainLooper())
        val watchdog = IntroPlaybackWatchdog()
        val poll =
            object : Runnable {
                override fun run() {
                    if (finished.isCompleted) return
                    if (watchdog.hasTimedOut(0.25, !foreground, if (prepared) positionMillis / 1000.0 else null)) {
                        Log.e("GameIntro", "Playback stopped making progress; opening menu.")
                        finished.complete(Unit)
                    } else {
                        handler.postDelayed(this, 250)
                    }
                }
            }
        var overlay: FrameLayout? = null
        try {
            withContext(Dispatchers.Main) {
                if (activity.isDestroyed || finished.isCancelled) {
                    throw CancellationException("Intro activity destroyed before playback")
                }
                val background = FrameLayout(activity)
                background.setBackgroundColor(Color.BLACK)
                background.isClickable = true
                val video = VideoView(activity)
                videoView = video
                background.addView(
                    video,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER,
                    ),
                )
                val density = activity.resources.displayMetrics.density

                fun dp(value: Int): Int = (value * density).toInt()
                val skip =
                    Button(activity).apply {
                        setText(R.string.skip_intro)
                        contentDescription = activity.getString(R.string.skip_intro_description)
                        isAllCaps = false
                        setTextColor(Color.rgb(16, 24, 39))
                        textSize = 24f
                        gravity = Gravity.CENTER
                        minHeight = dp(60)
                        minWidth = dp(160)
                        setPadding(dp(16), dp(8), dp(16), dp(8))
                        this.background = createIntroSkipBackground(activity)
                        setOnClickListener {
                            if (finished.complete(Unit)) isEnabled = false
                        }
                    }
                val buttonLayout =
                    FrameLayout
                        .LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            Gravity.TOP or Gravity.END,
                        ).apply {
                            topMargin = dp(16)
                            marginEnd = dp(16)
                        }
                background.addView(skip, buttonLayout)
                background.setOnApplyWindowInsetsListener { _, insets ->
                    if (Build.VERSION.SDK_INT >= 30) {
                        val safe = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                        buttonLayout.topMargin = dp(16) + safe.top
                        buttonLayout.marginEnd = dp(16) + safe.right
                    } else {
                        @Suppress("DEPRECATION")
                        buttonLayout.topMargin = dp(16) + insets.systemWindowInsetTop
                        @Suppress("DEPRECATION")
                        buttonLayout.marginEnd = dp(16) + insets.systemWindowInsetRight
                    }
                    skip.layoutParams = buttonLayout
                    insets
                }
                overlay = background
                activity.addContentView(
                    background,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    ),
                )
                background.requestApplyInsets()
                video.setOnPreparedListener { player ->
                    if (finished.isCompleted) return@setOnPreparedListener
                    prepared = true
                    player.isLooping = false
                    if (savedPositionMillis > 0) {
                        player.setOnSeekCompleteListener {
                            if (foreground && !finished.isCompleted) video.start()
                        }
                        video.seekTo(savedPositionMillis)
                    } else if (foreground) {
                        video.start()
                    }
                }
                video.setOnCompletionListener { finished.complete(Unit) }
                video.setOnErrorListener { _, what, extra ->
                    Log.e("GameIntro", "Video playback failed: $what / $extra")
                    finished.complete(Unit)
                    true
                }
                video.setVideoURI(Uri.fromFile(source))
                handler.postDelayed(poll, 250)
            }
            finished.await()
            withContext(Dispatchers.Main) {
                videoView?.pause()
                overlay
                    ?.animate()
                    ?.alpha(0f)
                    ?.setDuration(160)
                    ?.start()
            }
            // Consume the Skip release before the menu can receive input.
            delay(180)
        } finally {
            withContext(NonCancellable + Dispatchers.Main) {
                handler.removeCallbacks(poll)
                overlay?.animate()?.cancel()
                videoView?.stopPlayback()
                videoView = null
                prepared = false
                overlay?.let { (it.parent as? ViewGroup)?.removeView(it) }
            }
            withContext(NonCancellable + Dispatchers.IO) { source.delete() }
        }
    }
}
