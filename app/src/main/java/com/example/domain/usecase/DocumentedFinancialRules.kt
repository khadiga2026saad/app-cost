package com.example.domain.usecase

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Implements the legacy SMART COST monthly income rules without changing the
 * existing cash-balance or certificate-schedule workflows.
 *
 * All monetary inputs and outputs are integer piastres.
 */
object DocumentedFinancialRules {

    fun certificateMonthlyEquivalentProfitPiastres(
        monthlyEquivalentProfitPiastres: Long? = null,
        principalPiastres: Long? = null,
        annualRatePercent: BigDecimal? = null,
        profitAmountPiastres: Long? = null,
        payoutFrequency: String,
        durationMonths: Int? = null
    ): Long {
        monthlyEquivalentProfitPiastres
            ?.takeIf { it > 0L }
            ?.let { return it }

        val principal = principalPiastres?.takeIf { it > 0L }
        val rate = annualRatePercent?.takeIf { it > BigDecimal.ZERO }
        if (principal != null && rate != null) {
            val annualProfit = BigDecimal(principal)
                .multiply(rate)
                .divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
            return annualProfit.divide(BigDecimal(12), 0, RoundingMode.HALF_UP).toLong()
                .takeIf { it > 0L } ?: 0L
        }

        val profit = profitAmountPiastres?.takeIf { it > 0L } ?: return 0L
        val divisor = when (payoutFrequency.trim().uppercase()) {
            "MONTHLY", "شهري" -> 12
            "QUARTERLY", "ربع سنوي" -> 4
            "SEMI_ANNUALLY", "SEMI-ANNUALLY", "نصف سنوي" -> 2
            "ANNUALLY", "سنوي" -> 1
            "AT_MATURITY", "عند الاستحقاق" -> durationMonths?.takeIf { it > 0 } ?: 12
            else -> 12
        }
        return BigDecimal(profit)
            .divide(BigDecimal(divisor), 0, RoundingMode.HALF_UP)
            .toLong()
            .takeIf { it > 0L } ?: 0L
    }

    fun netIncomePiastres(monthlyIncomePiastres: Long, monthlyExpensesPiastres: Long): Long =
        monthlyIncomePiastres - monthlyExpensesPiastres

    fun savingsRatePercent(monthlyIncomePiastres: Long, netIncomePiastres: Long): Int {
        if (monthlyIncomePiastres <= 0L) return 0
        return (netIncomePiastres.toDouble() / monthlyIncomePiastres.toDouble() * 100.0)
            .toInt().coerceIn(0, 100)
    }

    fun expenseRatioPercent(monthlyIncomePiastres: Long, monthlyExpensesPiastres: Long): Int {
        if (monthlyIncomePiastres <= 0L) return 100
        return (monthlyExpensesPiastres.toDouble() / monthlyIncomePiastres.toDouble() * 100.0)
            .toInt().coerceAtMost(100)
    }

    /** Deposits are positive and withdrawals negative; callers pass positive stored amounts. */
    fun currentAccountNetPiastres(
        depositsPiastres: Iterable<Long>,
        withdrawalsPiastres: Iterable<Long>
    ): Long = depositsPiastres.sum() - withdrawalsPiastres.sum()
}
