package com.example.core

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Utility for exact financial calculations adhering to the SMART COST PRO Financial Constitution:
 * - Fundamental internal unit: Piastres (قروش) as [Long].
 * - External display/input unit: Egyptian Pound (ج.م) via [BigDecimal].
 * - Rounding rule: RoundingMode.HALF_UP at final step only.
 * - Zero floating-point tolerance (no Float or Double for currency amounts).
 */
object MoneyUtils {
    const val PIASTRES_PER_EGP = 100L

    val ZERO_PIASTRES: Long = 0L
    val ZERO_EGP: BigDecimal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
    val ZERO: BigDecimal get() = ZERO_EGP

    private val englishDecimalFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        DecimalFormat("#,##0.00", symbols)
    }

    /**
     * Converts Long piastres to BigDecimal EGP (1 EGP = 100 Piastres) with 2 decimal places.
     */
    fun piastresToEgp(piastres: Long): BigDecimal {
        return BigDecimal(piastres)
            .divide(BigDecimal(PIASTRES_PER_EGP), 2, RoundingMode.HALF_UP)
    }

    /**
     * Converts BigDecimal EGP to Long piastres with HALF_UP rounding.
     */
    fun egpToPiastres(egp: BigDecimal): Long {
        return egp.multiply(BigDecimal(PIASTRES_PER_EGP))
            .setScale(0, RoundingMode.HALF_UP)
            .toLong()
    }

    /**
     * Parses raw user string input in EGP (e.g. "150.50" or "١٥٠٫٥٠") to exact Long piastres.
     * Returns null if invalid or <= 0.
     */
    fun parseEgpToPiastres(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        return try {
            val cleaned = cleanNumberString(raw)
            val egp = BigDecimal(cleaned)
            if (egp <= BigDecimal.ZERO) return null
            egpToPiastres(egp)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Parses raw user string input in EGP, returning 0L if null, invalid, or negative.
     */
    fun parseEgpToPiastresAllowZero(raw: String?): Long {
        return parseEgpToPiastres(raw) ?: ZERO_PIASTRES
    }

    fun parseBigDecimal(raw: String?): BigDecimal? {
        if (raw.isNullOrBlank()) return null
        return try {
            val cleaned = cleanNumberString(raw)
            val value = BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP)
            if (value > BigDecimal.ZERO) value else null
        } catch (_: Exception) {
            null
        }
    }

    fun parseBigDecimalAllowZero(raw: String?): BigDecimal {
        return parseBigDecimal(raw) ?: ZERO_EGP
    }

    /**
     * Formats Long piastres into standard currency display (e.g. "1,500.00 ج.م").
     */
    fun formatPiastres(piastres: Long, currency: String = "ج.م"): String {
        val egp = piastresToEgp(piastres)
        return "${englishDecimalFormat.format(egp)} $currency"
    }

    /**
     * Formats Long piastres as plain numeric string (e.g. "1,500.00").
     */
    fun formatPiastresPlain(piastres: Long): String {
        val egp = piastresToEgp(piastres)
        return englishDecimalFormat.format(egp)
    }

    /**
     * Formats BigDecimal EGP amount.
     */
    fun formatEgp(amount: BigDecimal?, currency: String = "ج.م"): String {
        val value = amount ?: ZERO_EGP
        return "${englishDecimalFormat.format(value)} $currency"
    }

    fun format(amount: BigDecimal?, currency: String = "ج.م"): String = formatEgp(amount, currency)

    fun formatPlain(amount: BigDecimal?): String {
        val value = amount ?: ZERO_EGP
        return englishDecimalFormat.format(value)
    }

    fun add(a: BigDecimal, b: BigDecimal): BigDecimal {
        return a.add(b).setScale(2, RoundingMode.HALF_UP)
    }

    fun subtract(a: BigDecimal, b: BigDecimal): BigDecimal {
        return a.subtract(b).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Standard Front-Allocation Algorithm for Penny/Piastre remainder (Section 14 of Constitution):
     * When dividing [totalPiastres] across [count] installments, the base installment is floor(P / N).
     * The remainder R = P - (B * N) is distributed by adding 1 piastre (+1L) to each of the first R installments.
     * Guarantees sum(installments) == totalPiastres with zero loss of piastres.
     */
    fun allocatePennyRemainder(totalPiastres: Long, count: Int): List<Long> {
        require(count > 0) { "Installment count must be greater than zero" }
        require(totalPiastres >= 0) { "Total piastres cannot be negative" }

        val base = totalPiastres / count
        val remainder = (totalPiastres % count).toInt()

        return List(count) { index ->
            if (index < remainder) base + 1L else base
        }
    }

    private fun cleanNumberString(raw: String): String {
        return raw.trim()
            .replace("،", ".")
            .replace(",", ".")
            .replace(" ", "")
            .map { char ->
                when (char) {
                    '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
                    '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'
                    else -> char
                }
            }.joinToString("")
    }
}
