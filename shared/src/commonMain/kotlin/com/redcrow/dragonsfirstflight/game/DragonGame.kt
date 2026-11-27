package com.redcrow.dragonsfirstflight.game

import com.redcrow.dragonsfirstflight.game.audio.GameAudio
import com.redcrow.dragonsfirstflight.game.audio.GameSound
import com.redcrow.dragonsfirstflight.game.coins.CoinWallet
import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.controller.GameController
import com.redcrow.dragonsfirstflight.game.skins.DragonSkin
import com.redcrow.dragonsfirstflight.game.skins.NyanSkin
import com.redcrow.dragonsfirstflight.game.skins.SkinInventory
import com.redcrow.dragonsfirstflight.game.skins.SkinShop
import com.redcrow.dragonsfirstflight.ui.gameplay.createDragonFlightAnimation
import com.redcrow.dragonsfirstflight.ui.screens.createEndlessSetupScreen
import com.redcrow.dragonsfirstflight.ui.screens.createGameplayScreen
import com.redcrow.dragonsfirstflight.ui.screens.createInventoryScreen
import com.redcrow.dragonsfirstflight.ui.screens.createMainMenuScreen
import com.redcrow.dragonsfirstflight.ui.screens.createShopScreen
import korlibs.event.FocusEvent
import korlibs.event.PauseEvent
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.service.storage.storage
import korlibs.korge.view.Container
import korlibs.korge.view.Views
import korlibs.korge.view.solidRect
import korlibs.render.GameWindow

/**
 * Builds the menu, endless setup, inventory, shop and gameplay screens in this container and wires them to one
 * [GameController]. Only the screen matching the controller state is visible.
 */
suspend fun Container.createDragonGame(views: Views) {
    val audio = GameAudio.load(views.coroutineContext, views.storage)
    audio.bindWindow(views.gameWindow)
    val coinWallet = CoinWallet(views.storage)
    val controller =
        GameController(
            gameAreaWidth = DragonGameConfig.GAME_AREA_WIDTH,
            coinWallet = coinWallet,
        )
    pauseGameWhenWindowInactive(controller, views.gameWindow)

    val skinInventory = SkinInventory(views.storage)
    val skinShop = SkinShop(wallet = coinWallet, inventory = skinInventory)
    val skinAnimations =
        (DragonSkin.entries.map { it.spritePath } + NyanSkin.entries.map { it.spritePath })
            .distinct()
            .associateWith { path ->
                createDragonFlightAnimation(resourcesVfs[path].readBitmap())
            }

    solidRect(
        width = DragonGameConfig.VIRTUAL_SIZE.width,
        height = DragonGameConfig.VIRTUAL_SIZE.height,
        color = Colors["#101827"],
    )

    lateinit var menuContainer: Container
    var gameContainer: Container? = null
    var inventoryContainer: Container? = null
    var shopContainer: Container? = null
    var endlessSetupContainer: Container? = null

    fun updateScreenVisibility() {
        menuContainer.visible = controller.isMenuVisible
        gameContainer?.visible = controller.isGameVisible
        inventoryContainer?.visible = controller.isInventoryVisible
        shopContainer?.visible = controller.isShopVisible
        endlessSetupContainer?.visible = controller.isEndlessSetupVisible
    }

    menuContainer =
        createMainMenuScreen(
            views = views,
            audio = audio,
            skinInventory = skinInventory,
            skinAnimations = skinAnimations,
            onPlay = { difficulty ->
                controller.startGame(difficulty)
                updateScreenVisibility()
            },
            onEndless = { difficulty ->
                audio.play(GameSound.MENU_CLICK)
                controller.openEndlessSetup(difficulty)
                updateScreenVisibility()
            },
            onInventory = {
                audio.play(GameSound.MENU_CLICK)
                controller.openInventory()
                updateScreenVisibility()
            },
            onShop = {
                audio.play(GameSound.MENU_CLICK)
                controller.openShop()
                updateScreenVisibility()
            },
        )

    endlessSetupContainer =
        createEndlessSetupScreen(
            views = views,
            audio = audio,
            controller = controller,
            onStart = {
                controller.confirmEndlessSetup()
                updateScreenVisibility()
            },
            onBack = {
                audio.play(GameSound.MENU_BACK)
                controller.returnToMenu()
                updateScreenVisibility()
            },
        )

    inventoryContainer =
        createInventoryScreen(
            views = views,
            audio = audio,
            skinInventory = skinInventory,
            skinAnimations = skinAnimations,
            onBack = {
                audio.play(GameSound.MENU_BACK)
                controller.returnToMenu()
                updateScreenVisibility()
            },
        )

    shopContainer =
        createShopScreen(
            views = views,
            audio = audio,
            skinShop = skinShop,
            skinAnimations = skinAnimations,
            onBack = {
                audio.play(GameSound.MENU_BACK)
                controller.returnToMenu()
                updateScreenVisibility()
            },
        )

    gameContainer =
        createGameplayScreen(
            views = views,
            audio = audio,
            controller = controller,
            skinInventory = skinInventory,
            skinAnimations = skinAnimations,
            onBackToMenu = {
                audio.stopEffects()
                audio.music(active = false, paused = false, boss = false)
                audio.charge(active = false, paused = false)
                audio.play(GameSound.MENU_BACK)
                controller.returnToMenu()
                updateScreenVisibility()
            },
        )

    updateScreenVisibility()
}

/** Pauses the running game when the window loses focus or the platform pauses the app. */
internal fun pauseGameWhenWindowInactive(
    controller: GameController,
    window: GameWindow,
) {
    window.onEvent(FocusEvent.Type.BLUR) { controller.pauseGame() }
    window.onEvent(PauseEvent) { controller.pauseGame() }
}
