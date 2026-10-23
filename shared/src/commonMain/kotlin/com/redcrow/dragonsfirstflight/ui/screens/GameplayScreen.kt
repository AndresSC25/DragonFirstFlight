package com.redcrow.dragonsfirstflight.ui.screens

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.controller.GameController
import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import com.redcrow.dragonsfirstflight.ui.components.GameplayHud
import com.redcrow.dragonsfirstflight.ui.components.SessionOverlay
import com.redcrow.dragonsfirstflight.ui.gameplay.DragonView
import com.redcrow.dragonsfirstflight.ui.gameplay.ANIMATED_OBSTACLE_CONFIGS
import com.redcrow.dragonsfirstflight.ui.gameplay.FLYING_BATS_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.FLYING_CROWS_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.HitSparkView
import com.redcrow.dragonsfirstflight.ui.gameplay.ObstacleView
import com.redcrow.dragonsfirstflight.ui.gameplay.ScrollingBackground
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.onClick
import korlibs.korge.view.Container
import korlibs.korge.view.Views
import korlibs.korge.view.addUpdater
import korlibs.korge.view.clipContainer
import korlibs.korge.view.container
import korlibs.math.geom.Size
import kotlin.time.DurationUnit

private const val DRAGON_SPRITESHEET_PATH: String =
    "sprites/dragon/YoungBrassDragon.png"

private const val ZONE_ONE_BACKGROUND_PATH: String =
    "backgrounds/zone1/fortress_courtyard.png"

private const val SESSION_PANEL_PATH: String =
    "ui/panels/panel_medieval.png"

private const val GAME_BUTTON_PATH: String =
    "ui/buttons/button_normal.png"

private const val LIFE_FULL_PATH: String =
    "sprites/dragon/lives/dragon_life_full.png"

private const val LIFE_EMPTY_PATH: String =
    "sprites/dragon/lives/dragon_life_empty.png"

private const val HIT_SPARK_PATH: String =
    "effects/impact/hit_spark.png"

private val ZONE_ONE_OBSTACLE_PATHS =
    listOf(
        FLYING_BATS_SPRITE_PATH,
        FLYING_CROWS_SPRITE_PATH,
    )

internal suspend fun Container.createGameplayScreen(
    views: Views,
    controller: GameController,
    onBackToMenu: () -> Unit,
): Container {
    val zoneOneBackground =
        resourcesVfs[ZONE_ONE_BACKGROUND_PATH].readBitmap()

    val dragonSpriteMap =
        resourcesVfs[DRAGON_SPRITESHEET_PATH].readBitmap()

    val sessionPanelBitmap =
        resourcesVfs[SESSION_PANEL_PATH].readBitmap()

    val gameButtonBitmap =
        resourcesVfs[GAME_BUTTON_PATH].readBitmap()

    val lifeFullBitmap =
        resourcesVfs[LIFE_FULL_PATH].readBitmap()

    val lifeEmptyBitmap =
        resourcesVfs[LIFE_EMPTY_PATH].readBitmap()

    val hitSparkBitmap =
        resourcesVfs[HIT_SPARK_PATH].readBitmap()

    val obstacleBitmaps =
        ZONE_ONE_OBSTACLE_PATHS.associateWith { path ->
            resourcesVfs[path].readBitmap()
        }

    return container {
        val scrollingBackground =
            ScrollingBackground.create(
                container = this,
                bitmap = zoneOneBackground,
                views = views,
            )

        val obstacleLayer =
            container()

        val obstacleView =
            ObstacleView.create(
                container = obstacleLayer,
                obstacleBitmaps = obstacleBitmaps,
                views = views,
                animatedObstacleConfigs = ANIMATED_OBSTACLE_CONFIGS,
            )

        lateinit var dragonView: DragonView
        lateinit var hitSparkView: HitSparkView

        val gameArea =
            clipContainer(
                size =
                    Size(
                        width = DragonGameConfig.GAME_AREA_WIDTH,
                        height = GameEngineConfig.GAME_AREA_HEIGHT,
                    ),
            ) {
                x = DragonGameConfig.GAME_AREA_X
                y = DragonGameConfig.GAME_AREA_Y

                dragonView =
                    DragonView.create(
                        container = this,
                        spriteMap = dragonSpriteMap,
                    )

                hitSparkView =
                    HitSparkView.create(
                        container = this,
                        spriteMap = hitSparkBitmap,
                    )
            }

        gameArea.onClick {
            if (controller.isGameRunning) {
                controller.handleGameAreaInput()
            }
        }

        val gameplayHud =
            GameplayHud.create(
                container = this,
                lifeFullBitmap = lifeFullBitmap,
                lifeEmptyBitmap = lifeEmptyBitmap,
                gameButtonBitmap = gameButtonBitmap,
                onPause = controller::pauseGame,
                onBackToMenu = onBackToMenu,
            )

        val sessionOverlay =
            SessionOverlay.create(
                container = this,
                views = views,
                panelBitmap = sessionPanelBitmap,
                buttonBitmap = gameButtonBitmap,
                onResume = controller::resumeGame,
                onRestart = controller::startGame,
                onBackToMenu = onBackToMenu,
            )

        fun updateView(snapshot: GameSnapshot) {
            obstacleView.update(
                snapshot = snapshot,
                isPaused = controller.isPaused,
            )

            gameplayHud.update(
                snapshot = snapshot,
                isPaused = controller.isPaused,
            )

            sessionOverlay.update(
                snapshot = snapshot,
                isPaused = controller.isPaused,
            )
        }

        addUpdater { delta ->
            val viewportLeft =
                views.actualVirtualLeft.toDouble()

            val viewportRight =
                viewportLeft +
                    views.actualVirtualWidth.toDouble()

            controller.resizeGameArea(
                leftBoundary = viewportLeft,
                rightBoundary = viewportRight,
            )

            val elapsedSeconds =
                delta.toDouble(DurationUnit.SECONDS)

            controller.update(elapsedSeconds)

            val snapshot =
                controller.snapshot()

            if (
                snapshot.status == GameStatus.RUNNING &&
                !controller.isPaused
            ) {
                scrollingBackground.update(elapsedSeconds)
            } else {
                scrollingBackground.update(0.0)
            }

            dragonView.update(
                snapshot = snapshot,
                isPaused = controller.isPaused,
            )

            hitSparkView.update(
                snapshot = snapshot,
                isPaused = controller.isPaused,
            )

            updateView(snapshot)
        }

        val initialViewportLeft =
            views.actualVirtualLeft.toDouble()

        val initialViewportRight =
            initialViewportLeft +
                views.actualVirtualWidth.toDouble()

        controller.resizeGameArea(
            leftBoundary = initialViewportLeft,
            rightBoundary = initialViewportRight,
        )

        val initialSnapshot =
            controller.snapshot()

        scrollingBackground.update(0.0)

        dragonView.update(
            snapshot = initialSnapshot,
            isPaused = controller.isPaused,
        )

        hitSparkView.update(
            snapshot = initialSnapshot,
            isPaused = controller.isPaused,
        )

        updateView(initialSnapshot)
    }
}
