package com.redcrow.dragonsfirstflight.game

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GameStartupTest {
    @Test
    fun menuWaitsForIntroCompletion() =
        runBlocking {
            val finished = CompletableDeferred<Unit>()
            val events = mutableListOf<String>()
            val startup =
                launch(start = CoroutineStart.UNDISPATCHED) {
                    runGameStartup(
                        showIntro = {
                            events += "intro"
                            finished.await()
                        },
                        showMenu = { events += "menu" },
                    )
                }

            assertEquals(listOf("intro"), events)
            finished.complete(Unit)
            startup.join()
            assertEquals(listOf("intro", "menu"), events)
        }

    @Test
    fun playbackFailureStillOpensMenuOnce() =
        runBlocking {
            var menuCalls = 0
            runGameStartup(
                showIntro = { error("Unsupported video codec") },
                showMenu = { menuCalls++ },
            )
            assertEquals(1, menuCalls)
        }

    @Test
    fun cancellingStartupDoesNotOpenMenu() =
        runBlocking {
            var menuCalls = 0
            assertFailsWith<CancellationException> {
                runGameStartup(
                    showIntro = { throw CancellationException("Window closed") },
                    showMenu = { menuCalls++ },
                )
            }
            assertEquals(0, menuCalls)
        }
}
