package com.redcrow.dragonsfirstflight.game.engine

internal class ProjectileField {
    private val active = mutableListOf<Projectile>()
    private var nextId = 0

    fun launch(
        x: Double,
        y: Double,
        size: Double,
        velocityX: Double,
        velocityY: Double = 0.0,
        targetY: Double = y,
        formationDistance: Double = 0.0,
        floatAmplitude: Double = 0.0,
        gravity: Double = 0.0,
    ) {
        active +=
            Projectile(
                id = nextId++,
                x = x,
                y = y,
                size = size,
                velocityX = velocityX,
                velocityY = velocityY,
                targetY = targetY,
                formationDistance = formationDistance,
                floatAmplitude = floatAmplitude,
                gravity = gravity,
            )
    }

    fun update(
        delta: Double,
        leftBoundary: Double,
        rightBoundary: Double,
    ): List<Projectile> {
        val groundHits = mutableListOf<Projectile>()
        for (index in active.indices) {
            val projectile = active[index]
            val moved =
                projectile.copy(
                    x = projectile.x + projectile.velocityX * delta,
                    y = ProjectileMovementCalculator.yAt(projectile, projectile.ageSeconds + delta),
                    ageSeconds = projectile.ageSeconds + delta,
                )
            active[index] = moved
            val visibleBottom = moved.y + moved.size * (1.0 - CollisionProfileCatalog.bossProjectileMargins.bottomRatio)
            val isDescending = moved.velocityY + moved.gravity * moved.ageSeconds > 0.0
            if (
                moved.gravity > 0.0 &&
                isDescending &&
                visibleBottom >= GameEngineConfig.CATAPULT_GROUND_Y &&
                moved.x + moved.size >= leftBoundary &&
                moved.x <= rightBoundary
            ) {
                groundHits += moved
            }
        }
        val groundHitIds = groundHits.mapTo(mutableSetOf()) { it.id }
        active.removeAll {
            it.id in groundHitIds ||
                it.x + it.size < leftBoundary ||
                it.x > rightBoundary ||
                it.y + it.size < 0.0 ||
                it.y > GameEngineConfig.GAME_AREA_HEIGHT
        }
        return groundHits
    }

    fun snapshot(): List<Projectile> = active.toList()

    fun removeColliding(bounds: CollisionBounds): List<Projectile> {
        val hits = mutableListOf<Projectile>()
        for (index in active.lastIndex downTo 0) {
            val projectile = active[index]
            val projectileBounds =
                CollisionBoundsCalculator.calculate(
                    x = projectile.x,
                    y = projectile.y,
                    width = projectile.size,
                    height = projectile.size,
                    margins = CollisionProfileCatalog.bossProjectileMargins,
                )
            if (collisionBoundsOverlap(bounds, projectileBounds)) {
                hits += active.removeAt(index)
            }
        }
        return hits
    }

    fun reset() {
        active.clear()
        nextId = 0
    }
}
