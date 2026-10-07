package com.redcrow.dragonsfirstflight.game

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.controller.GameController
import com.redcrow.dragonsfirstflight.ui.screens.createGameplayScreen
import com.redcrow.dragonsfirstflight.ui.screens.createMainMenuScreen
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.solidRect

suspend fun Container.createDragonGame() {
    val controller = GameController(
        gameAreaWidth = DragonGameConfig.GAME_AREA_WIDTH
    )

    solidRect(
        width = DragonGameConfig.VIRTUAL_SIZE.width,
        height = DragonGameConfig.VIRTUAL_SIZE.height,
        color = Colors["#101827"]
    )

    lateinit var menuContainer: Container
    lateinit var gameContainer: Container

    fun updateScreenVisibility() {
        menuContainer.visible = controller.isMenuVisible
        gameContainer.visible = controller.isGameVisible
    }

    menuContainer = createMainMenuScreen(
        onPlay = {
            controller.startGame()
            updateScreenVisibility()
        }
    )

    gameContainer = createGameplayScreen(
        controller = controller,
        onBackToMenu = {
            controller.returnToMenu()
            updateScreenVisibility()
        }
    )

    updateScreenVisibility()
}