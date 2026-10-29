package com.example.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.LoginViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.R

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onBiometricClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = viewModel()
) {
    val backAction = onBackClick
    val username = loginViewModel.username
    val password = loginViewModel.password
    val showPassword = loginViewModel.showPassword
    val rememberDevice = loginViewModel.rememberDevice
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) {
        var current: Context? = context
        var found: FragmentActivity? = null
        while (current != null) {
            if (current is FragmentActivity) {
                found = current
                break
            }
            current = (current as? ContextWrapper)?.baseContext
        }
        found
    }
    val notice = loginViewModel.notice
    val error = loginViewModel.errorMessage
    val isLoading = loginViewModel.isLoading
    val showRegisterDialog = loginViewModel.showRegisterDialog
    val showForgotPasswordDialog = loginViewModel.showForgotPasswordDialog
    var confirmPassword by remember { mutableStateOf("") }
    var googlePassword by remember { mutableStateOf("") }
    var confirmGooglePassword by remember { mutableStateOf("") }
    var resetEmail by remember(username) { mutableStateOf(username) }
    LaunchedEffect(notice, error) {
        (error ?: notice)?.let { android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show() }
        if (notice != null && !loginViewModel.showRegisterDialog && !loginViewModel.showForgotPasswordDialog) loginViewModel.clearNotice()
    }
    val lang = loginViewModel.currentLanguage
    val badgeText = when (lang) {
        "en" -> "Secure Encrypted System"
        "fr" -> "Système Chiffré Sécurisé"
        else -> "نظام مشفر آمن"
    }
    val subtitleText = when (lang) {
        "en" -> "Smart Executive Cost & Management System"
        "fr" -> "Système Intelligent de Gestion et Coûts Exécutifs"
        else -> "منظومة الإدارة والتكاليف التنفيذية الذكية"
    }
    val usernameLabel = when (lang) {
        "en" -> "Username or Email"
        "fr" -> "Nom d'utilisateur ou E-mail"
        else -> "اسم المستخدم أو البريد الإلكتروني"
    }
    val usernamePlaceholder = when (lang) {
        "en" -> "e.g. CEO@smartcost.pro or 29401"
        "fr" -> "ex: CEO@smartcost.pro ou 29401"
        else -> "مثال: CEO@smartcost.pro أو 29401"
    }
    val passwordLabel = when (lang) {
        "en" -> "Encrypted Password"
        "fr" -> "Mot de passe chiffré"
        else -> "كلمة المرور المشفرة"
    }
    val rememberText = when (lang) {
        "en" -> "Remember this secure device"
        "fr" -> "Se souvenir de cet appareil"
        else -> "تذكر هذا الجهاز الآمن"
    }
    val forgotText = when (lang) {
        "en" -> "Forgot Code?"
        "fr" -> "Code oublié ?"
        else -> "استعادة الرمز؟"
    }
    val loginButtonText = when (lang) {
        "en" -> "Login"
        "fr" -> "Connexion"
        else -> "تسجيل الدخول"
    }
    val verifyingText = when (lang) {
        "en" -> "Verifying..."
        "fr" -> "Vérification..."
        else -> "جارٍ التحقق..."
    }
    val quickLoginText = when (lang) {
        "en" -> "Or Quick Login"
        "fr" -> "Ou Connexion Rapide"
        else -> "أو تسجيل الدخول السريع"
    }
    val biometricTitle = when (lang) {
        "en" -> "Quick Login via Face ID / Biometrics"
        "fr" -> "Connexion Rapide par Face ID / Biométrie"
        else -> "الدخول السريع عبر Face ID / البصمة"
    }
    val biometricSub = when (lang) {
        "en" -> "Verified Biometric Check"
        "fr" -> "Vérification biométrique approuvée"
        else -> "التحقق البيومتري المعتمد"
    }
    val googleLoginText = when (lang) {
        "en" -> "Continue with Google"
        "fr" -> "Continuer avec Google"
        else -> "متابعة الدخول بحساب Google"
    }
    val noAccountText = when (lang) {
        "en" -> "Don't have an account?"
        "fr" -> "Vous n'avez pas de compte ?"
        else -> "ليس لديك حساب؟"
    }
    val registerNowText = when (lang) {
        "en" -> "Register now"
        "fr" -> "S'inscrire"
        else -> "سجل الآن"
    }
    val guestText = when (lang) {
        "en" -> "Login as Guest to browse system"
        "fr" -> "Connexion en tant qu'invité"
        else -> "الدخول كزائر لتصفح النظام"
    }
    val langDisplay = when (lang) {
        "en" -> "◎ EN"
        "fr" -> "◎ FR"
        else -> "◎ AR"
    }

    val fieldShape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFFF9F9FF))
            .windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { backAction() }, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = SlateLight)
            }
            Row(
                Modifier.background(FinancialGreenContainer, CircleShape)
                    .border(1.dp, EmeraldGreen.copy(alpha = .25f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(8.dp).background(EmeraldGreen, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(badgeText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
            }
            Surface(onClick = { loginViewModel.onLanguageClick() }, shape = CircleShape, color = GoldLight, border = BorderStroke(1.dp, GoldPrimary.copy(alpha = .4f))) {
                Text(langDisplay, Modifier.padding(horizontal = 11.dp, vertical = 6.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldDark)
            }
        }
        Image(
            painter = painterResource(id = R.drawable.ic_smart_cost_logo),
            contentDescription = "الشعار الرسمي لتطبيق سمارت كوست برو",
            modifier = Modifier.fillMaxWidth().height(140.dp),
            contentScale = ContentScale.Fit
        )
        Text(subtitleText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondaryDark, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Surface(shape = CircleShape, color = SurfaceWhite, border = BorderStroke(1.dp, BorderLight), shadowElevation = 2.dp) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, null, Modifier.size(14.dp), tint = GoldPrimary)
                Spacer(Modifier.width(6.dp))
                Text("eng.mamdouh saad", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateLight)
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 448.dp),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderLight.copy(alpha = .6f)),
            shadowElevation = 12.dp
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, Modifier.size(18.dp), tint = GoldDark)
                    Spacer(Modifier.width(6.dp))
                    Text(usernameLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                }
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = username, onValueChange = loginViewModel::onUsernameChange,
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text(usernamePlaceholder, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Person, null, Modifier.size(20.dp), tint = TextTertiaryDark) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = DeepSlate, fontSize = 14.sp),
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary, unfocusedBorderColor = BorderLight,
                        focusedContainerColor = SurfaceCard, unfocusedContainerColor = SurfaceCard
                    )
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, Modifier.size(18.dp), tint = GoldDark)
                    Spacer(Modifier.width(6.dp))
                    Text(passwordLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                }
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = password, onValueChange = loginViewModel::onPasswordChange,
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("••••••••••••", fontSize = 14.sp) },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(Icons.Default.Lock, null, Modifier.size(20.dp), tint = TextTertiaryDark) },
                    trailingIcon = {
                        IconButton(onClick = { loginViewModel.onPasswordVisibilityClick() }) {
                            Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                tint = TextSecondaryDark, modifier = Modifier.size(20.dp))
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = DeepSlate, fontSize = 14.sp),
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary, unfocusedBorderColor = BorderLight,
                        focusedContainerColor = SurfaceCard, unfocusedContainerColor = SurfaceCard
                    )
                )
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { loginViewModel.onRememberDeviceChange(!rememberDevice) }) {
                        Checkbox(checked = rememberDevice, onCheckedChange = loginViewModel::onRememberDeviceChange,
                            modifier = Modifier.size(36.dp),
                            colors = CheckboxDefaults.colors(checkedColor = GoldDark))
                        Text(rememberText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondaryDark)
                    }
                    Text(forgotText, Modifier.clickable(onClick = { loginViewModel.onForgotPasswordClick() }), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldDark)
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { activity?.let { loginViewModel.onLoginClick(it, onLoginClick) } ?: run { android.widget.Toast.makeText(context, "تعذر فتح نافذة المصادقة", android.widget.Toast.LENGTH_SHORT).show() } }, enabled = !isLoading, modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = fieldShape, colors = ButtonDefaults.buttonColors(containerColor = DeepSlate),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = GoldPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isLoading) verifyingText else loginButtonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowBack, null, Modifier.size(19.dp), tint = GoldPrimary)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(Modifier.weight(1f), color = BorderLight)
                    Text(quickLoginText, Modifier.padding(horizontal = 10.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextTertiaryDark)
                    HorizontalDivider(Modifier.weight(1f), color = BorderLight)
                }
                Spacer(Modifier.height(8.dp))
                Surface(
                    onClick = { activity?.let { loginViewModel.onBiometricClick(it, onLoginClick) } ?: run { android.widget.Toast.makeText(context, "تعذر فتح نافذة البصمة", android.widget.Toast.LENGTH_SHORT).show() } }, modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape, color = GoldLight.copy(alpha = .7f),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = .45f))
                ) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(32.dp).background(SurfaceWhite, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Shield, null, Modifier.size(19.dp), tint = GoldDark)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(biometricTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepSlate)
                            Text(biometricSub, fontSize = 11.sp, color = TextSecondaryDark)
                        }
                        Icon(Icons.Default.ArrowBack, null, Modifier.size(17.dp), tint = GoldDark)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { loginViewModel.onGoogleClick(context, onLoginClick) }, enabled = !isLoading, modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = fieldShape, border = BorderStroke(1.dp, BorderLight),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceCard)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "شعار Google",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(googleLoginText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(noAccountText, fontSize = 13.sp, color = TextSecondaryDark)
                    Spacer(Modifier.width(6.dp))
                    Text(registerNowText, Modifier.clickable(onClick = { loginViewModel.onRegisterClick() }), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldDark)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            onClick = { loginViewModel.onGuestClick(onGuestClick) },
            modifier = Modifier.fillMaxWidth().widthIn(max = 448.dp),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderLight),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(42.dp)
                        .background(GoldLight.copy(alpha = .35f), CircleShape)
                        .border(1.dp, GoldPrimary.copy(alpha = .45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_smart_cost_logo),
                        contentDescription = "شعار SMART COST PRO",
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    guestText,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark,
                    textAlign = TextAlign.Start
                )
                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp), tint = GoldDark)
            }
        }
        Spacer(Modifier.height(4.dp))
    }

    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) { loginViewModel.dismissDialogs(); confirmPassword = "" } },
            title = { Text(if (loginViewModel.awaitingEmailVerification) "تفعيل البريد الإلكتروني" else "إنشاء حساب جديد", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (loginViewModel.awaitingEmailVerification) {
                        Text("افتح رابط التفعيل المرسل إلى " + username + " ثم اضغط تحقق.")
                        TextButton(enabled = !isLoading, onClick = loginViewModel::resendVerification) { Text("إعادة إرسال رابط التفعيل") }
                    } else {
                        Text("سيتم إنشاء الحساب باستخدام البريد الإلكتروني وكلمة المرور.")
                        OutlinedTextField(value = username, onValueChange = loginViewModel::onUsernameChange,
                            label = { Text("البريد الإلكتروني") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(),
                            isError = username.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches(),
                            trailingIcon = { if (username.isNotBlank()) Icon(
                                if (android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = "صحة صيغة البريد",
                                tint = if (android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) Color(0xFF075E52) else MaterialTheme.colorScheme.error
                            ) })
                        OutlinedTextField(value = password, onValueChange = loginViewModel::onPasswordChange,
                            label = { Text("كلمة المرور (6 أحرف على الأقل)") }, singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it },
                            label = { Text("تأكيد كلمة المرور") }, singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(),
                            isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                            trailingIcon = { if (confirmPassword.isNotEmpty()) Icon(
                                if (confirmPassword == password) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = if (confirmPassword == password) "كلمتا المرور متطابقتان" else "كلمتا المرور غير متطابقتين",
                                tint = if (confirmPassword == password) Color(0xFF075E52) else MaterialTheme.colorScheme.error
                            ) })
                    }
                    if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
                    if (notice != null) Text(notice, color = Color(0xFF075E52))
                }
            },
            confirmButton = {
                TextButton(enabled = !isLoading, onClick = {
                    if (loginViewModel.awaitingEmailVerification) loginViewModel.checkEmailVerification(onLoginClick)
                    else activity?.let { loginViewModel.registerAccount(confirmPassword, it, onLoginClick) }
                }) { Text(if (isLoading) "جارٍ التحقق..." else if (loginViewModel.awaitingEmailVerification) "تحقق من التفعيل" else "إنشاء الحساب") }
            },
            dismissButton = { TextButton(onClick = { loginViewModel.dismissDialogs(); confirmPassword = "" }) { Text("إلغاء") } }
        )
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { loginViewModel.dismissDialogs() },
            title = { Text("استعادة كلمة المرور", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أدخل البريد المسجل لإرسال رابط استعادة من Firebase.")
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("البريد الإلكتروني") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(enabled = !isLoading, onClick = {
                    loginViewModel.onUsernameChange(resetEmail)
                    loginViewModel.sendPasswordReset()
                }) { Text(if (isLoading) "جارٍ الإرسال..." else "إرسال رابط الاستعادة") }
            },
            dismissButton = {
                TextButton(onClick = { loginViewModel.dismissDialogs() }) { Text("إلغاء") }
            }
        )
    }

    if (loginViewModel.showGooglePasswordDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("تعيين كلمة مرور للحساب", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("حساب Google دخل بنجاح. يمكنك إضافة كلمة مرور لتسجيل الدخول بالبريد الإلكتروني أيضًا، وسيظل الحساب نفسه دون إنشاء حساب جديد.")
                    OutlinedTextField(
                        value = googlePassword,
                        onValueChange = { googlePassword = it },
                        label = { Text("كلمة المرور (6 أحرف على الأقل)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmGooglePassword,
                        onValueChange = { confirmGooglePassword = it },
                        label = { Text("تأكيد كلمة المرور") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isLoading,
                    onClick = {
                        loginViewModel.setPasswordForGoogleAccount(googlePassword, confirmGooglePassword)
                        if (googlePassword.length >= 6 && googlePassword == confirmGooglePassword) {
                            googlePassword = ""
                            confirmGooglePassword = ""
                        }
                    }
                ) { Text(if (isLoading) "جارٍ الحفظ..." else "تعيين كلمة المرور") }
            },
        )
    }

}

@Preview(showBackground = true, locale = "ar")
@Composable
fun LoginScreenPreview() {
    SmartCostProTheme { LoginScreen() }
}
