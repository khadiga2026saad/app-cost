package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

/** Real Firebase Authentication actions for the login screen. */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("smart_cost_auth_prefs", Context.MODE_PRIVATE)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val savedRememberDevice = preferences.getBoolean("remember_device", true)

    var username by mutableStateOf(if (savedRememberDevice) preferences.getString("saved_user", "") ?: "" else "")
        private set
    var password by mutableStateOf("")
        private set
    var showPassword by mutableStateOf(false)
        private set
    var rememberDevice by mutableStateOf(savedRememberDevice)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var showRegisterDialog by mutableStateOf(false)
        private set
    var showForgotPasswordDialog by mutableStateOf(false)
        private set
    var showGooglePasswordDialog by mutableStateOf(false)
        private set
    private var pendingGoogleSuccess: (() -> Unit)? = null
    private var pendingGoogleActivity: FragmentActivity? = null

    fun onUsernameChange(value: String) {
        username = value.trim()
        errorMessage = null
        if (rememberDevice) preferences.edit().putString("saved_user", username).apply()
    }

    fun onPasswordChange(value: String) { password = value; errorMessage = null }
    fun onPasswordVisibilityClick() { showPassword = !showPassword }

    fun onRememberDeviceChange(value: Boolean) {
        rememberDevice = value
        preferences.edit().putBoolean("remember_device", value).apply {
            if (value) putString("saved_user", username) else remove("saved_user")
        }.apply()
    }

    var currentLanguage by mutableStateOf("ar")
        private set

    fun onLanguageClick() {
        currentLanguage = when (currentLanguage) {
            "ar" -> "en"
            "en" -> "fr"
            else -> "ar"
        }
        notice = when (currentLanguage) {
            "ar" -> "تم التبديل إلى اللغة العربية"
            "en" -> "Switched to English"
            else -> "Passé au Français"
        }
    }
    fun onForgotPasswordClick() { errorMessage = null; showForgotPasswordDialog = true }
    fun onRegisterClick() { errorMessage = null; showRegisterDialog = true }
    fun dismissDialogs() { showRegisterDialog = false; showForgotPasswordDialog = false; showGooglePasswordDialog = false }

    fun onBiometricClick(activity: FragmentActivity, onSuccess: () -> Unit) {
        if (auth.currentUser == null) {
            notice = "سجّل الدخول بحسابك أولًا، ثم فعّل الدخول بالبصمة على هذا الجهاز"
            return
        }
        val manager = BiometricManager.from(activity)
        val availability = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
            notice = when (availability) {
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    "أضف بصمة أو وسيلة تعرف حيوي من إعدادات أمان الهاتف أولًا"
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    "هذا الجهاز لا يدعم التحقق الحيوي"
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    "مستشعر البصمة غير متاح حاليًا؛ حاول مرة أخرى"
                else -> "تعذر استخدام البصمة حاليًا؛ تحقق من إعدادات أمان الهاتف"
            }
            return
        }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                        notice = "لم يتم التحقق بالبصمة: " + errString
                    }
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("الدخول الآمن إلى SMART COST PRO")
                .setSubtitle("تحقق بالبصمة أو وسيلة المصادقة الحيوية المسجلة")
                .setNegativeButtonText("إلغاء")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build()
        )
    }

    private fun offerBiometricSetup(activity: FragmentActivity?, onComplete: () -> Unit) {
        if (activity == null) {
            onComplete()
            return
        }
        val availability = BiometricManager.from(activity)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
            if (availability == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                notice = "يمكنك إضافة بصمة من إعدادات أمان الهاتف ثم تفعيلها عند تسجيل الدخول مرة أخرى"
            }
            onComplete()
            return
        }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    preferences.edit().putBoolean("biometric_enabled", true).apply()
                    notice = "تم تفعيل الدخول السريع بالبصمة على هذا الجهاز"
                    onComplete()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onComplete()
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("تفعيل الدخول السريع بالبصمة؟")
                .setSubtitle("يمكنك استخدام البصمة لفتح جلستك المحفوظة عند العودة للتطبيق")
                .setNegativeButtonText("ليس الآن")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build()
        )
    }

    fun onGoogleClick(context: Context, onSuccess: () -> Unit) {
        if (isLoading) return
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val googleOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()
                val result = CredentialManager.create(context).getCredential(context, request)
                val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful && auth.currentUser != null) {
                            val user = auth.currentUser!!
                            saveRememberedUser(user.email.orEmpty())
                            val hasPasswordProvider = user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
                            if (hasPasswordProvider) {
                                offerBiometricSetup(findFragmentActivity(context), onSuccess)
                            } else {
                                // Keep the same Google-backed Firebase account and add email/password as a linked provider.
                                pendingGoogleSuccess = onSuccess
                                pendingGoogleActivity = findFragmentActivity(context)
                                showGooglePasswordDialog = true
                                notice = "يمكنك تعيين كلمة مرور للدخول بالبريد الإلكتروني أيضًا"
                            }
                        } else {
                            errorMessage = task.exception?.localizedMessage ?: "تعذر تسجيل الدخول بحساب Google"
                        }
                    }
            } catch (exception: GetCredentialException) {
                isLoading = false
                errorMessage = when (exception) {
                    is androidx.credentials.exceptions.GetCredentialCancellationException -> "تم إلغاء تسجيل الدخول بحساب Google"
                    else -> exception.localizedMessage ?: "تعذر الحصول على بيانات حساب Google"
                }
            } catch (exception: Exception) {
                isLoading = false
                errorMessage = exception.localizedMessage ?: "حدث خطأ أثناء تسجيل الدخول بحساب Google"
            }
        }
    }

    fun setPasswordForGoogleAccount(newPassword: String, confirmPassword: String) {
        if (newPassword.length < 6) {
            errorMessage = "كلمة المرور يجب ألا تقل عن 6 أحرف"
            return
        }
        if (newPassword != confirmPassword) {
            errorMessage = "كلمتا المرور غير متطابقتين"
            return
        }
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email.isNullOrBlank()) {
            errorMessage = "انتهت جلسة Google؛ سجّل الدخول مرة أخرى"
            showGooglePasswordDialog = false
            return
        }
        if (isLoading) return
        isLoading = true
        errorMessage = null
        val credential = EmailAuthProvider.getCredential(email, newPassword)
        user.linkWithCredential(credential).addOnCompleteListener { task ->
            isLoading = false
            if (task.isSuccessful) {
                showGooglePasswordDialog = false
                password = ""
                notice = "تم ربط البريد الإلكتروني وكلمة المرور بنفس حساب Google"
                val callback = pendingGoogleSuccess
                val activity = pendingGoogleActivity
                pendingGoogleSuccess = null
                pendingGoogleActivity = null
                if (callback != null) offerBiometricSetup(activity, callback)
            } else {
                val raw = task.exception?.message.orEmpty().lowercase()
                errorMessage = when {
                    "email already in use" in raw || "credential already in use" in raw ->
                        "هذا البريد مرتبط بحساب آخر. لم يتم إنشاء حساب مكرر."
                    "provider already linked" in raw ->
                        "طريقة الدخول بالبريد وكلمة المرور مرتبطة بالفعل"
                    "weak password" in raw ->
                        "كلمة المرور ضعيفة؛ استخدم 6 أحرف على الأقل"
                    else -> task.exception?.localizedMessage ?: "تعذر ربط كلمة المرور بحساب Google"
                }
            }
        }
    }

    fun onLoginClick(activity: FragmentActivity, onSuccess: () -> Unit) {
        val email = username.trim()
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "من فضلك أدخل البريد الإلكتروني وكلمة المرور"
            return
        }
        if (!email.contains("@")) {
            errorMessage = "أدخل بريدًا إلكترونيًا صحيحًا"
            return
        }
        if (isLoading) return
        isLoading = true
        errorMessage = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful && auth.currentUser != null) {
                    saveRememberedUser(email)
                    password = ""
                    offerBiometricSetup(activity, onSuccess)
                } else {
                    errorMessage = authError(task.exception?.message, isRegistration = false)
                }
            }
    }

    fun registerAccount(confirmPassword: String, activity: FragmentActivity, onSuccess: () -> Unit) {
        val email = username.trim()
        if (email.isBlank() || !email.contains("@")) {
            errorMessage = "أدخل بريدًا إلكترونيًا صحيحًا"
            return
        }
        if (password.length < 6) {
            errorMessage = "كلمة المرور يجب ألا تقل عن 6 أحرف"
            return
        }
        if (password != confirmPassword) {
            errorMessage = "كلمتا المرور غير متطابقتين"
            return
        }
        if (isLoading) return
        isLoading = true
        errorMessage = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful && auth.currentUser != null) {
                    saveRememberedUser(email)
                    password = ""
                    showRegisterDialog = false
                    offerBiometricSetup(activity, onSuccess)
                } else {
                    errorMessage = authError(task.exception?.message, isRegistration = true)
                }
            }
    }

    fun sendPasswordReset() {
        val email = username.trim()
        if (email.isBlank() || !email.contains("@")) {
            errorMessage = "أدخل بريدك الإلكتروني المسجل أولًا"
            return
        }
        if (isLoading) return
        isLoading = true
        errorMessage = null
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            isLoading = false
            if (task.isSuccessful) {
                showForgotPasswordDialog = false
                notice = "لو البريد مسجل، هيوصلك رابط استعادة كلمة المرور"
            } else {
                errorMessage = authError(task.exception?.message, isRegistration = false)
            }
        }
    }

    fun onGuestClick(onEnter: () -> Unit) {
        // Do not let guest mode bypass the biometric lock on an authenticated session.
        if (auth.currentUser != null && preferences.getBoolean("biometric_enabled", false)) {
            notice = "افتح جلستك بالبصمة أولًا؛ وضع الزائر لا يتجاوز قفل الحساب"
            return
        }
        onEnter()
    }

    fun clearNotice() { notice = null }

    private fun findFragmentActivity(context: Context): FragmentActivity? {
        var current: Context? = context
        while (current != null) {
            if (current is FragmentActivity) return current
            current = (current as? ContextWrapper)?.baseContext
        }
        return null
    }

    private fun saveRememberedUser(email: String) {
        if (rememberDevice) preferences.edit().putString("saved_user", email).apply()
    }

    private fun authError(message: String?, isRegistration: Boolean): String {
        val code = message.orEmpty().lowercase()
        return when {
            "email address is badly formatted" in code -> "صيغة البريد الإلكتروني غير صحيحة"
            "no user record" in code || "user not found" in code -> "لا يوجد حساب مسجل بهذا البريد"
            "wrong password" in code || "invalid credential" in code || "password is invalid" in code ->
                "البريد الإلكتروني أو كلمة المرور غير صحيحة"
            "email address is already in use" in code -> "هذا البريد مسجل بالفعل"
            "weak password" in code -> "كلمة المرور ضعيفة؛ استخدم 6 أحرف على الأقل"
            "network" in code -> "تعذر الاتصال بالإنترنت؛ حاول مرة أخرى"
            else -> message ?: if (isRegistration) "تعذر إنشاء الحساب" else "تعذر تسجيل الدخول"
        }
    }
}
