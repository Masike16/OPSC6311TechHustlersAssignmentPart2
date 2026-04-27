package com.example.easebudgetv1

import com.example.easebudgetv1.utils.CurrencyUtils
import com.example.easebudgetv1.utils.DateUtils
import com.example.easebudgetv1.utils.HashUtils
import org.junit.Test

import org.junit.Assert.*

// (Author, 2024) Unit tests for EasEBudget utility functions
class ExampleUnitTest {
    
    @Test
    fun currencyFormatting_isCorrect() {
        val amount = 2500.75
        val formatted = CurrencyUtils.formatCurrency(amount)
        assertTrue(formatted.contains("R"))
        assertTrue(formatted.contains("2,500.75"))
    }
    
    @Test
    fun currencyParsing_isCorrect() {
        val amountStr = "R 2,500.75"
        val parsed = CurrencyUtils.parseCurrency(amountStr)
        assertEquals(2500.75, parsed, 0.01)
    }
    
    @Test
    fun dateFormatting_isCorrect() {
        val timestamp = System.currentTimeMillis()
        val formatted = DateUtils.formatDate(timestamp)
        assertNotNull(formatted)
        assertTrue(formatted.isNotEmpty())
    }
    
    @Test
    fun hashUtils_isConsistent() {
        val input = "testpassword"
        val hash1 = HashUtils.sha256(input)
        val hash2 = HashUtils.sha256(input)
        assertEquals(hash1, hash2)
        assertEquals(64, hash1.length) // SHA-256 produces 64 character hex string
    }
    
    @Test
    fun hashUtils_isDifferentForDifferentInputs() {
        val input1 = "password1"
        val input2 = "password2"
        val hash1 = HashUtils.sha256(input1)
        val hash2 = HashUtils.sha256(input2)
        assertNotEquals(hash1, hash2)
    }
}