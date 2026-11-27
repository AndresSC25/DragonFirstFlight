package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EndlessExtraLifeTest {
    @Test
    fun endlessRenewsItsLimitedLifeOffersAfterSixtyActiveSeconds() {
        val field = ExtraLifeField(AlwaysSpawnRandom())
        advance(field, 5.0)
        offer(field, 1)
        assertEquals(0, field.extraLives.single().id)
        advance(field, 18.0)
        offer(field, 2)
        assertEquals(1, field.extraLives.single().id)
        advance(field, 18.0)
        offer(field, 3)
        assertTrue(field.extraLives.isEmpty())
        advance(field, 18.0)
        offer(field, 4)
        assertTrue(field.extraLives.isEmpty())
        advance(field, 1.1)
        offer(field, 5)
        assertEquals(2, field.extraLives.single().id)
    }

    @Test
    fun normalRunsKeepTheirOriginalTwoLifeLimit() {
        val field = ExtraLifeField(AlwaysSpawnRandom())
        advance(field, 5.0, isEndless = false)
        offer(field, 1)
        advance(field, 18.0, isEndless = false)
        offer(field, 2)
        advance(field, 180.0, isEndless = false)
        offer(field, 3)
        assertTrue(field.extraLives.isEmpty())
    }

    @Test
    fun victoryContinuationDoesNotImmediatelyGrantANewLifeBudget() {
        val field = ExtraLifeField(AlwaysSpawnRandom())
        advance(field, 5.0, isEndless = false)
        offer(field, 1)
        advance(field, 18.0, isEndless = false)
        offer(field, 2)
        field.clear()
        advance(field, 30.0)
        offer(field, 3)
        assertTrue(field.extraLives.isEmpty())
        advance(field, 30.0)
        offer(field, 4)
        assertEquals(2, field.extraLives.single().id)
    }

    @Test
    fun lifeCollectionRespectsTheCapAndCannotCollectTheSameLifeTwice() {
        val field = ExtraLifeField(AlwaysSpawnRandom())
        advance(field, 5.0)
        offer(field, 1)
        val life = field.extraLives.single()
        field.advance(0.05, field.screenX(life) - GameEngineConfig.DRAGON_X, 0.0, isEndless = true)
        assertFalse(field.collect(life.y, canIncreaseLives = false))
        assertEquals(1, field.extraLives.size)
        assertTrue(field.collect(life.y, canIncreaseLives = true))
        assertFalse(field.collect(life.y, canIncreaseLives = true))
    }

    @Test
    fun restartClearsTheWindowAndTheOfferCounters() {
        val field = ExtraLifeField(AlwaysSpawnRandom())
        advance(field, 5.0)
        offer(field, 1)
        advance(field, 18.0)
        offer(field, 2)
        field.reset()
        assertTrue(field.extraLives.isEmpty())
        assertEquals(0.0, field.currentScrollX)
        advance(field, 5.0)
        offer(field, 1)
        assertEquals(0, field.extraLives.single().id)
    }

    private fun advance(
        field: ExtraLifeField,
        seconds: Double,
        isEndless: Boolean = true,
    ) {
        field.advance(seconds, seconds * 240.0, 0.0, isEndless = isEndless)
    }

    private fun offer(
        field: ExtraLifeField,
        id: Int,
    ) {
        field.spawnForNewObstacles(
            listOf(
                Obstacle(
                    id = id,
                    x = 1280.0,
                    y = GameEngineConfig.OBSTACLE_Y,
                    size = GameEngineConfig.OBSTACLE_SIZE,
                    spritePath = "sprites/obstacles/zone1/flying_crows.png",
                ),
            ),
            rightBoundary = 1280.0,
            zone = GameZone.ZONE_ONE,
        )
    }

    private class AlwaysSpawnRandom : Random() {
        override fun nextBits(bitCount: Int): Int = 0
    }
}
