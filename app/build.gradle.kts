plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.unip.cc7p33.memorizeflashcardapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.unip.cc7p33.memorizeflashcardapp"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)// Use a versão mais recente

    // Room
    val room_version = "2.7.2"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // Para testes de unidade
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.8.0") // Mockito para simular objetos
    testImplementation("org.robolectric:robolectric:4.9") // Robolectric para testes que precisam de Android framework (como Context)

    // Para testes do Room
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    // Testes de Instrumentação
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Firebase Emulator Test Helper (dependência crucial para testes locais)
    androidTestImplementation("com.google.firebase:firebase-auth-interop:19.0.0") // Versão pode variar

    // Para testes de corrotinas
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}