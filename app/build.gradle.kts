import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val mapboxAccessToken: String = localProps.getProperty("MAPBOX_ACCESS_TOKEN") ?: ""

android {
    namespace = "com.stignit.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.stignit.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-milestone1"
        // API_BASE_URL is defined per build type below — never hardcode it in
        // Kotlin. Must include the trailing slash (Retrofit base-URL contract).
    }

    buildTypes {
        debug {
            // Same deployed backend as release for now; this is the seam where a
            // local (10.0.2.2) or staging backend would be swapped in for dev.
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"https://stgv3-production.up.railway.app/\"",
            )
            buildConfigField("String", "MAPBOX_ACCESS_TOKEN", "\"$mapboxAccessToken\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"https://stgv3-production.up.railway.app/\"",
            )
            buildConfigField("String", "MAPBOX_ACCESS_TOKEN", "\"$mapboxAccessToken\"")
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Backend wiring (auth OTP + registration against stignit-api).
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Live location tracking (Mapbox map rendering, device GPS, /rt WebSocket).
    implementation("com.mapbox.maps:android:11.9.1")
    implementation("com.mapbox.extension:maps-compose:11.9.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("io.socket:socket.io-client:2.1.1") {
        exclude(group = "org.json", module = "json")
    }
}
