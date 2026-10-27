package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(val route: String, val titleAr: String, val icon: ImageVector) {
    DASHBOARD("dashboard", "الرئيسية", Icons.Default.Dashboard),
    EXPENSES("expenses", "المصروفات", Icons.Default.ReceiptLong),
    VEHICLES("vehicles", "المركبات", Icons.Default.DirectionsCar),
    REVENUES("revenues", "الإيرادات", Icons.Default.AttachMoney),
    REPORTS("reports", "التقارير", Icons.Default.Assessment);

    companion object {
        const val AUTH_ROUTE = "auth"
    }
}
