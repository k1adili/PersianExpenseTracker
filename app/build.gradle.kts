plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.rialtracker.expense"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rialtracker.expense"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }

    // امضای نسخه‌ی release فقط وقتی تنظیم می‌شود که مقادیر لازم از طریق -P پاس داده شده باشند
    // (این کار توسط .github/workflows/release.yml هنگام ساخت نسخه‌ی امضاشده انجام می‌شود).
    // اگر این مقادیر داده نشوند (مثلاً در workflow ساده‌ی build-apk.yml یا در بیلد لوکال)، خروجی release بدون امضا ساخته می‌شود.
    val hasSigningProps = project.hasProperty("rialtracker.storeFile")
    signingConfigs {
        if (hasSigningProps) {
            create("release") {
                storeFile = file(project.property("rialtracker.storeFile") as String)
                storePassword = project.property("rialtracker.storePassword") as String
                keyAlias = project.property("rialtracker.keyAlias") as String
                keyPassword = project.property("rialtracker.keyPassword") as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasSigningProps) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.multidex:multidex:2.0.1")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    // Room (local database)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // توجه: خروجی اکسل (.xlsx) با یک نویسنده‌ی سبک دست‌ساز (util/XlsxWriter.kt) تولید می‌شود،
    // نه با Apache POI. دلیل: POI روی اندروید به‌خاطر تعداد متدها و وابستگی xmlbeans معمولاً
    // باعث مشکل در بیلد و حجم بالای APK می‌شود؛ برای یک فایل ساده‌ی چند-شیتی نیازی به آن نیست.

    // WorkManager (for periodic/background tasks like auto-backup reminders)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Charts (native Compose canvas is used, no external chart lib needed)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
