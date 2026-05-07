import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

val appMetricaApiKey = (localProperties.getProperty("APPMETRICA_API_KEY") ?: "").trim()
val mapkitApiKey = (localProperties.getProperty("MAPKIT_API_KEY") ?: "").trim()
val yandexClientId = (localProperties.getProperty("YANDEX_CLIENT_ID") ?: "").trim()
val vkIdClientId = (localProperties.getProperty("VKID_CLIENT_ID") ?: "").trim()
val vkIdClientSecret = (localProperties.getProperty("VKID_CLIENT_SECRET") ?: "").trim()
val vkIdRedirectHost = (localProperties.getProperty("VKID_REDIRECT_HOST") ?: "vk.ru").trim()
val vkIdRedirectScheme = (localProperties.getProperty("VKID_REDIRECT_SCHEME") ?: "vk$vkIdClientId").trim()

android {
    namespace = "com.example.wish_list"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.wish_list"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "APPMETRICA_API_KEY", "\"$appMetricaApiKey\"")
        buildConfigField("String", "MAPKIT_API_KEY", "\"$mapkitApiKey\"")
        buildConfigField("String", "YANDEX_CLIENT_ID", "\"$yandexClientId\"")
        buildConfigField("String", "VKID_CLIENT_ID", "\"$vkIdClientId\"")
        buildConfigField("String", "VKID_CLIENT_SECRET", "\"$vkIdClientSecret\"")
        manifestPlaceholders["YANDEX_CLIENT_ID"] = yandexClientId
        manifestPlaceholders["VKIDClientID"] = vkIdClientId
        manifestPlaceholders["VKIDClientSecret"] = vkIdClientSecret
        manifestPlaceholders["VKIDRedirectHost"] = vkIdRedirectHost
        manifestPlaceholders["VKIDRedirectScheme"] = vkIdRedirectScheme
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":data"))
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(platform(libs.firebase.bom))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")
    implementation("io.appmetrica.analytics:analytics:8.1.0")
    implementation("com.yandex.android:maps.mobile:4.33.1-lite")
    implementation("com.yandex.android:authsdk:3.1.3")
    implementation("com.vk.id:vkid:2.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.firebase.config)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
}
