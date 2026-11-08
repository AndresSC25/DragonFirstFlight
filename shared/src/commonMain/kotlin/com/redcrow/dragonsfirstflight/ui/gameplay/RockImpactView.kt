package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.sliceWithSize
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

internal const val ROCK_IMPACT_PATH = "effects/impact/rock_impact.png"
private const val IMPACT_FRAME_SIZE = 64
private const val ROCK_FRAME_COUNT = 8
private const val SPARK_FRAME_COUNT = 4
private const val ROCK_FRAME_SECONDS = 0.06
private const val SPARK_FRAME_SECONDS = 0.08

/** Snapshot ages make the effect play once and freeze naturally while the game is paused. */
internal class RockImpactView private constructor(
    private val container: Container,
    private val rockFrames: List<BmpSlice>,
    private val sparkFrames: List<BmpSlice>,
) {
    private val rocks = mutableListOf<Image>()
    private val sparks = mutableListOf<Image>()

    fun update(snapshot: GameSnapshot) {
        val impacts = if (snapshot.isZoneTransitioning) emptyList() else snapshot.rockImpacts
        while (rocks.size < impacts.size) {
            rocks += container.image(rockFrames[0]) { smoothing = false }
            sparks += container.image(sparkFrames[0]) { smoothing = false }
        }
        impacts.forEachIndexed { index, impact ->
            val rock = rocks[index]
            rock.bitmap = rockFrames[(impact.ageSeconds / ROCK_FRAME_SECONDS).toInt().coerceIn(0, ROCK_FRAME_COUNT - 1)]
            rock.scaleX = impact.size / IMPACT_FRAME_SIZE
            rock.scaleY = rock.scaleX
            rock.x = impact.x - impact.size / 2.0
            rock.y = impact.y - impact.size / 2.0
            rock.visible = true

            val spark = sparks[index]
            val sparkFrame = (impact.ageSeconds / SPARK_FRAME_SECONDS).toInt()
            // Dragon collisions already display HitSparkView; add the same flash only for floor hits.
            spark.visible = impact.isGroundImpact && sparkFrame < SPARK_FRAME_COUNT
            spark.bitmap = sparkFrames[sparkFrame.coerceIn(0, SPARK_FRAME_COUNT - 1)]
            spark.x = impact.x - IMPACT_FRAME_SIZE / 2.0
            spark.y = impact.y + impact.size / 2.0 - IMPACT_FRAME_SIZE / 2.0
        }
        for (index in impacts.size until rocks.size) {
            rocks[index].visible = false
            sparks[index].visible = false
        }
    }

    companion object {
        fun create(
            container: Container,
            rockMap: Bitmap,
            sparkMap: Bitmap,
        ): RockImpactView {
            require(rockMap.width == IMPACT_FRAME_SIZE * ROCK_FRAME_COUNT && rockMap.height == IMPACT_FRAME_SIZE)
            require(sparkMap.width == IMPACT_FRAME_SIZE * SPARK_FRAME_COUNT && sparkMap.height == IMPACT_FRAME_SIZE)

            fun frames(
                bitmap: Bitmap,
                count: Int,
            ): List<BmpSlice> =
                List(count) { index ->
                    bitmap.sliceWithSize(index * IMPACT_FRAME_SIZE, 0, IMPACT_FRAME_SIZE, IMPACT_FRAME_SIZE)
                }

            return RockImpactView(container, frames(rockMap, ROCK_FRAME_COUNT), frames(sparkMap, SPARK_FRAME_COUNT))
        }
    }
}
