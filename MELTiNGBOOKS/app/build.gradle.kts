import java.util.Properties

// local.properties에서 API 키를 가져오는 함수
fun getApiKeyFromLocalProperties(): String {

    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
    }
    return properties.getProperty("OPENAI_API_KEY") ?: ""
}

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.protobuf") version "0.9.1"
}

android {
    namespace = "com.example.meltingbooks"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.meltingbooks"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties에서 OpenAI API 키를 읽어 BuildConfig에 추가
        buildConfigField("String", "OPENAI_API_KEY", "\"${getApiKeyFromLocalProperties()}\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/DEPENDENCIES",
                "META-INF/versions/9/module-info.class"
            )
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.emoji2:emoji2:1.1.0")

    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage:20.2.1")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.cloud:google-cloud-speech:4.50.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation("com.google.protobuf:protobuf-javalite:3.25.5")

    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.8.0")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    //implementation ("androidx.emoji2:emoji2:1.4.0")
    implementation ("org.jsoup:jsoup:1.15.4")

    implementation ("com.google.android.flexbox:flexbox:3.0.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")

    //⭐ SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}

// Firebase 설정 적용
apply(plugin = "com.google.gms.google-services")