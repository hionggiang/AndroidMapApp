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
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.firebase:firebase-storage:21.0.1")
    implementation("com.google.firebase:firebase-firestore:25.1.0")
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("org.apache.poi:poi-ooxml-lite:5.2.5")
    implementation("org.apache.xmlbeans:xmlbeans:5.2.0")
    implementation("commons-io:commons-io:2.15.1")
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("com.google.firebase:firebase-database:21.0.0")

}
