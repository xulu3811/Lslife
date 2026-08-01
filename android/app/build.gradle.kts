import java.util.Properties
import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val versionPropsFile = file("version.properties")
val versionProps = Properties()

if (!versionPropsFile.exists()) {
    versionProps["versionCode"] = "9"
    versionProps["versionName"] = "1.8"
    versionProps.store(versionPropsFile.writer(), "Auto-generated Version Properties")
} else {
    versionProps.load(versionPropsFile.reader())
}

val isReleaseTask = gradle.startParameter.taskNames.any { 
    it.contains("Release", ignoreCase = true) || it.contains("assemble", ignoreCase = true) 
}

if (isReleaseTask) {
    val curCode = (versionProps["versionCode"] as? String)?.toIntOrNull() ?: 9
    val curName = (versionProps["versionName"] as? String) ?: "1.8"
    val parts = curName.split(".")
    var major = parts.getOrNull(0)?.toIntOrNull() ?: 1
    var minor = parts.getOrNull(1)?.toIntOrNull() ?: 8

    minor += 1
    if (minor > 9) {
        major += 1
        minor = 0
    }
    val newCode = curCode + 1
    val newName = "$major.$minor"

    // Set the properties in memory for the current build
    versionProps["versionCode"] = newCode.toString()
    versionProps["versionName"] = newName
}

val appVersionCode = (versionProps["versionCode"] as String).toInt()
val appVersionName = versionProps["versionName"] as String

android {
    namespace = "com.lianshan.lslife"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lianshan.lslife"
        minSdk = 24
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // 默认对接生产域名; 本地后端请临时改为 http://10.0.2.2:4000/api/
            buildConfigField("String", "API_BASE_URL", "\"https://mentalhlp.site/api/\"")
            buildConfigField("String", "WS_BASE_URL", "\"wss://mentalhlp.site/ws\"")
        }
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "API_BASE_URL", "\"https://mentalhlp.site/api/\"")
            buildConfigField("String", "WS_BASE_URL", "\"wss://mentalhlp.site/ws\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.datastore.preferences)
    implementation(libs.coil.compose)

    // OSMdroid (免费开源地图替代方案)
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    
    // ZXing QR Code Scanner
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

afterEvaluate {
    val verName = android.defaultConfig.versionName ?: "1.9"



    val copyReleaseApk by tasks.registering(Copy::class) {
        mustRunAfter("assembleRelease")
        from(layout.buildDirectory.dir("outputs/apk/release"))
        into(rootProject.file("../releases"))
        include("**/*.apk")

        rename { _ ->
            "LsLife-v${verName}-release.apk"
        }
        includeEmptyDirs = false

        val propsFilePath = project.file("version.properties").absolutePath
        val nextName = verName
        val nextCode = android.defaultConfig.versionCode.toString()
        doLast {
            val file = File(propsFilePath)
            val props = Properties()
            if (file.exists()) {
                props.load(file.reader())
            }
            props.setProperty("versionCode", nextCode)
            props.setProperty("versionName", nextName)
            props.store(file.writer(), "Auto-incremented Version Properties")
        }
    }



    tasks.named("assembleRelease") {
        finalizedBy(copyReleaseApk)
    }
}
