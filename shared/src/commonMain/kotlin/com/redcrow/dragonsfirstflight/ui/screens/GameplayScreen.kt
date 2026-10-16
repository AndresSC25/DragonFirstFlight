package com.redcrow.dragonsfirstflight.ui.screens

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.controller.GameController
import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import com.redcrow.dragonsfirstflight.ui.components.GameplayHud
import com.redcrow.dragonsfirstflight.ui.components.SessionOverlay
import com.redcrow.dragonsfirstflight.ui.gameplay.DragonView
import com.redcrow.dragonsfirstflight.ui.gameplay.ObstacleView
import com.redcrow.dragonsfirstflight.ui.gameplay.ScrollingBackground
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveBackdrop
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

private val ZONE_ONE_OBSTACLE_PATHS = listOf(
    "sprites/obstacles/zone1/broken_cart.png",
    "sprites/obstacles/zone1/broken_stone_column.png",
    "sprites/obstacles/zone1/hanging_banner.png",
    "sprites/obstacles/zone1/training_dummy.png",
    "sprites/obstacles/zone1/wooden_barrier.png",
    "sprites/obstacles/zone1/wooden_crates.png"
)


internal suspend fun Container.createGameplayScreen(
    views: Views,
    controller: GameController,
    onBackToMenu: () -> Unit
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

    val obstacleBitmaps = ZONE_ONE_OBSTACLE_PATHS.associateWith {
        resourcesVfs[it].readBitmap()
    }

    return container {
        lateinit var dragonView: DragonView
        lateinit var obstacleView: ObstacleView
        lateinit var scrollingBackground: ScrollingBackground
        val backgroundWidth =
            DragonGameConfig.GAME_AREA_WIDTH

        createResponsiveBackdrop(
            bitmap = zoneOneBackground,
            views = views
        )

        val gameArea = clipContainer(
            size = Size(
                width = DragonGameConfig.GAME_AREA_WIDTH,
                height = GameEngineConfig.GAME_AREA_HEIGHT
            )
        ) {
            x = DragonGameConfig.GAME_AREA_X
            y = DragonGameConfig.GAME_AREA_Y

            scrollingBackground = ScrollingBackground.create(
                container = this,
                bitmap = zoneOneBackground,
                width = backgroundWidth,
                height = GameEngineConfig.GAME_AREA_HEIGHT
            )

            dragonView = DragonView.create(
                container = this,
                spriteMap = dragonSpriteMap
            )

            obstacleView = ObstacleView.create(
                container = this,
                obstacleBitmaps = obstacleBitmaps
            )
        }

        gameArea.onClick {
            if (controller.isGameRunning) {
                controller.handleGameAreaInput()
            }
        }

        val gameplayHud = GameplayHud.create(
            container = this,
            lifeFullBitmap = lifeFullBitmap,
            lifeEmptyBitmap = lifeEmptyBitmap,
            gameButtonBitmap = gameButtonBitmap,
            onPause = controller::pauseGame,
            onBackToMenu = onBackToMenu
        )

        val sessionOverlay = SessionOverlay.create(
            container = this,
            panelBitmap = sessionPanelBitmap,
            buttonBitmap = gameButtonBitmap,
            onResume = controller::resumeGame,
            onRestart = controller::startGame,
            onBackToMenu = onBackToMenu
        )

        fun updateView(snapshot: GameSnapshot) {

            obstacleView.update(snapshot)
            gameplayHud.update(
                snapshot = snapshot,
                isPaused = controller.isPaused
            )

            sessionOverlay.update(
                snapshot = snapshot,
                isPaused = controller.isPaused
            )
        }

        gameArea.addUpdater { delta ->
            val elapsedSeconds =
                delta.toDouble(DurationUnit.SECONDS)

            controller.update(elapsedSeconds)

            val snapshot = controller.snapshot()

            if (
                snapshot.status == GameStatus.RUNNING &&
                !controller.isPaused
            ) {
                scrollingBackground.update(elapsedSeconds)
            }
            dragonView.update(
                snapshot = snapshot,
                isPaused = controller.isPaused
            )
            updateView(snapshot)
        }

        val initialSnapshot = controller.snapshot()
        dragonView.update(
            snapshot = initialSnapshot,
            isPaused = controller.isPaused
        )
        updateView(initialSnapshot)
    }
}
