package com.redcrow.dragonsfirstflight.ui.screens

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.skins.DragonSkin
import com.redcrow.dragonsfirstflight.game.skins.SkinShop
import com.redcrow.dragonsfirstflight.ui.components.MEDIEVAL_PANEL_BORDER
import com.redcrow.dragonsfirstflight.ui.components.createMedievalImageButton
import com.redcrow.dragonsfirstflight.ui.components.medievalPanel
import com.redcrow.dragonsfirstflight.ui.components.skinCard
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveBackdrop
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveShade
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.view.Container
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.Text
import korlibs.korge.view.Views
import korlibs.korge.view.addUpdater
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.text

private const val BACKGROUND_PATH = "backgrounds/zone1/fortress_courtyard.png"
private const val PANEL_PATH = "ui/panels/panel_medieval.png"
private const val BUTTON_PATH = "ui/buttons/button_normal.png"
private const val COIN_ICON_PATH = "sprites/coins/coin_icon.png"
private const val PANEL_WIDTH = 1000.0
private const val PANEL_HEIGHT = 600.0
private const val PANEL_PADDING = 24.0
private const val CARD_HEIGHT = 248.0
private const val CARD_GAP = 14.0
private const val COIN_ICON_SIZE = 28.0
private const val COIN_TEXT_GAP = 8.0

internal suspend fun Container.createShopScreen(
    views: Views,
    skinShop: SkinShop,
    skinAnimations: Map<String, SpriteAnimation>,
    onBack: () -> Unit,
): Container {
    val backgroundBitmap = resourcesVfs[BACKGROUND_PATH].readBitmap()
    val panelBitmap = resourcesVfs[PANEL_PATH].readBitmap()
    val buttonBitmap = resourcesVfs[BUTTON_PATH].readBitmap()
    val coinIconBitmap = resourcesVfs[COIN_ICON_PATH].readBitmap()

    return container {
        val screenWidth = DragonGameConfig.VIRTUAL_SIZE.width
        val screenHeight = DragonGameConfig.VIRTUAL_SIZE.height
        val panelX = (screenWidth - PANEL_WIDTH) / 2.0
        val panelY = (screenHeight - PANEL_HEIGHT) / 2.0
        val innerX = panelX + MEDIEVAL_PANEL_BORDER
        val innerY = panelY + MEDIEVAL_PANEL_BORDER
        val innerWidth = PANEL_WIDTH - 2.0 * MEDIEVAL_PANEL_BORDER
        val innerHeight = PANEL_HEIGHT - 2.0 * MEDIEVAL_PANEL_BORDER

        createResponsiveBackdrop(
            bitmap = backgroundBitmap,
            views = views,
        )
        createResponsiveShade(
            views = views,
            color = Colors.BLACK,
            shadeAlpha = 0.24,
        )

        solidRect(
            width = innerWidth,
            height = innerHeight,
            color = Colors["#101827"],
        ) {
            x = innerX
            y = innerY
            alpha = 0.60
        }
        medievalPanel(panelBitmap, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT)

        val title =
            text(
                text = "Shop",
                textSize = 46.0,
                color = Colors["#F8D66D"],
            )
        title.x = (screenWidth - title.width) / 2.0
        title.y = innerY + 15.0

        val balanceRow = container { y = innerY + 78.0 }
        val balanceText = balanceRow.coinAmount(coinIconBitmap, skinShop.balance, textSize = 28.0)

        val saleSkins = DragonSkin.entries.filter { it.price > 0 }
        val cardWidth =
            minOf(
                160.0,
                (innerWidth - 2.0 * PANEL_PADDING - CARD_GAP * (saleSkins.size - 1)) /
                    saleSkins.size,
            )
        val rowWidth = cardWidth * saleSkins.size + CARD_GAP * (saleSkins.size - 1)
        val cards =
            saleSkins.mapIndexed { index, skin ->
                val card =
                    container {
                        x = (screenWidth - rowWidth) / 2.0 + index * (cardWidth + CARD_GAP)
                        y = innerY + 120.0
                    }
                card.skinCard(
                    skin = skin,
                    animation = skinAnimations.getValue(skin.spritePath),
                    width = cardWidth,
                    height = CARD_HEIGHT,
                )

                val priceRow = card.container { y = 156.0 }
                val priceText = priceRow.coinAmount(coinIconBitmap, skin.price, textSize = 24.0)
                priceRow.x = (cardWidth - coinAmountWidth(priceText)) / 2.0

                val owned =
                    card.text(
                        text = "Owned",
                        textSize = 22.0,
                        color = Colors["#F8D66D"],
                    ) {
                        x = (cardWidth - width) / 2.0
                        y = 190.0 + (50.0 - height) / 2.0
                    }
                val buy =
                    card.container {
                        createMedievalImageButton(
                            x = 10.0,
                            y = 190.0,
                            width = cardWidth - 20.0,
                            height = 50.0,
                            texture = buttonBitmap,
                            label = "Buy",
                            textVerticalOffset = 0.0,
                            onClick = { skinShop.buy(skin) },
                        )
                    }
                val refresh: () -> Unit = {
                    val isOwned = skin in skinShop.unlocked
                    val affordable = skinShop.canBuy(skin)
                    owned.visible = isOwned
                    buy.visible = !isOwned
                    buy.alpha = if (affordable) 1.0 else 0.45
                    priceText.color = if (isOwned || affordable) Colors.WHITE else Colors["#C77979"]
                }
                refresh
            }

        var renderedBalance = -1
        addUpdater {
            val balance = skinShop.balance
            if (balance != renderedBalance) {
                renderedBalance = balance
                balanceText.text = balance.toString()
                balanceRow.x = (screenWidth - coinAmountWidth(balanceText)) / 2.0
                cards.forEach { it() }
            }
        }

        createMedievalImageButton(
            x = (screenWidth - 220.0) / 2.0,
            y = innerY + innerHeight - PANEL_PADDING - 64.0,
            width = 220.0,
            height = 64.0,
            texture = buttonBitmap,
            label = "Back",
            textSize = 28.0,
            onClick = onBack,
        )
    }
}

private fun Container.coinAmount(
    icon: Bitmap,
    amount: Int,
    textSize: Double,
): Text {
    image(texture = icon) {
        scaleX = COIN_ICON_SIZE / icon.width.toDouble()
        scaleY = COIN_ICON_SIZE / icon.height.toDouble()
        smoothing = true
    }
    return text(
        text = amount.toString(),
        textSize = textSize,
        color = Colors.WHITE,
    ) {
        x = COIN_ICON_SIZE + COIN_TEXT_GAP
    }
}

private fun coinAmountWidth(amountText: Text): Double = COIN_ICON_SIZE + COIN_TEXT_GAP + amountText.width
