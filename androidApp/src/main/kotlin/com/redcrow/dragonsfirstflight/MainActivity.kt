package com.redcrow.dragonsfirstflight

import android.os.Bundle
import com.redcrow.dragonsfirstflight.game.launchDragonGame
import korlibs.event.FocusEvent
import korlibs.render.GameWindowCreationConfig
import korlibs.render.KorgwActivity

class MainActivity :
    KorgwActivity(
        config =
            GameWindowCreationConfig(
                msaa = 1,
                fullscreen = true,
            ),
    ) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep GPU resources available across ordinary Home/Resume interruptions.
        mGLView?.preserveEGLContextOnPause = true
    }

    override suspend fun activityMain() {
        launchDragonGame()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            gameWindow?.let { window ->
                window.queue { window.dispatch(FocusEvent(FocusEvent.Type.BLUR)) }
            }
        }
    }
}
