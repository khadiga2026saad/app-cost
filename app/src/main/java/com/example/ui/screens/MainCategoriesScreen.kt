package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinanceViewModel

private object CategoryPalette {
    val background = Color(0xFFF7F6F2)
    val white = Color.White
    val border = Color(0xFFE8DFC9)
    val ink = Color(0xFF171717)
    val muted = Color(0xFF77736A)
    val gold = Color(0xFFD4AF37)
    val goldSoft = Color(0xFFF8F1D9)
}
private data class CategoryTile(val id: String, val title: String, val subtitle: String, val icon: ImageVector)
private data class CategoryGroup(val title: String, val subtitle: String, val icon: ImageVector, val items: List<CategoryTile>)

@Composable
fun MainCategoriesScreen(
    viewModel: FinanceViewModel? = null,
    onNavigateBack: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onAddNewCategory: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val groups = listOf(
        CategoryGroup("المصروفات", "الأقسام المتخصصة لتسجيل ومراجعة التكاليف", Icons.Outlined.ReceiptLong, listOf(
            CategoryTile("home_expenses", "المنزل", "مصروفات البيت والاحتياجات اليومية", Icons.Outlined.Home),
            CategoryTile("personal_expenses", "الشخصي", "المصروفات والالتزامات الشخصية", Icons.Outlined.Person),
            CategoryTile("medical_expenses", "الطبي", "العلاج والأدوية والخدمات الطبية", Icons.Outlined.MedicalServices),
            CategoryTile("education", "التعليم", "مصاريف الدراسة والطلاب", Icons.Outlined.MenuBook),
            CategoryTile("work_expenses", "العمل", "تكاليف ومصروفات العمل", Icons.Outlined.Work)
        )),
        CategoryGroup("المركبات", "متابعة تكاليف كل مركبة بصورة مستقلة", Icons.Outlined.DirectionsCar, listOf(
            CategoryTile("vehicle_fuel", "الوقود", "تسجيل ومراجعة استهلاك الوقود", Icons.Outlined.LocalGasStation),
            CategoryTile("vehicle_maintenance", "الصيانة", "الصيانة والإصلاحات وقطع الغيار", Icons.Outlined.Build),
            CategoryTile("vehicle_oil", "الزيوت", "تغيير الزيت ومواعيد المتابعة", Icons.Outlined.Opacity)
        )),
        CategoryGroup("الأموال والحسابات", "مصادر الدخل والأوعية والحسابات المالية", Icons.Outlined.AccountBalance, listOf(
            CategoryTile("income", "الإيرادات", "مصادر الدخل والإيرادات", Icons.Outlined.TrendingUp),
            CategoryTile("certificates", "الشهادات البنكية", "الشهادات والعوائد والاستحقاقات", Icons.Outlined.AccountBalance),
            CategoryTile("current_account", "الحساب الجاري", "الحركات والأرصدة والتحويلات", Icons.Outlined.AccountBalanceWallet),
            CategoryTile("gamaeya", "الجمعيات", "الأقساط والقبض ومواعيد الاستحقاق", Icons.Outlined.Groups)
        )),
        CategoryGroup("المتابعة والتحليل", "عرض الأداء المالي ومراجعة النتائج", Icons.Outlined.Analytics, listOf(
            CategoryTile("reports", "التقارير والتحليلات", "ملخصات ومقارنات الفترات المالية", Icons.Outlined.BarChart)
        ))
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = CategoryPalette.background,
            bottomBar = {
                NavigationBar(containerColor = CategoryPalette.white) {
                    NavigationBarItem(selected = false, onClick = onNavigateBack, icon = { Icon(Icons.Outlined.Home, null) }, label = { Text("الرئيسية") })
                    NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Outlined.ReceiptLong, null) }, label = { Text("الأقسام") })
                    NavigationBarItem(selected = false, onClick = { onCategoryClick("reports") }, icon = { Icon(Icons.Outlined.BarChart, null) }, label = { Text("التقارير") })
                    NavigationBarItem(selected = false, onClick = onAccountClick, icon = { Icon(Icons.Outlined.Person, null) }, label = { Text("الحساب") })
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Surface(shape = RoundedCornerShape(20.dp), color = CategoryPalette.ink, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("الأقسام المالية", color = CategoryPalette.gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("كل أموالك في مكان واحد", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Text("اختر القسم المطلوب للوصول إلى شاشته ومراجعة تفاصيله", color = Color(0xFFE7E3D8), fontSize = 12.sp)
                        }
                    }
                }
                groups.forEach { group ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(10.dp), color = CategoryPalette.goldSoft) {
                                    Icon(group.icon, null, Modifier.padding(9.dp).size(21.dp), tint = CategoryPalette.ink)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(group.title, color = CategoryPalette.ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    Text(group.subtitle, color = CategoryPalette.muted, fontSize = 11.sp)
                                }
                                Text("${group.items.size}", color = CategoryPalette.muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            group.items.chunked(2).forEach { rowItems ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    rowItems.forEach { item -> CategoryTileCard(item, Modifier.weight(1f)) { onCategoryClick(item.id) } }
                                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedButton(onClick = onAddNewCategory, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, CategoryPalette.gold)) {
                        Icon(Icons.Default.Add, null, tint = CategoryPalette.ink)
                        Spacer(Modifier.width(8.dp))
                        Text("إضافة قسم مالي", color = CategoryPalette.ink, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTileCard(item: CategoryTile, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = 132.dp), shape = RoundedCornerShape(16.dp), color = CategoryPalette.white, border = BorderStroke(1.dp, CategoryPalette.border), shadowElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = CategoryPalette.goldSoft) {
                Icon(item.icon, item.title, Modifier.padding(8.dp).size(20.dp), tint = CategoryPalette.ink)
            }
            Text(item.title, color = CategoryPalette.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.subtitle, color = CategoryPalette.muted, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("فتح القسم", color = CategoryPalette.gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
