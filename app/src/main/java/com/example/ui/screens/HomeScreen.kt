package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.MoneyUtils
import com.example.ui.theme.BackgroundWhite
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.BlackSecondary
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CoralRed
import com.example.ui.theme.DeepSlate
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.FinancialGreenContainer
import com.example.ui.theme.FinancialRedContainer
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldGradientEnd
import com.example.ui.theme.GoldGradientStart
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMedium
import com.example.ui.theme.SmartCostProTheme
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCrispWhite
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * بيانات بوابات الوصول السريع الثمانية
 */
data class GatewayActionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String? = null,
    val isPrimary: Boolean = false
)

/**
 * الشاشة الرئيسية التنفيذية المتكاملة لـ SMART COST PRO
 * - Top App Bar تنفيذي (User Profile, Live Clock, Search, Voice, Settings)
 * - شريط التنبيهات والأخبار المالي المتحرك (basicMarquee)
 * - بطاقات المؤشرات المالية التنفيذية والـ KPI
 * - شبكة بوابات الوصول السريع الثمانية (8-Gateway Action Grid)
 * - زر العمليات السريعة FAB وشريط التنقل السفلي الفاخر (Bottom Navigation)
 */
/**
 * Approved home layout: one gateway to the costs/expenses section.
 * Other surfaces are read-only indicators, not navigation cards.
 * Financial values are sourced from FinanceViewModel; no Stitch demo figures.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: FinanceViewModel? = null,
    onNavigateToGateway: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) { now = Date(); delay(1000L) }
    }
    val time = remember(now) { SimpleDateFormat("hh:mm:ss a", Locale("ar")).format(now) }
    val date = remember(now) { SimpleDateFormat("d MMMM yyyy", Locale("ar")).format(now) }
    val summary = viewModel?.currentMonthSummary?.collectAsStateWithLifecycle()?.value
    val income = summary?.let { MoneyUtils.formatPiastres(it.totalRevenuesPiastres) } ?: "—"
    val expenses = summary?.let { MoneyUtils.formatPiastres(it.totalExpensesPiastres) } ?: "—"
    val balance = summary?.let { MoneyUtils.formatPiastres(it.currentLiquidBalancePiastres) } ?: "—"
    var showMenu by remember { mutableStateOf(false) }
    val ink = Color(0xFF0B1528)
    val pale = Color(0xFFF8FAFC)
    val cyan = Color(0xFF06B6D4)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = pale,
        bottomBar = {
            NavigationBar(containerColor = Color.White, modifier = Modifier.testTag("home_bottom_nav")) {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text("الرئيسية") })
                NavigationBarItem(selected = false, onClick = { onNavigateToGateway("expenses") }, icon = { Icon(Icons.Default.ReceiptLong, null) }, label = { Text("التكاليف") })
                NavigationBarItem(selected = false, onClick = { onNavigateToGateway("expenses") }, icon = { Icon(Icons.Default.PieChart, null) }, label = { Text("الملخص") })
                NavigationBarItem(selected = false, onClick = { showMenu = true }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("الحساب") })
            }
        }
    ) { insets ->
        Column(Modifier.fillMaxSize().padding(insets).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
            Column(Modifier.fillMaxWidth().background(pale).padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFE0F2FE)).border(2.dp, cyan, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = "الحساب", tint = ink)
                        }
                        Spacer(Modifier.width(7.dp))
                        Column {
                            Text("مرحباً بك", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = ink)
                            Text("لوحة الإدارة المالية", fontSize = 10.sp, color = cyan)
                        }
                    }
                    Icon(Icons.Default.Shield, contentDescription = "الحالة", tint = cyan, modifier = Modifier.size(18.dp))
                    Column(Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White).border(1.dp, BorderLight, RoundedCornerShape(12.dp)).padding(horizontal = 7.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(time, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ink)
                        Text(date, fontSize = 9.sp, color = SlateMedium)
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Settings, contentDescription = "الإعدادات", tint = ink)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("تسجيل الخروج") }, onClick = { showMenu = false; onLogout() })
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                ApprovedHomeTicker()
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(GoldPrimary, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text("نظام المراقبة المالية", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ink)
                Text("  •  FINANCIAL MONITORING", fontSize = 9.sp, color = SlateMedium)
            }
            Spacer(Modifier.height(12.dp))
            Surface(Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 3.dp) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Analytics, null, tint = GoldDark)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Cost Estimation", fontSize = 19.sp, fontWeight = FontWeight.Black, color = ink)
                            Text("مؤشرات تقدير ومتابعة التكاليف", fontSize = 11.sp, color = SlateMedium)
                        }
                        Text("ملخص مالي", color = EmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("تُحتسب المؤشرات من بيانات البرنامج عند توفرها؛ لا تُعرض نسب توقعية دون معادلات معتمدة.", fontSize = 10.sp, color = SlateMedium)
                }
            }
            Spacer(Modifier.height(14.dp))
            Surface(
                onClick = { onNavigateToGateway("expenses") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).testTag("costs_only_gateway"),
                shape = RoundedCornerShape(16.dp), color = ink, shadowElevation = 7.dp
            ) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(GoldDark), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color.White)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("دخول قسم التكاليف", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        Text("إدارة الميزانيات والمصروفات والتدفقات المالية", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                    }
                    Icon(Icons.Default.ArrowDownward, contentDescription = "فتح أفرع التكاليف", tint = GoldPrimary)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                HomeReadOnlyIndicator("الدخل الشهري", income, EmeraldGreen, Modifier.weight(1f))
                HomeReadOnlyIndicator("المصروفات الشهرية", expenses, CoralRed, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Surface(Modifier.fillMaxWidth().padding(horizontal = 14.dp), shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, BorderLight)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, null, tint = GoldDark)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("الرصيد الجاري", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ink)
                            Text("Current Balance & Liquidity", fontSize = 10.sp, color = SlateMedium)
                        }
                        Text("مؤشر مالي", fontSize = 10.sp, color = cyan)
                    }
                    Spacer(Modifier.height(13.dp))
                    Text(balance, fontSize = 24.sp, fontWeight = FontWeight.Black, color = ink)
                }
            }
            Spacer(Modifier.height(12.dp))
            Surface(Modifier.fillMaxWidth().padding(horizontal = 14.dp), shape = RoundedCornerShape(10.dp), color = Color.White, border = BorderStroke(1.dp, BorderLight)) {
                Text("التصدير المالي: يُفعّل بعد تنفيذ تقارير قسم التكاليف", Modifier.padding(11.dp), textAlign = TextAlign.Center, fontSize = 11.sp, color = SlateMedium)
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun HomeReadOnlyIndicator(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, BorderLight)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(accent, CircleShape))
                Spacer(Modifier.width(5.dp))
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepSlate)
            }
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = accent)
        }
    }
}

/** Logo is fixed on the RTL right; text scrolls physically left to right toward it. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ApprovedHomeTicker() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFE6F4FA), border = BorderStroke(1.dp, Color(0xFFBAE6FD))) {
            Row(Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_smart_cost_logo), contentDescription = "الشعار الرسمي", modifier = Modifier.size(38.dp), contentScale = ContentScale.Fit)
                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF0891B2), modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.width(8.dp))
        Surface(Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE9D5FF))) {
            // RTL marquee reverses the default physical direction: left to right.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    "✦ SMART COST PRO • نظام المراقبة المالية • تابع بيانات التكاليف من أفرع القسم • ",
                    modifier = Modifier.fillMaxWidth().padding(10.dp).basicMarquee(iterations = Int.MAX_VALUE, velocity = 35.dp),
                    maxLines = 1, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepSlate
                )
            }
        }
    }
}

/**
 * 1. Top App Bar (Executive Header)
 * مرتب بالتوالي من اليسار إلى اليمين:
 * 1. صورة المستخدم بأيقونة، وتحته اسمه ومهنته
 * 2. نفس الأيقونات السابقة (الساعة الحية، أيقونة البحث، أيقونة البحث الصوتي)
 * 3. علامة الترس (الإعدادات)
 * 4. علامة الهوم (الرئيسية)
 */
@Composable
fun ExecutiveHeader(
    timeString: String,
    dateString: String,
    userName: String = "م. ممدوح سعد",
    userRole: String = "مهندس ومستشار تنفيذي",
    onSearchClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    onHomeClick: () -> Unit = {}
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("executive_header_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCrispWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // ترتيب صارم من اليسار إلى اليمين (LTR Sequence)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. أقصى اليسار: صورة المستخدم بأيقونة، وتحته اسمه ومهنته
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .testTag("header_user_profile")
                            .widthIn(max = 120.dp)
                    ) {
                        // صورة المستخدم بأيقونة (حجم 46.dp / 44.dp)
                        Box(modifier = Modifier.size(46.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(DeepSlate, SlateMedium)
                                        )
                                    )
                                    .border(1.5.dp, GoldPrimary, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "صورة المستخدم",
                                    tint = GoldLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // شارة الاتصال والحالة النشطة
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(EmeraldGreen, CircleShape)
                                    .border(2.dp, SurfaceCrispWhite, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // تحته اسمه
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepSlate,
                                fontSize = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // ومهنته
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = userRole,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 2. بعد ذلك نفس الأيقونات + علامة الترس + شعار البرنامج (بنفس حجم ومستوى أيقونة المستخدم)
                    Row(
                        modifier = Modifier.height(46.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // شارة الساعة والتاريخ الحية
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(GoldLight.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 5.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (timeString.isNotEmpty()) timeString else "12:00 م",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepSlate,
                                    fontSize = 10.5.sp
                                )
                            )
                            Text(
                                text = if (dateString.isNotEmpty()) dateString else "اليوم",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryDark,
                                    fontSize = 8.sp
                                )
                            )
                        }

                        // أيقونة البحث
                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier
                                .size(34.dp)
                                .background(SurfaceCard, CircleShape)
                                .border(1.dp, BorderLight, CircleShape)
                                .testTag("btn_header_search")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = DeepSlate,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // أيقونة البحث الصوتي
                        IconButton(
                            onClick = onVoiceClick,
                            modifier = Modifier
                                .size(34.dp)
                                .background(GoldLight, CircleShape)
                                .border(1.dp, GoldPrimary.copy(alpha = 0.4f), CircleShape)
                                .testTag("btn_header_voice")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "بحث صوتي",
                                tint = GoldDark,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // علامة الترس (Settings)
                        Box {
                            IconButton(
                                onClick = { expandedMenu = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(SurfaceCard, CircleShape)
                                    .border(1.dp, BorderLight, CircleShape)
                                    .testTag("btn_header_settings")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "الإعدادات",
                                    tint = DeepSlate,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = expandedMenu,
                                onDismissRequest = { expandedMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("إعدادات النظام") },
                                    onClick = {
                                        expandedMenu = false
                                        onSettingsClick()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Settings, contentDescription = null, tint = DeepSlate)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("قفل مساحة العمل") },
                                    onClick = {
                                        expandedMenu = false
                                        onLogout()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = GoldDark)
                                    }
                                )
                            }
                        }

                        // شعار البرنامج (أيقونة الرئيسية) بحجم أكبر وأكثر بروزاً في الشريط العلوى
                        Box(modifier = Modifier.size(54.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(SurfaceCrispWhite, GoldLight.copy(alpha = 0.45f))
                                        )
                                    )
                                    .border(2.dp, GoldPrimary, RoundedCornerShape(16.dp))
                                    .clickable(onClick = onHomeClick)
                                    .testTag("btn_header_home"),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_smart_cost_logo),
                                    contentDescription = "شعار البرنامج - الرئيسية",
                                    modifier = Modifier
                                        .size(42.dp)
                                        .padding(1.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. Live Financial News & Ticker Bar (شريط التنبيهات والأخبار المالي المتحرك)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiveFinancialTickerBar() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ticker_bar_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GoldLight.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo Emblem (شعار البرنامج في رأس الشريط المتحرك)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_smart_cost_logo),
                    contentDescription = "شعار سمارت كوست برو",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Brand Label Tag
            Text(
                text = "نبض التكلفة:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepSlate,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Scrolling Ticker with basicMarquee
            Text(
                text = "💡 تنبيه الميزانية: تم استهلاك 65% من مخصصات التشغيل لشهر مارس   •   📈 مؤشر التوفير: ارتفاع معدل خفض التكاليف بنسبة +8.4%   •   🔔 فاتورة تشغيل مستحقة خلال 48 ساعة   •   💰 التدفق النقدي: تم استلام إيرادات جديدة بقيمة 45,000 ج.م",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DeepSlate,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        velocity = 45.dp
                    )
            )
        }
    }
}

/**
 * 3. Financial Summary / KPI Cards (بطاقات المؤشرات التنفيذية)
 */
@Composable
fun FinancialSummaryKpiSection(
    operationalBalance: String,
    totalIncome: String,
    totalExpenses: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Main Wallet Balance Card (Deep luxury dark slate with gold accents)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("kpi_main_balance_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DeepSlate),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background subtle gold decorative glow
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(GoldPrimary.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إجمالي الرصيد التشغيلي والسيولة",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = GoldLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        // Growth Badge (+12.5%)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(EmeraldGreen.copy(alpha = 0.2f))
                                .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+12.5% نمو",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = operationalBalance,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Sub-summary breakdown (Income vs Expenses split)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SlateMedium.copy(alpha = 0.7f))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Quick Income Metric
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(EmeraldGreen.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "إجمالي الإيرادات",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GoldLight)
                                )
                                Text(
                                    text = totalIncome,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(SlateLight)
                        )

                        // Quick Expenses Metric
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(CoralRed.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = CoralRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "إجمالي التكاليف",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GoldLight)
                                )
                                Text(
                                    text = totalExpenses,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. 8-Gateway Action Grid Item Card
 */
@Composable
fun GatewayCard(
    item: GatewayActionItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(138.dp)
            .clickable(onClick = onClick)
            .testTag("gateway_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPrimary) SurfaceCrispWhite else SurfaceCrispWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isPrimary) 3.dp else 1.5.dp),
        border = BorderStroke(
            1.dp,
            if (item.isPrimary) GoldPrimary.copy(alpha = 0.5f) else BorderLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Gold-tinted icon container
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (item.isPrimary) GoldLight else SurfaceCard
                        )
                        .border(
                            1.dp,
                            if (item.isPrimary) GoldPrimary.copy(alpha = 0.6f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (item.isPrimary) GoldDark else DeepSlate,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Optional Badge
                if (item.badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (item.isPrimary) GoldLight else SurfaceCard
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (item.isPrimary) GoldDark else DeepSlate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepSlate,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Search Dialog
 */
@Composable
fun SearchDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "البحث المالي الشامل",
                fontWeight = FontWeight.Bold,
                color = DeepSlate
            )
        },
        text = {
            Column {
                Text(
                    "ابحث عن أي فاتورة، مشروع، عميل، أو تدفق نقدي...",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("اكتب كلمة البحث...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = GoldDark)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DeepSlate)
            ) {
                Text("إغلاق")
            }
        }
    )
}

/**
 * Voice Command Simulation Dialog
 */
@Composable
fun VoiceSearchDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(GoldLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = GoldDark,
                    modifier = Modifier.size(34.dp)
                )
            }
        },
        title = {
            Text(
                "المساعد الصوتي الذكي",
                fontWeight = FontWeight.Bold,
                color = DeepSlate,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "جارٍ الاستماع لأمرك المالي...",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "أمثلة: «سجل وقود 500 جنيه»، «كم متبقي في ميزانية هذا الشهر؟»",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("تم", color = DeepSlate, fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * AI Financial Advisor Dialog
 */
@Composable
fun AiAdvisorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = GoldDark,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                "توصيات المستشار المالي الذكي",
                fontWeight = FontWeight.Bold,
                color = DeepSlate,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "• 💡 مراجعة نفقات الوقود: تم تسجيل زيادة بنسبة 11% هذا الشهر عن المعدل الطبيعي.",
                    style = MaterialTheme.typography.bodySmall.copy(color = DeepSlate)
                )
                Text(
                    "• 📈 فرصة توفير: تحويل 15,000 ج.م الفائضة لحساب استثمار أو شهادة بنكية بعائد دوري.",
                    style = MaterialTheme.typography.bodySmall.copy(color = EmeraldGreen)
                )
                Text(
                    "• 🔔 استحقاق قريب: قسط الجمعية لشهر أبريل يستحق بعد 4 أيام.",
                    style = MaterialTheme.typography.bodySmall.copy(color = CoralRed)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DeepSlate)
            ) {
                Text("تطبيق التوصيات")
            }
        }
    )
}

/**
 * Quick Cost Bottom Sheet (ModalBottomSheet)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCostBottomSheet(
    onDismiss: () -> Unit,
    onCostSaved: (amount: String, category: String, desc: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("المنزل") }

    val categories = listOf("المنزل", "الطبي", "الشخصي", "العمل", "التعليم", "المركبات")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCrispWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تسجيل تكلفة سريعة",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepSlate
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("المبلغ (بالجنيه المصري)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description Field
            OutlinedTextField(
                value = descriptionText,
                onValueChange = { descriptionText = it },
                label = { Text("بيان المصروف أو البند") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "التصنيف:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.take(3).forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) GoldPrimary else SurfaceCard)
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) DeepSlate else TextSecondaryDark
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (amountText.isNotBlank()) {
                        onCostSaved(amountText, selectedCategory, descriptionText)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = DeepSlate
                ),
                shape = RoundedCornerShape(14.dp),
                enabled = amountText.isNotBlank()
            ) {
                Text(
                    text = "حفظ التكلفة فوراً",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepSlate
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
fun HomeScreenPreview() {
    SmartCostProTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            HomeScreen(
                viewModel = null,
                onNavigateToGateway = {}
            )
        }
    }
}
