plugins {
    alias(libs.plugins.android.application)
}

// Load local properties
val localProperties = java.util.Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

// Function to get property with fallback
fun getLocalProperty(key: String, fallback: String = ""): String {
    return localProperties.getProperty(key) ?: System.getenv(key) ?: fallback
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
        
        // Add database configuration to BuildConfig
        buildConfigField("String", "DB_HOST", "\"${getLocalProperty("DB_HOST", "localhost")}\"")
        buildConfigField("String", "DB_HOST_FALLBACK", "\"${getLocalProperty("DB_HOST_FALLBACK", "10.0.2.2")}\"")
        buildConfigField("String", "DB_PORT", "\"${getLocalProperty("DB_PORT", "1433")}\"")
        buildConfigField("String", "DB_NAME", "\"${getLocalProperty("DB_NAME", "MovieTicketBookingSystem")}\"")
        buildConfigField("String", "DB_USERNAME", "\"${getLocalProperty("DB_USERNAME", "sa")}\"")
        buildConfigField("String", "DB_PASSWORD", "\"${getLocalProperty("DB_PASSWORD", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Production database configuration (override for production)
            buildConfigField("String", "DB_HOST", "\"${getLocalProperty("PROD_DB_HOST", getLocalProperty("DB_HOST", "localhost"))}\"")
            buildConfigField("String", "DB_USERNAME", "\"${getLocalProperty("PROD_DB_USERNAME", getLocalProperty("DB_USERNAME", "sa"))}\"")
            buildConfigField("String", "DB_PASSWORD", "\"${getLocalProperty("PROD_DB_PASSWORD", getLocalProperty("DB_PASSWORD", ""))}\"")
        }
        debug {
            // Debug build uses local.properties values (already set in defaultConfig)
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    
    // Image loading library
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    
    // JTDS SQL Server driver
    implementation(files("libs/jtds-1.3.1.jar"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}