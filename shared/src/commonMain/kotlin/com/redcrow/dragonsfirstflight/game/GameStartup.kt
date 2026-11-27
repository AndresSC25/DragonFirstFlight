package com.redcrow.dragonsfirstflight.game

import kotlinx.coroutines.CancellationException

/** Runs [showIntro], then always [showMenu]; an intro failure is logged and skipped, cancellation propagates. */
internal suspend fun runGameStartup(
    showIntro: suspend () -> Unit,
    showMenu: suspend () -> Unit,
) {
    try {
        showIntro()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        println("Game intro unavailable; continuing to the menu: $error")
    }
    showMenu()
}
