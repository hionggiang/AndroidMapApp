plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.bdsdcna"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.bdsdcna"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}
dependencies {
    // AndroidX & Giao diện cốt lõi (Đã giữ bản cao nhất 1.12.0)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.constraintlayout)
    implementation(libs.activity.ktx)
    implementation("androidx.cardview:cardview:1.0.0")

    // Cấu hình Firebase tập trung (Sử dụng BoM để đồng bộ phiên bản)
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
    implementation(libs.firebase.database)
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore")

    // Bản đồ Google Maps & Định vị vị trí
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Biểu đồ báo cáo thống kê
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Xử lý file Excel (Apache POI)
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // Lifecycle & Coroutines xử lý đa tiến trình / dữ liệu lớn nền background
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.cloudinary:cloudinary-android:2.5.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("me.relex:circleindicator:2.1.6")

    implementation ("com.google.firebase:firebase-auth:24.0.1")
    implementation ("com.google.firebase:firebase-database:22.0.0")

    // 1. Thư viện PhotoView: Giúp biến ImageView thông thường thành ảnh có thể zoom bằng 2 ngón tay
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    implementation ("com.google.android.material:material:1.12.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
}
