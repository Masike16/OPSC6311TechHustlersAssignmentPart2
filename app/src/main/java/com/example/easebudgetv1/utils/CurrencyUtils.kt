package com.example.easebudgetv1.utils

import java.text.NumberFormat
import java.util.*

// (Author, 2024) Currency formatting utilities for South African Rands
object CurrencyUtils {
    private val southAfricaLocale = Locale("en", "ZA")
    private val currencyFormat = NumberFormat.getCurrencyInstance(southAfricaLocale)

    fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount)
    }

    fun formatCurrencyWithoutSymbol(amount: Double): String {
        val format = NumberFormat.getNumberInstance(southAfricaLocale)
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = 2
        return format.format(amount)
    }

    fun parseCurrency(amount: String): Double? {
        return try {
            val cleanAmount = amount.replace("[^\\d.]".toRegex(), "")
            cleanAmount.toDoubleOrNull()
        } catch (e: NumberFormatException) {
            null
        }
    }
}
