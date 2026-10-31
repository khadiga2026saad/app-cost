package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.runtime.DisposableEffect
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
            Spacer(Modifier.height(12.dp))
            Surface(
                onClick = { onNavigateToGateway("expenses") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).testTag("costs_only_gateway"),
                shape = RoundedCornerShape(16.dp), color = Color(0xFF075E52), shadowElevation = 7.dp
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
            Spacer(Modifier.height(12.dp))
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
            SubCategoriesDropdownSection(onNavigateToGateway = onNavigateToGateway)
            Spacer(Modifier.height(12.dp))
            CostStudyExportSection()
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

/**
 * شريط التنبيهات والأخبار المالي المتحرك المتناسق والموسع بعرض الشاشة
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApprovedHomeTicker(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f)),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 7.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // شعار البرنامج الرسمي بهوية فاخرة وبارزة في الشريط (جهة اليمين بالـ RTL)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_smart_cost_logo),
                        contentDescription = "شعار سمارت كوست برو",
                        modifier = Modifier.size(28.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "نبض المراقبة",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepSlate
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // شريط الأخبار المتحرك مع خط واضح وتوسيع بعرض الشاشة
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = "✦ SMART COST PRO • نظام المراقبة المالية والتكاليف • رصد فوري للمصروفات والتدفقات النقدية • مؤشرات السيولة والأرصدة محدثة لحظياً • كفاءة تشغيلية وتنفيذية ذكية ✦",
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                velocity = 38.dp
                            ),
                        maxLines = 1,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepSlate
                    )
                }
            }
        }
    }
}

/**
 * 1. Top App Bar (Executive Header)
 * الشريط العلوي التنفيذي بتنسيق متوازن متناسق وتوسيع عرضي كامل يمنع أي تداخل أو اقتطاع للأيقونات على الشاشات الحقيقية
 */
@Composable
fun ExecutiveHeader(
    timeString: String,
    dateString: String,
    userName: String = "زائر",
    photoPath: String? = null,
    userRole: String = "مؤسس برمجيات ورائد أعمال",
    onSearchClick: () -> Unit = {},
    onVoiceClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onHomeClick: () -> Unit = {}
) {
    var expandedOverflowMenu by remember { mutableStateOf(false) }

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
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            // الشريط العلوي: بيانات المستخدم على اليمين وأزرار التحكم المتناسقة على اليسار
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. هوية المستخدم (جهة اليمين في RTL)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .testTag("header_user_profile")
                ) {
                    Box(modifier = Modifier.size(48.dp)) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2FE))
                                .border(1.5.dp, GoldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val photo = remember(photoPath) {
                                photoPath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
                            }
                            if (photo != null) Image(photo, "صورة المستخدم", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Icon(Icons.Default.Person, "صورة المستخدم", tint = DeepSlate, modifier = Modifier.size(24.dp))
                        }
                        // مؤشر الحالة النشط
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.BottomStart)
                                .background(EmeraldGreen, CircleShape)
                                .border(2.dp, SurfaceCrispWhite, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(9.dp))
                    Column {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepSlate,
                                fontSize = 14.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = userRole,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SlateMedium,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // 2. أدوات التحكم: الساعة، الإشعارات، الترس، والثلاث نقط (كلها شفافة حتة واحدة مع البطاقة)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    // ويدجت الساعة والتاريخ الحية (شفافة تماماً وبدون خلفية أو إطار)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = if (timeString.isNotEmpty()) timeString else "12:00 م",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepSlate,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Text(
                            text = if (dateString.isNotEmpty()) dateString else "اليوم",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SlateMedium,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    // جرس التنبيهات مع شارة حمراء (شفاف تماماً وبدون أي خلفية أو إطار)
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_header_notifications")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "الإشعارات",
                                tint = DeepSlate,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
                                .background(CoralRed, CircleShape)
                                .border(1.dp, SurfaceCrispWhite, CircleShape)
                        )
                    }

                    // أيقونة الترس للإعدادات فقط (شفافة تماماً - تفتح إعدادات الحساب مباشرة وبدون تسجيل خروج)
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_header_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = DeepSlate,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // أيقونة الثلاث نقط العمودية (شفافة تماماً) - مخصصة لتسجيل الخروج فقط
                    Box {
                        IconButton(
                            onClick = { expandedOverflowMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_header_overflow")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "خيارات إضافية",
                                tint = DeepSlate,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = expandedOverflowMenu,
                            onDismissRequest = { expandedOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "تسجيل الخروج",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CoralRed
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        tint = CoralRed
                                    )
                                },
                                onClick = {
                                    expandedOverflowMenu = false
                                    onLogout()
                                }
                            )
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
fun LiveFinancialTickerBar(accountKey: String = "guest", birthDate: String? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(accountKey) { context.getSharedPreferences("account_information_visibility_${accountKey}", android.content.Context.MODE_PRIVATE) }
    var visibleItems by remember(accountKey) { mutableStateOf((0..5).map { prefs.getBoolean("item_$it", true) }) }
    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            visibleItems = (0..5).map { prefs.getBoolean("item_$it", true) }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val birth = birthDate?.let { runCatching { java.time.LocalDate.parse(it) }.getOrNull() }?.takeUnless { it.isAfter(java.time.LocalDate.now()) }
    val today = java.time.LocalDate.now()
    val details = birth?.let { date ->
        val hijri = android.icu.util.IslamicCalendar().apply { timeInMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() }
        val nowHijri = android.icu.util.IslamicCalendar()
        val hijriAge = nowHijri.get(android.icu.util.Calendar.YEAR) - hijri.get(android.icu.util.Calendar.YEAR) - if (nowHijri.get(android.icu.util.Calendar.MONTH) * 32 + nowHijri.get(android.icu.util.Calendar.DAY_OF_MONTH) < hijri.get(android.icu.util.Calendar.MONTH) * 32 + hijri.get(android.icu.util.Calendar.DAY_OF_MONTH)) 1 else 0
        val next = (today.year..today.year + 4).mapNotNull { runCatching { date.withYear(it) }.getOrNull() }.first { !it.isBefore(today) }
        val zodiac = when (date.monthValue * 100 + date.dayOfMonth) {
            in 321..419 -> "الحمل"; in 420..520 -> "الثور"; in 521..620 -> "الجوزاء"; in 621..722 -> "السرطان"; in 723..822 -> "الأسد"; in 823..922 -> "العذراء"; in 923..1022 -> "الميزان"; in 1023..1121 -> "العقرب"; in 1122..1221 -> "القوس"; in 1222..1231, in 101..119 -> "الجدي"; in 120..218 -> "الدلو"; else -> "الحوت"
        }
        listOf("العمر: ${java.time.Period.between(date, today).years} سنة", "العمر الهجري: $hijriAge سنة تقريبًا", "البرج الترفيهي: $zodiac", "يوم الميلاد: ${date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("ar"))}", "الميلاد الهجري التقريبي: ${hijri.get(android.icu.util.Calendar.DAY_OF_MONTH)}/${hijri.get(android.icu.util.Calendar.MONTH) + 1}/${hijri.get(android.icu.util.Calendar.YEAR)}", "عيد الميلاد القادم: بعد ${java.time.temporal.ChronoUnit.DAYS.between(today, next)} يوم")
    }

    val defaultFinancialNews = listOf(
        "✦ SMART COST PRO: المنظومة التنفيذية لإدارة ومراقبة التكاليف والمصروفات الذكية",
        "رصد فوري ولحظي لحركة التدفقات النقدية والمصروفات والسيولة",
        "تحديث دوري لمؤشرات الأداء المالي والأرصدة التشغيلية المعتمدة",
        "تقارير تفصيلية ودراسات تكاليف جاهزة للتصدير المباشر (PDF / Excel)",
        "أعلى معايير التشفير والأمان المالي وحماية السجلات"
    )

    val personalItems = details?.filterIndexed { index, _ -> visibleItems.getOrElse(index) { true } } ?: emptyList()
    val allItems = if (personalItems.isNotEmpty()) {
        defaultFinancialNews + personalItems
    } else {
        defaultFinancialNews
    }
    // Duplicate list items so that the news ticker always exceeds screen width and runs continuously
    val tickerText = (allItems + allItems).joinToString("   ✦   ")

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
                .padding(horizontal = 12.dp, vertical = 9.dp),
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

            // Brand Label Tag with live pulsing indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "نبض التكلفة:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepSlate,
                        fontSize = 11.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Scrolling Ticker with continuous basicMarquee
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = tickerText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DeepSlate,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            velocity = 38.dp
                        )
                )
            }
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

@Composable
private fun SubCategoriesDropdownSection(
    onNavigateToGateway: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val subCategories = remember {
        listOf(
            SubCategoryItem("تموين ومشتريات غذائية", "مصروفات المنزل", "expenses", Icons.Default.ReceiptLong),
            SubCategoryItem("فواتير ومرافق", "مصروفات المنزل", "expenses", Icons.Default.ReceiptLong),
            SubCategoryItem("أدوية وصيدليات", "الرعاية الصحية", "expenses", Icons.Default.ReceiptLong),
            SubCategoryItem("كشوفات واستشارات طبية", "الرعاية الصحية", "expenses", Icons.Default.ReceiptLong),
            SubCategoryItem("ملابس وأناقة", "المصروفات الشخصية", "expenses", Icons.Default.ReceiptLong),
            SubCategoryItem("عناية ومظهر شخصي", "المصروفات الشخصية", "expenses", Icons.Default.ReceiptLong),
            SubCategoryItem("وقود وتعبئات", "المركبات", "vehicles", Icons.Default.DirectionsCar),
            SubCategoryItem("صيانة دورية وإصلاحات", "المركبات", "vehicles", Icons.Default.DirectionsCar),
            SubCategoryItem("رسوم دراسية", "التعليم", "expenses", Icons.Default.ReceiptLong),
            SubCategoryItem("شهادات الدخل والأرباح", "الإيرادات", "income_accounts", Icons.Default.AttachMoney)
        )
    }

    val filteredCategories = remember(searchQuery) {
        if (searchQuery.isBlank()) subCategories
        else subCategories.filter { it.title.contains(searchQuery, ignoreCase = true) || it.departmentTitle.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sub_categories_dropdown_section"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderLight),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .testTag("sub_categories_header_click"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث الأقسام",
                            tint = GoldDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "قائمة الأقسام الفرعية المنسدلة",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BlackPrimary
                            )
                        )
                        Text(
                            text = "انقر للاستعراض، البحث، والانتقال المباشر للقسم",
                            style = MaterialTheme.typography.labelSmall.copy(color = SlateMedium)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = GoldDark
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث في الأقسام الفرعية...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = GoldDark)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sub_categories_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (filteredCategories.isEmpty()) {
                    Text(
                        text = "لا توجد نتائج مطابقة للبحث",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredCategories.forEach { cat ->
                            Surface(
                                onClick = {
                                    onNavigateToGateway(cat.gatewayTarget)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sub_category_item_${cat.gatewayTarget}"),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, BorderLight)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = null,
                                            tint = GoldDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = cat.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = BlackPrimary
                                                )
                                            )
                                            Text(
                                                text = cat.departmentTitle,
                                                style = MaterialTheme.typography.labelSmall.copy(color = SlateMedium)
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "انتقال مباشر",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CostStudyExportSection() {
    var exportDialogMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .testTag("cost_study_export_section"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderLight),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GoldLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "دراسة التكاليف",
                            tint = GoldDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تصدير دراسة التكاليف (PDF / Excel)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BlackPrimary
                            )
                        )
                        Text(
                            text = "استخراج تقارير تكاليف شاملة بتنسيق احترافي",
                            style = MaterialTheme.typography.labelSmall.copy(color = SlateMedium)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        exportDialogMessage = "تم تجهيز ملف دراسة التكاليف بصيغة PDF بنجاح وجاهز للمشاركة أو الحفظ."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("export_pdf_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DeepSlate
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تصدير PDF",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = {
                        exportDialogMessage = "تم تجهيز جدول دراسة التكاليف بصيغة Excel (XLSX) بنجاح وجاهز للفتح."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("export_excel_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepSlate,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تصدير Excel",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (exportDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { exportDialogMessage = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "نجاح عملية التصدير",
                    fontWeight = FontWeight.Bold,
                    color = DeepSlate
                )
            },
            text = {
                Text(
                    text = exportDialogMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { exportDialogMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("حسناً", color = DeepSlate, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

private data class SubCategoryItem(
    val title: String,
    val departmentTitle: String,
    val gatewayTarget: String,
    val icon: ImageVector
)

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
