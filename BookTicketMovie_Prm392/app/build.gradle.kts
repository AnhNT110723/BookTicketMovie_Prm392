plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.bookingticketmove_prm392"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bookingticketmove_prm392"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true // Bật tính năng buildConfig
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "VNPAY_TMN_CODE", "\"your_vnpay_tmn_code\"")
            buildConfigField("String", "VNPAY_HASH_SECRET", "\"your_vnpay_hash_secret\"")
        }
        debug {
            buildConfigField("String", "VNPAY_TMN_CODE", "\"your_sandbox_tmn_code\"")
            buildConfigField("String", "VNPAY_HASH_SECRET", "\"your_sandbox_hash_secret\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packaging {
        resources {
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/LICENSE.md"
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
      // CardView for home screen
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // SwipeRefreshLayout for User Management
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Image loading library
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // JTDS SQL Server driver
    implementation(files("libs/jtds-1.3.1.jar"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Thêm Firebase Authentication
//    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
//    implementation("com.google.firebase:firebase-auth")
//    implementation("com.google.firebase:firebase-analytics")

    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")


    // Card View
    implementation ("androidx.cardview:cardview:1.0.0")

    // ZXing và Barcode Scanner
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.2")

    implementation ("com.jakewharton.threetenabp:threetenabp:1.4.0")
    implementation("com.google.zxing:core:3.4.0")

    //momo
//    implementation("com.github.momo-wallet:mobile-sdk:1.0.7") {
//        exclude(group = "com.android.support")
//    }
}
