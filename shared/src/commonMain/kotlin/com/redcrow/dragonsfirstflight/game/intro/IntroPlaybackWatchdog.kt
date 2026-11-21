package com.redcrow.dragonsfirstflight.game.intro

/** Detects startup and playback stalls using foreground time only. */
class IntroPlaybackWatchdog(
    private val startupTimeoutSeconds: Double = 15.0,
    private val stallTimeoutSeconds: Double = 5.0,
) {
    private var waitingSeconds = 0.0
    private var lastPositionSeconds: Double? = null

    init {
        require(startupTimeoutSeconds.isFinite() && startupTimeoutSeconds > 0.0)
        require(stallTimeoutSeconds.isFinite() && stallTimeoutSeconds > 0.0)
    }

    fun hasTimedOut(
        elapsedSeconds: Double,
        isPaused: Boolean,
        positionSeconds: Double?,
    ): Boolean {
        if (isPaused || !elapsedSeconds.isFinite() || elapsedSeconds <= 0.0) return false
        val validPosition = positionSeconds?.takeIf { it.isFinite() && it >= 0.0 }
        val previous = lastPositionSeconds
        if (validPosition != null && (previous == null || validPosition != previous)) {
            waitingSeconds = 0.0
            lastPositionSeconds = validPosition
            return false
        }
        waitingSeconds += elapsedSeconds
        val timeout = if (previous == null) startupTimeoutSeconds else stallTimeoutSeconds
        return waitingSeconds >= timeout
    }
}
