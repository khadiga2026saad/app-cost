package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.ProfileViewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.BitmapFactory
import android.app.DatePickerDialog
import java.util.Calendar
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
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
fun AccountSettingsScreen(profileViewModel: ProfileViewModel, modifier: Modifier = Modifier, onSwitchAccount: () -> Unit = {}, onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val auth = remember { FirebaseAuth.getInstance() }
    val context = LocalContext.current
    var signedInUser by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }
    val accountKey = signedInUser?.uid ?: "guest"
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val prefs = remember(accountKey) { context.getSharedPreferences("account_information_visibility_${accountKey}", Context.MODE_PRIVATE) }
    val infoLabels = listOf("العمر الميلادي", "العمر الهجري (تقريبي)", "البرج (ترفيهي)", "يوم الميلاد", "الميلاد الهجري (تقريبي)", "عيد الميلاد القادم")
    val enabled = remember(accountKey) { mutableStateListOf<Boolean>().apply { repeat(6) { add(prefs.getBoolean("item_$it", true)) } } }
    val authPrefs = remember(context) { context.getSharedPreferences("smart_cost_auth_prefs", Context.MODE_PRIVATE) }
    var biometricEnabled by remember(accountKey) { mutableStateOf(signedInUser != null && authPrefs.getBoolean("biometric_enabled", false) && authPrefs.getString("biometric_uid", null) == signedInUser?.uid) }
    var biometricNotice by remember { mutableStateOf<String?>(null) }
    fun setBiometricEnabled(enable: Boolean) {
        if (!enable) {
            authPrefs.edit().putBoolean("biometric_enabled", false).remove("biometric_uid").apply()
            biometricEnabled = false
            biometricNotice = "تم إيقاف الدخول بالبصمة دون حذف الحساب أو بياناته."
            return
        }
        val user = auth.currentUser
        if (user == null || user.isAnonymous || !user.isEmailVerified) {
            biometricEnabled = false
            biometricNotice = "سجّل الدخول بحساب موثّق أولًا لتفعيل البصمة."
            return
        }
        val activity = context.findSettingsFragmentActivity()
        if (activity == null) { biometricNotice = "تعذر فتح نافذة التحقق على هذا الجهاز."; return }
        val availability = BiometricManager.from(activity).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
            biometricNotice = when (availability) {
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "أضف بصمة أو وسيلة تعرف حيوي من إعدادات أمان الهاتف أولًا."
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "هذا الجهاز لا يدعم التحقق الحيوي."
                else -> "التحقق الحيوي غير متاح حاليًا."
            }
            return
        }
        val expectedUid = user.uid
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val current = auth.currentUser
                if (current != null && current.uid == expectedUid && !current.isAnonymous && current.isEmailVerified) {
                    authPrefs.edit().putBoolean("biometric_enabled", true).putString("biometric_uid", expectedUid).apply()
                    biometricEnabled = true
                    biometricNotice = "تم تفعيل الدخول بالبصمة لهذا الحساب."
                } else {
                    biometricEnabled = false
                    biometricNotice = "تغيّر الحساب؛ لم يتم تفعيل البصمة."
                }
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                biometricNotice = if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) "تم إلغاء تفعيل البصمة." else "تعذر التحقق: $errString"
            }
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                biometricNotice = "لم تتطابق البصمة؛ حاول مرة أخرى."
            }
        }).authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("تفعيل الدخول بالبصمة").setSubtitle("أكد هويتك لتفعيل الدخول السريع لهذا الحساب").setNegativeButtonText("إلغاء").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK).build())
    }
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
    Column(modifier.fillMaxSize().background(Color(0xFFF7F6F2))) {
        Text(
            "الإعدادات",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF171717),
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
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8D8A7)),
                shadowElevation = 3.dp
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (selectedTab) {
                        0 -> {
                            AccountProfileEditor(
                                user = signedInUser,
                                profileViewModel = profileViewModel,
                                onSwitchAccount = onSwitchAccount,
                                onLogout = onLogout
                            )
                            HorizontalDivider()
                            Text("معلوماتي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val date = profile?.birthDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }?.takeUnless { it.isAfter(LocalDate.now()) }
                            if (date == null) Text("أضف تاريخ ميلادك لتظهر معلوماتك التلقائية.", color = Color.Gray)
                            val values = if (date == null) List(6) { "—" } else {
                                val today = LocalDate.now()
                                val next = (today.year..today.year + 4).map { year -> date.withYear(year) }.first { !it.isBefore(today) }
                                val birthHijri = android.icu.util.IslamicCalendar().apply { timeInMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() }
                                val nowHijri = android.icu.util.IslamicCalendar()
                                val hijriYears = nowHijri.get(android.icu.util.Calendar.YEAR) - birthHijri.get(android.icu.util.Calendar.YEAR) - if (nowHijri.get(android.icu.util.Calendar.MONTH) * 32 + nowHijri.get(android.icu.util.Calendar.DAY_OF_MONTH) < birthHijri.get(android.icu.util.Calendar.MONTH) * 32 + birthHijri.get(android.icu.util.Calendar.DAY_OF_MONTH)) 1 else 0
                                listOf(
                                    "${Period.between(date, today).years} سنة",
                                    "$hijriYears سنة تقريبًا", zodiacLabel(date),
                                    date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale("ar")),
                                    "${birthHijri.get(android.icu.util.Calendar.DAY_OF_MONTH)}/${birthHijri.get(android.icu.util.Calendar.MONTH) + 1}/${birthHijri.get(android.icu.util.Calendar.YEAR)} هـ (تقريبي)",
                                    "$next — بعد ${ChronoUnit.DAYS.between(today, next)} يوم"
                                )
                            }
                            infoLabels.forEachIndexed { index, label ->
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(label, color = Color(0xFF766027), style = MaterialTheme.typography.labelMedium)
                                            Text(values[index], color = Color(0xFF171717), fontWeight = FontWeight.SemiBold)
                                        }
                                        Switch(
                                            checked = enabled[index],
                                            onCheckedChange = { value ->
                                                enabled[index] = value
                                                prefs.edit().putBoolean("item_$index", value).apply()
                                            },
                                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFD4AF37))
                                        )
                                    }
                                    if (index != infoLabels.lastIndex) HorizontalDivider(color = Color(0xFFF0E9D8))
                                }
                        }
                        1 -> {
                            Text("إعداد شريط الأخبار", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("التحكم في المعلومات التي تظهر بالشريط؛ جميع الخيارات تظل متاحة هنا حتى عند إخفائها.")
                            InformationVisibilityControls(infoLabels, enabled) { index, value ->
                                enabled[index] = value
                                prefs.edit().putBoolean("item_$index", value).apply()
                            }
                        }
                        2 -> {
                            Text("إعداد البرنامج", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("إعدادات البرنامج العامة", style = MaterialTheme.typography.bodyMedium)
                            HorizontalDivider()
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text("تفعيل الدخول بالبصمة", fontWeight = FontWeight.SemiBold, color = Color(0xFF171717))
                                    Text("فعّلها لاستخدام البصمة عند فتح التطبيق", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Switch(checked = biometricEnabled, onCheckedChange = { setBiometricEnabled(it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFD4AF37)))
                            }
                            biometricNotice?.let { Text(it, color = if (it.startsWith("تم")) Color(0xFF15803D) else Color(0xFFB45309), style = MaterialTheme.typography.bodySmall) }
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
private fun AccountProfileEditor(user: FirebaseUser?, profileViewModel: ProfileViewModel, onSwitchAccount: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val loading by profileViewModel.loading.collectAsStateWithLifecycle()
    val error by profileViewModel.error.collectAsStateWithLifecycle()
    val accountKey = user?.uid ?: "guest"
    var fullName by remember(accountKey) { mutableStateOf("") }
    var nickname by remember(accountKey) { mutableStateOf("") }
    var phone by remember(accountKey) { mutableStateOf("") }
    var birthDate by remember(accountKey) { mutableStateOf("") }
    var pendingPhoto by remember(accountKey) { mutableStateOf<android.net.Uri?>(null) }
    var notice by remember(accountKey) { mutableStateOf("") }
    var saving by remember(accountKey) { mutableStateOf(false) }
    var confirmAction by remember(accountKey) { mutableStateOf<String?>(null) }
    LaunchedEffect(profile, accountKey) {
        profile?.takeIf { it.accountKey == accountKey }?.let {
            fullName = it.fullName; nickname = it.nickname; phone = it.phone.orEmpty(); birthDate = it.birthDate.orEmpty()
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { pendingPhoto = uri; notice = "" }
    }
    val stored = profile?.takeIf { it.accountKey == accountKey }
    val dateValid = birthDate.isBlank() || try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(birthDate)
        parsed != null && parsed.time <= System.currentTimeMillis()
    } catch (_: Exception) { false }
    val phoneValid = phone.isBlank() || (phone.all { it.isDigit() || it == '+' || it == ' ' || it == '-' } && phone.count { it.isDigit() } in 7..15)
    val changed = stored != null && (fullName.trim() != stored.fullName || nickname.trim() != stored.nickname ||
        phone.trim() != stored.phone.orEmpty() || birthDate != stored.birthDate.orEmpty() || pendingPhoto != null)
    Text("الملف الشخصي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF9B7921))
    if (loading) { CircularProgressIndicator(); return }
    if (stored == null) { Text("تعذر تحميل بيانات الحساب. يرجى إعادة فتح الشاشة."); return }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val bitmap = remember(pendingPhoto, stored.photoPath) {
                try {
                    pendingPhoto?.let { uri -> context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) }
                        ?: stored.photoPath?.let { BitmapFactory.decodeFile(it) }
                } catch (_: Exception) { null }
            }?.asImageBitmap()
            Box(Modifier.size(88.dp).clip(CircleShape).background(Color(0xFFF6E8BC)).clickable { picker.launch("image/*") },
                contentAlignment = Alignment.Center) {
                if (bitmap != null) Image(bitmap, "صورة المستخدم", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Icon(Icons.Default.Person, "صورة المستخدم", Modifier.size(46.dp), tint = Color(0xFF9B7921))
            }
            TextButton(onClick = { picker.launch("image/*") }) { Text("تغيير صورة المستخدم") }
            if (pendingPhoto != null) Text("الصورة للمعاينة فقط حتى الحفظ", style = MaterialTheme.typography.bodySmall)
        }
    }
    OutlinedTextField(fullName, { fullName = it; notice = "" }, label = { Text("الاسم الكامل") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(nickname, { nickname = it; notice = "" }, label = { Text("اسم العرض") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(phone, { phone = it; notice = "" }, label = { Text("رقم الهاتف (اختياري)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), isError = !phoneValid, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(birthDate, {}, readOnly = true, label = { Text("تاريخ الميلاد (اختياري)") },
        modifier = Modifier.fillMaxWidth(), singleLine = true, isError = !dateValid)
    OutlinedButton(onClick = {
        val calendar = Calendar.getInstance()
        if (birthDate.isNotBlank()) try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(birthDate)?.let { calendar.time = it }
        } catch (_: Exception) {}
        DatePickerDialog(context, { _, y, m, d ->
            birthDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            .apply { datePicker.maxDate = System.currentTimeMillis(); show() }
    }, modifier = Modifier.fillMaxWidth()) { Text("اختيار تاريخ الميلاد من التقويم") }
    if (birthDate.isNotBlank()) TextButton(onClick = { birthDate = "" }) { Text("مسح تاريخ الميلاد") }
    Text("البريد الإلكتروني: " + (user?.email ?: "حساب زائر"))
    Button(enabled = changed && dateValid && phoneValid && !saving, onClick = {
        saving = true
        val previousPath = stored.photoPath
        var newFile: File? = null
        try {
            val path = pendingPhoto?.let { uri ->
                File(context.filesDir, "profile_${accountKey.hashCode()}_${System.nanoTime()}.jpg").also { file ->
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    } ?: error("Cannot read image")
                    newFile = file
                }.absolutePath
            } ?: previousPath
            profileViewModel.save(stored.copy(fullName = fullName.trim(), nickname = nickname.trim(),
                phone = phone.trim().ifBlank { null }, birthDate = birthDate.ifBlank { null }, photoPath = path)) { success ->
                saving = false
                if (success) {
                    pendingPhoto = null
                    notice = "تم حفظ البيانات بنجاح"
                    if (newFile != null && previousPath != null && previousPath != path) File(previousPath).delete()
                } else {
                    newFile?.delete()
                    notice = "تعذر حفظ البيانات؛ لم يتم اعتماد الصورة الجديدة"
                }
            }
        } catch (_: Exception) {
            newFile?.delete(); saving = false; notice = "تعذر قراءة الصورة المختارة"
        }
    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))) {
        Text(if (saving) "جارٍ الحفظ..." else "حفظ التغييرات", color = Color.Black)
    }
    if (error != null) Text("حدث خطأ أثناء تحميل أو حفظ البيانات", color = MaterialTheme.colorScheme.error)
    if (notice.isNotBlank()) Text(notice, style = MaterialTheme.typography.bodySmall)
    HorizontalDivider()
    OutlinedButton(onClick = { confirmAction = "switch" }, modifier = Modifier.fillMaxWidth()) { Text("تبديل الحساب") }
    TextButton(onClick = { confirmAction = "logout" }, modifier = Modifier.fillMaxWidth()) { Text("تسجيل الخروج") }
    if (confirmAction != null) {
        val switching = confirmAction == "switch"
        AlertDialog(onDismissRequest = { confirmAction = null },
            title = { Text(if (switching) "تبديل الحساب" else "تسجيل الخروج") },
            text = { Text("هل تريد المتابعة؟") },
            confirmButton = { TextButton(onClick = {
                confirmAction = null
                if (switching) onSwitchAccount() else onLogout()
            }) { Text("تأكيد") } },
            dismissButton = { TextButton(onClick = { confirmAction = null }) { Text("إلغاء") } })
    }
}

@Composable
private fun InformationVisibilityControls(labels: List<String>, enabled: List<Boolean>, onChange: (Int, Boolean) -> Unit) {
    labels.forEachIndexed { index, label ->
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, modifier = Modifier.weight(1f), color = Color(0xFF171717))
            Switch(checked = enabled[index], onCheckedChange = { onChange(index, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFD4AF37)))
        }
        if (index != labels.lastIndex) HorizontalDivider(color = Color(0xFFF0E9D8))
    }
}

private fun zodiacLabel(date: LocalDate): String {
    val day = date.monthValue * 100 + date.dayOfMonth
    return when (day) {
        in 321..419 -> "الحمل"; in 420..520 -> "الثور"; in 521..620 -> "الجوزاء"
        in 621..722 -> "السرطان"; in 723..822 -> "الأسد"; in 823..922 -> "العذراء"
        in 923..1022 -> "الميزان"; in 1023..1121 -> "العقرب"; in 1122..1221 -> "القوس"
        in 1222..1231, in 101..119 -> "الجدي"; in 120..218 -> "الدلو"
        in 219..320 -> "الحوت"; else -> "غير محدد"
    } + " (ترفيهي)"
}

private fun Context.findSettingsFragmentActivity(): FragmentActivity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return current as? FragmentActivity
}
