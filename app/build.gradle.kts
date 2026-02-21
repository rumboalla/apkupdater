import java.util.Properties
import java.io.FileInputStream
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.apkupdater"
    compileSdk = 35

    val buildNumber = System.getenv("BUILD_NUMBER").orEmpty()
    defaultConfig {
        applicationId = "com.apkupdater" + System.getenv("BUILD_TAG").orEmpty()
        minSdk = 21
        targetSdk = 35
        versionCode = 52
        versionName = if (buildNumber.isEmpty()) "4.0" else "0.0.$buildNumber"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        val properties = Properties()
        if (file("../local.properties").exists()) {
            FileInputStream(file("../local.properties")).use { properties.load(it) }
        }

        val apkMirrorToken = properties.getProperty("apkmirror.token") ?: System.getenv("APKMIRROR_TOKEN") ?: ""
        buildConfigField("String", "APKMIRROR_TOKEN", "\"$apkMirrorToken\"")
    }

    signingConfigs {
        create("release") {
            try {
                val props = Properties()
                props.load(FileInputStream(file("../local.properties")))
                storeFile = file(props.getProperty("keystore.file"))
                storePassword = props.getProperty("keystore.password")
                keyAlias = props.getProperty("keystore.keyalias")
                keyPassword = props.getProperty("keystore.keypassword")
            } catch (ignored: Exception) {
                val config = signingConfigs.getByName("debug")
                storeFile = config.storeFile
                storePassword = config.storePassword
                keyAlias = config.keyAlias
                keyPassword = config.keyPassword
                println("Signing config not found. Using debug settings.")
            }
            enableV3Signing = true
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    applicationVariants.configureEach {
        outputs.configureEach {
            val variant = (this as BaseVariantOutputImpl)
            variant.outputFileName = defaultConfig.applicationId + "-" + buildType.name + ".apk"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        warning.addAll(arrayOf("ExtraTranslation", "MissingTranslation", "MissingQuantity"))
    }
}

// Disable all lint tasks
tasks.configureEach {
    if (name.contains("lint")) {
        enabled = false
    }
}

dependencies {

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kryptoprefs)
    implementation(libs.libsu.core)
    implementation(libs.gplayapi)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.retrofit)
    implementation(libs.coil.compose)
    implementation(libs.versioncompare)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.jsoup)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)

}
