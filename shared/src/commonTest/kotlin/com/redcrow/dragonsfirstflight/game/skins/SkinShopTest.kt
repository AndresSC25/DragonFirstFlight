package com.redcrow.dragonsfirstflight.game.skins

import com.redcrow.dragonsfirstflight.game.coins.CoinWallet
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SkinShopTest {
    @Test
    fun purchaseDeductsPriceUnlocksAndSurvivesRestart() {
        val storage = InmemoryStorage()
        val wallet = CoinWallet(storage)
        val inventory = SkinInventory(storage)
        val shop = SkinShop(wallet, inventory)
        wallet.deposit(DragonSkin.LDA.price + 100)

        assertTrue(shop.canBuy(DragonSkin.LDA))
        assertTrue(shop.buy(DragonSkin.LDA))
        assertEquals(100, shop.balance)
        assertTrue(inventory.isUnlocked(DragonSkin.LDA))
        assertEquals(DragonSkin.DEFAULT, inventory.equipped)

        val restarted = SkinShop(CoinWallet(storage), SkinInventory(storage))
        assertEquals(100, restarted.balance)
        assertTrue(DragonSkin.LDA in restarted.unlocked)
    }

    @Test
    fun insufficientBalanceChangesNothing() {
        val storage = InmemoryStorage()
        val wallet = CoinWallet(storage)
        val inventory = SkinInventory(storage)
        val shop = SkinShop(wallet, inventory)
        wallet.deposit(DragonSkin.LDA.price - 1)
        val storedBalance = storage.getOrNull("coins.balance")

        assertFalse(shop.canBuy(DragonSkin.LDA))
        assertFalse(shop.buy(DragonSkin.LDA))
        assertEquals(DragonSkin.LDA.price - 1, shop.balance)
        assertEquals(storedBalance, storage.getOrNull("coins.balance"))
        assertFalse(inventory.isUnlocked(DragonSkin.LDA))
    }

    @Test
    fun duplicatePurchaseChargesOnce() {
        val storage = InmemoryStorage()
        val wallet = CoinWallet(storage)
        val shop = SkinShop(wallet, SkinInventory(storage))
        wallet.deposit(DragonSkin.LDA.price * 2)

        assertTrue(shop.buy(DragonSkin.LDA))
        assertFalse(shop.buy(DragonSkin.LDA))
        assertEquals(DragonSkin.LDA.price, shop.balance)
    }

    @Test
    fun exactBalancePurchaseEndsAtZero() {
        val storage = InmemoryStorage()
        val wallet = CoinWallet(storage)
        val shop = SkinShop(wallet, SkinInventory(storage))
        wallet.deposit(DragonSkin.CARTAGO.price)

        assertTrue(shop.canBuy(DragonSkin.CARTAGO))
        assertTrue(shop.buy(DragonSkin.CARTAGO))
        assertEquals(0, shop.balance)
    }
}
