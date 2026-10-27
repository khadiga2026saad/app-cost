package com.example.domain.model

enum class Department(val id: String, val titleAr: String, val defaultCategories: List<String>) {
    HOME(
        "HOME",
        "مصروفات المنزل",
        listOf("تموين ومشتريات غذائية", "فواتير ومرافق", "صيانة وأجهزة", "إيجار سكني", "أثاث ومستلزمات منزلية", "طوارئ منزلية")
    ),
    MEDICAL(
        "MEDICAL",
        "الرعاية الصحية والطبية",
        listOf("أدوية وصيدليات", "كشوفات واستشارات", "تحاليل وأشعة", "عمليات وإجراءات", "أجهزة ومستلزمات طبية", "أسنان وبصريات")
    ),
    PERSONAL(
        "PERSONAL",
        "المصروفات الشخصية",
        listOf("ملابس وأناقة", "عناية ومظهر شخصي", "مقاهي ومطاعم", "ترفيه وهوايات", "اشتراكات شخصية", "مواصلات وتنقل", "مصروف جيب")
    ),
    WORK(
        "WORK",
        "مصروفات العمل والمشروعات",
        listOf("معدات ومكتب", "اشتراكات وتراخيص", "تنقلات وسفر عمل", "تسويق وإعلانات", "استشارات وخدمات مهنية", "أخرى")
    ),
    EDUCATION(
        "EDUCATION",
        "مصروفات التعليم والطلاب",
        listOf("رسوم دراسية", "كتب وأدوات مدرسية", "دروس ومجموعات تقوية", "أنشطة واشتراكات تعليمية", "أخرى")
    ),
    VEHICLES(
        "VEHICLES",
        "مصروفات المركبات",
        listOf("وقود وتعبئات", "صيانة دورية وإصلاحات", "زيوت وفلاتر", "حوادث وتلفيات", "تأمين وتراخيص", "أخرى")
    ),
    VEHICLE(
        "VEHICLE",
        "مصروفات المركبات",
        listOf("وقود وتعبئات", "صيانة دورية وإصلاحات", "زيوت وفلاتر", "حوادث وتلفيات", "تأمين وتراخيص", "أخرى")
    );

    companion object {
        fun fromId(id: String?): Department {
            if (id.equals("VEHICLE", ignoreCase = true)) return VEHICLES
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: HOME
        }
    }
}
