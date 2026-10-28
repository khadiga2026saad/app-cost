package com.example.ui.screens

import androidx.compose.foundation.layout.*
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
fun AccountSettingsScreen(modifier: Modifier = Modifier) {
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
                        0 -> {
                            Text("إعداد الحساب", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val user = signedInUser
                            Text("البريد الإلكتروني: " + (user?.email ?: "حساب زائر أو غير مسجل"))
                            Text("خيارات تعديل بيانات الحساب ستُضاف بعد تحديد الحقول المطلوبة.", style = MaterialTheme.typography.bodySmall)
                        }
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
