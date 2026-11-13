package com.redcrow.dragonsfirstflight.game.skins

import com.redcrow.dragonsfirstflight.game.coins.CoinWallet

class SkinShop(
    private val wallet: CoinWallet,
    private val inventory: SkinInventory,
) {
    val balance: Int
        get() = wallet.balance

    val unlocked: Set<DragonSkin>
        get() = inventory.unlocked

    fun canBuy(skin: DragonSkin): Boolean = !inventory.isUnlocked(skin) && wallet.balance >= skin.price

    fun buy(skin: DragonSkin): Boolean {
        if (inventory.isUnlocked(skin) || !wallet.spend(skin.price)) return false
        inventory.unlock(skin)
        return true
    }
}
