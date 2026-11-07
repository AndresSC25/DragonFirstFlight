package com.redcrow.dragonsfirstflight.game.coins

import korlibs.korge.service.storage.InmemoryStorage
import kotlin.test.Test
import kotlin.test.assertEquals

class CoinWalletTest {
    @Test
    fun corruptStoredValueLoadsAsZero() {
        val storage = InmemoryStorage()
        storage["coins.balance"] = "not-a-number"

        assertEquals(0, CoinWallet(storage).balance)
    }

    @Test
    fun depositAddsAndPersists() {
        val storage = InmemoryStorage()
        val wallet = CoinWallet(storage)

        wallet.deposit(10)
        wallet.deposit(5)

        assertEquals(15, wallet.balance)
        assertEquals(15, CoinWallet(storage).balance)
    }
}
