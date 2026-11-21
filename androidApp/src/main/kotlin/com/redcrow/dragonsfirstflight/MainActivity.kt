package com.redcrow.dragonsfirstflight

import android.os.Bundle
import com.redcrow.dragonsfirstflight.game.launchDragonGame
import korlibs.render.GameWindowCreationConfig
import korlibs.render.KorgwActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class MainActivity :
    KorgwActivity(
        config =
            GameWindowCreationConfig(
                msaa = 1,
                fullscreen = true,
            ),
    ) {
    private var introPlayer: IntroVideoPlayer? = null
    private var foreground = false
    private var introFinished = false
    private var restoredIntroPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        introFinished = savedInstanceState?.getBoolean("intro.finished") ?: false
        restoredIntroPosition = savedInstanceState?.getInt("intro.position") ?: 0
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("intro.finished", introFinished)
        outState.putInt("intro.position", introPlayer?.positionMillis ?: restoredIntroPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        foreground = true
        introPlayer?.resume()
    }

    override fun onPause() {
        foreground = false
        introPlayer?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        introPlayer?.cancel()
        introPlayer = null
        super.onDestroy()
    }

    override suspend fun activityMain() {
        launchDragonGame(
            beforeMenu = {
                val player =
                    withContext(Dispatchers.Main) {
                        if (introFinished) {
                            null
                        } else {
                            IntroVideoPlayer(this@MainActivity, foreground, restoredIntroPosition)
                                .also { introPlayer = it }
                        }
                    }
                try {
                    player?.play()
                } finally {
                    withContext(NonCancellable + Dispatchers.Main) {
                        introFinished = !isChangingConfigurations && !isDestroyed
                        introPlayer = null
                    }
                }
            },
        )
    }
}
