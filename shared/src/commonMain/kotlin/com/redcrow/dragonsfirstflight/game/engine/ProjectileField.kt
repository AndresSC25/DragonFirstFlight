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
    ) {
        active += Projectile(nextId++, x, y, size, velocityX, velocityY)
    }

    fun update(
        delta: Double,
        leftBoundary: Double,
        rightBoundary: Double,
    ) {
        for (index in active.indices) {
            val projectile = active[index]
            active[index] =
                projectile.copy(
                    x = projectile.x + projectile.velocityX * delta,
                    y = projectile.y + projectile.velocityY * delta,
                    ageSeconds = projectile.ageSeconds + delta,
                )
        }
        active.removeAll {
            it.x + it.size < leftBoundary ||
                it.x > rightBoundary ||
                it.y + it.size < 0.0 ||
                it.y > GameEngineConfig.GAME_AREA_HEIGHT
        }
    }

    fun snapshot(): List<Projectile> = active.toList()

    fun reset() {
        active.clear()
        nextId = 0
    }
}
