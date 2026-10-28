package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.BitmapFactory
import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun AccountSettingsScreen(modifier: Modifier = Modifier, onSwitchAccount: () -> Unit = {}, onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val auth = remember { FirebaseAuth.getInstance() }
    var signedInUser by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { signedInUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }
    val isOwner = signedInUser?.let { user ->
        user.isEmailVerified && user.email?.equals("Eng.mamdouh2009@gmail.com", ignoreCase = true) == true
    } == true
    val tabs = if (isOwner) listOf("إعداد الحساب", "إعداد شريط الأخبار", "إعداد البرنامج", "إعدادات المالك")
        else listOf("إعداد الحساب", "إعداد شريط الأخبار", "إعداد البرنامج")
    LaunchedEffect(isOwner) { if (!isOwner && selectedTab == 3) selectedTab = 0 }
    Column(modifier.fillMaxSize()) {
        Text(
            "الإعدادات",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 8.dp,
            containerColor = Color.White,
            contentColor = Color(0xFF9B7921)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 2.dp
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (selectedTab) {
                        0 -> AccountProfileEditor(
                            user = signedInUser,
                            onSwitchAccount = onSwitchAccount,
                            onLogout = onLogout
                        )
                        1 -> {
                            Text("إعداد شريط الأخبار", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("مساحة تخصيص شريط الأخبار الثابت. الشريط الحالي لم يتم تغييره.", style = MaterialTheme.typography.bodyMedium)
                        }
                        2 -> {
                            Text("إعداد البرنامج", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("مساحة إعدادات البرنامج العامة. لم يتم تغيير أي تفضيلات حالية.", style = MaterialTheme.typography.bodyMedium)
                        }
                        3 -> if (isOwner) {
                            Text("إعدادات المالك", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("مساحة مخصصة لحساب المالك الموثّق فقط.", style = MaterialTheme.typography.bodyMedium)
                            Text("لم تُفعّل أي صلاحيات إدارية أو عمليات حساسة بعد.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountProfileEditor(
    user: FirebaseUser?,
    onSwitchAccount: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val accountKey = user?.uid ?: "guest"
    val prefs = remember(accountKey) {
        context.getSharedPreferences("profile_" + accountKey, Context.MODE_PRIVATE)
    }
    var fullName by remember(accountKey) { mutableStateOf(prefs.getString("full_name", user?.displayName.orEmpty()).orEmpty()) }
    var nickname by remember(accountKey) { mutableStateOf(prefs.getString("nickname", "").orEmpty()) }
    var birthDate by remember(accountKey) { mutableStateOf(prefs.getString("birth_date", "").orEmpty()) }
    var photoVersion by remember(accountKey) { mutableIntStateOf(0) }
    var notice by remember(accountKey) { mutableStateOf("") }
    var confirmAction by remember { mutableStateOf<String?>(null) }
    val imageFile = remember(accountKey) { File(context.filesDir, "profile_" + accountKey.hashCode() + ".jpg") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    imageFile.outputStream().use { output -> input.copyTo(output) }
                } ?: error("Cannot open selected image")
                photoVersion++
                notice = "تم تحديث الصورة على هذا الجهاز"
            } catch (e: Exception) {
                notice = "تعذر حفظ الصورة. حاول اختيار صورة أخرى."
            }
        }
    }
    Text("الملف الشخصي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val bitmap = remember(photoVersion, accountKey) {
                if (imageFile.exists()) BitmapFactory.decodeFile(imageFile.absolutePath)?.asImageBitmap() else null
            }
            Box(
                Modifier.size(88.dp).clip(CircleShape).background(Color(0xFFF6E8BC))
                    .clickable { picker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) Image(bitmap, "صورة المستخدم", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Icon(Icons.Default.Person, "صورة المستخدم", Modifier.size(46.dp), tint = Color(0xFF9B7921))
            }
            TextButton(onClick = { picker.launch("image/*") }) { Text("تغيير صورة المستخدم") }
        }
    }
    OutlinedTextField(fullName, { fullName = it }, label = { Text("الاسم الكامل") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(nickname, { nickname = it }, label = { Text("اسم الشهرة") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(birthDate, { birthDate = it }, label = { Text("تاريخ الميلاد (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Text("البريد الإلكتروني: " + (user?.email ?: "حساب زائر"), style = MaterialTheme.typography.bodyMedium)
    Text("نوع الحساب: " + if (user == null) "زائر" else "حساب مسجل", style = MaterialTheme.typography.bodySmall)
    Button(
        onClick = {
            val validDate = birthDate.isBlank() || run {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
                try { format.parse(birthDate)?.let { it.time <= System.currentTimeMillis() } == true } catch (_: Exception) { false }
            }
            if (!validDate) notice = "أدخل تاريخ ميلاد صحيحًا بصيغة YYYY-MM-DD"
            else {
                prefs.edit().putString("full_name", fullName.trim())
                    .putString("nickname", nickname.trim())
                    .putString("birth_date", birthDate.trim()).apply()
                notice = "تم حفظ بيانات الملف الشخصي على هذا الجهاز"
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF075E52))
    ) { Text("حفظ التغييرات") }
    if (notice.isNotBlank()) Text(notice, style = MaterialTheme.typography.bodySmall, color = Color(0xFF075E52))
    HorizontalDivider()
    OutlinedButton(onClick = { confirmAction = "switch" }, modifier = Modifier.fillMaxWidth()) { Text("تبديل الحساب") }
    TextButton(onClick = { confirmAction = "logout" }, modifier = Modifier.fillMaxWidth()) { Text("تسجيل الخروج") }
    if (confirmAction != null) {
        val switching = confirmAction == "switch"
        AlertDialog(
            onDismissRequest = { confirmAction = null },
            title = { Text(if (switching) "تبديل الحساب" else "تسجيل الخروج") },
            text = { Text(if (switching) "هل تريد العودة لشاشة الدخول لاستخدام حساب آخر؟" else "هل تريد تسجيل الخروج من الحساب الحالي؟") },
            confirmButton = {
                TextButton(onClick = {
                    confirmAction = null
                    if (switching) onSwitchAccount() else onLogout()
                }) { Text("تأكيد") }
            },
            dismissButton = { TextButton(onClick = { confirmAction = null }) { Text("إلغاء") } }
        )
    }
}
