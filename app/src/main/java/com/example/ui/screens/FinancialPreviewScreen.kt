package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider

private val PreviewInk = Color(0xFF171717)
private val PreviewGold = Color(0xFFD4AF37)
private val PreviewCanvas = Color(0xFFF7F6F2)
private val PreviewBorder = Color(0xFFE8DFC9)

@Composable
fun FinancialPreviewScreen(
    sectionId: String,
    onBack: () -> Unit
) {
    var showPhaseTwoNotice by remember { mutableStateOf(false) }
    val isAssociation = sectionId == "gamaeya"
    val title = if (isAssociation) "الجمعيات" else "الحساب الجاري"
    val subtitle = if (isAssociation)
        "متابعة الاشتراكات والأقساط ومواعيد القبض"
    else
        "عرض الحركات المالية والأرصدة والتحويلات"
    val icon = if (isAssociation) Icons.Outlined.Groups else Icons.Outlined.AccountBalanceWallet
    val metrics = if (isAssociation)
        listOf("قيمة القسط", "إجمالي المدفوع", "موعد القبض")
    else
        listOf("الرصيد الحالي", "إجمالي الإيداعات", "إجمالي السحوبات")
    val actions = if (isAssociation)
        listOf("جمعية جديدة", "تسجيل قسط", "تسجيل قبض")
    else
        listOf("إضافة إيداع", "إضافة سحب", "تحويل بين الحسابات")

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = PreviewCanvas,
            topBar = {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowForward, contentDescription = "رجوع", tint = PreviewInk) }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = PreviewInk)
                        Text(subtitle, fontSize = 11.sp, color = Color(0xFF77736A))
                    }
                    Icon(icon, contentDescription = null, tint = PreviewGold, modifier = Modifier.size(27.dp))
                }
            }
        ) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PreviewInk),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("ملخص ${title}", color = PreviewGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("—", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                            Text("ستظهر البيانات الفعلية بعد تفعيل الوظائف في المرحلة الثانية", color = Color(0xFFE7E3D8), fontSize = 11.sp)
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        metrics.forEach { label ->
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, PreviewBorder)
                            ) {
                                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(label, color = Color(0xFF77736A), fontSize = 10.sp)
                                    Text("—", color = PreviewInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                item {
                    Text("الإجراءات", color = PreviewInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        actions.forEachIndexed { index, action ->
                            OutlinedButton(
                                onClick = { showPhaseTwoNotice = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(13.dp),
                                border = BorderStroke(1.dp, PreviewGold),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp)
                            ) {
                                Icon(
                                    if (index == 0) Icons.Outlined.AddCircleOutline else Icons.Outlined.EditNote,
                                    contentDescription = null,
                                    tint = PreviewInk
                                )
                                Spacer(Modifier.width(9.dp))
                                Text(action, color = PreviewInk, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                Text("قريبًا", color = Color(0xFF9B7921), fontSize = 10.sp)
                            }
                        }
                    }
                }
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, PreviewBorder)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(if (isAssociation) "الجمعيات المسجلة" else "آخر الحركات", color = PreviewInk, fontWeight = FontWeight.Bold)
                            Text("لا توجد بيانات للعرض في المعاينة التصميمية.", color = Color(0xFF77736A), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        if (showPhaseTwoNotice) {
            AlertDialog(
                onDismissRequest = { showPhaseTwoNotice = false },
                title = { Text("الوظيفة في المرحلة الثانية", fontWeight = FontWeight.Bold) },
                text = { Text("هذه الأزرار معروضة لمراجعة التصميم فقط. سيتم تفعيل التسجيل والحفظ والحسابات بعد اعتماد جميع الشاشات.") },
                confirmButton = {
                    TextButton(onClick = { showPhaseTwoNotice = false }) { Text("حسنًا") }
                },
                containerColor = Color.White
            )
        }
    }
}
