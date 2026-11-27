package com.redcrow.dragonsfirstflight.game.skins

import com.redcrow.dragonsfirstflight.game.coins.CoinWallet

/** Sells skins for coins from the [CoinWallet] and unlocks them in the [SkinInventory]. */
class SkinShop(
    private val wallet: CoinWallet,
    private val inventory: SkinInventory,
) {
    val balance: Int
        get() = wallet.balance

    val unlocked: Set<DragonSkin>
        get() = inventory.unlocked

    /** Returns true when [skin] is still locked and the balance covers its price. */
    fun canBuy(skin: DragonSkin): Boolean = !inventory.isUnlocked(skin) && wallet.balance >= skin.price

    /** Spends the price and unlocks [skin]; returns false, charging nothing, if owned or unaffordable. */
    fun buy(skin: DragonSkin): Boolean {
        if (inventory.isUnlocked(skin) || !wallet.spend(skin.price)) return false
        inventory.unlock(skin)
        return true
    }
}
