package com.example.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class DocumentedFinancialRulesTest {

    @Test fun positiveMonthlyEquivalentHasPriority() {
        assertEquals(
            125_000L,
            DocumentedFinancialRules.certificateMonthlyEquivalentProfitPiastres(
                monthlyEquivalentProfitPiastres = 125_000L,
                principalPiastres = 10_000_000L,
                annualRatePercent = BigDecimal("24"),
                payoutFrequency = "MONTHLY",
                durationMonths = 36
            )
        )
    }

    @Test fun annualRateFallbackProducesMonthlyEquivalent() {
        assertEquals(
            200_000L,
            DocumentedFinancialRules.certificateMonthlyEquivalentProfitPiastres(
                principalPiastres = 10_000_000L,
                annualRatePercent = BigDecimal("24"),
                payoutFrequency = "MONTHLY"
            )
        )
    }

    @Test fun atMaturityFallbackAveragesAcrossDuration() {
        assertEquals(
            50_000L,
            DocumentedFinancialRules.certificateMonthlyEquivalentProfitPiastres(
                profitAmountPiastres = 1_800_000L,
                payoutFrequency = "AT_MATURITY",
                durationMonths = 36
            )
        )
    }

    @Test fun currentAccountWithdrawalsAreSubtractedOnce() {
        assertEquals(
            70_000L,
            DocumentedFinancialRules.currentAccountNetPiastres(
                depositsPiastres = listOf(100_000L),
                withdrawalsPiastres = listOf(30_000L)
            )
        )
    }

    @Test fun netIncomeAndRatiosFollowDocumentedBounds() {
        val net = DocumentedFinancialRules.netIncomePiastres(200_000L, 150_000L)
        assertEquals(50_000L, net)
        assertEquals(25, DocumentedFinancialRules.savingsRatePercent(200_000L, net))
        assertEquals(75, DocumentedFinancialRules.expenseRatioPercent(200_000L, 150_000L))
        assertEquals(0, DocumentedFinancialRules.savingsRatePercent(0L, net))
        assertEquals(100, DocumentedFinancialRules.expenseRatioPercent(0L, 150_000L))
    }
}
