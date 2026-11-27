package com.redcrow.dragonsfirstflight.game.coins

import korlibs.korge.service.storage.IStorage

/**
 * Persistent colones balance.
 *
 * Every deposit writes to storage synchronously (to disk on desktop), so callers should bank coins
 * at safe points (end of run, pause, return to menu) instead of once per pickup.
 */
class CoinWallet(
    private val storage: IStorage,
) {
    var balance: Int = loadBalance()
        private set

    /** Adds [amount] to the balance and saves it. Throws if [amount] is negative. */
    fun deposit(amount: Int) {
        require(amount >= 0) { "Deposit amount must not be negative: $amount" }
        balance += amount
        storage[BALANCE_KEY] = balance.toString()
    }

    /** Subtracts [amount] and saves it, returning false without change when the balance is too low. */
    fun spend(amount: Int): Boolean {
        require(amount >= 0) { "Spend amount must not be negative: $amount" }
        if (amount > balance) return false

        balance -= amount
        storage[BALANCE_KEY] = balance.toString()
        return true
    }

    private fun loadBalance(): Int = storage.getOrNull(BALANCE_KEY)?.toIntOrNull()?.coerceAtLeast(0) ?: 0

    private companion object {
        const val BALANCE_KEY = "coins.balance"
    }
}
