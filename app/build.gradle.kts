plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.bookvoyager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bookvoyager"
        minSdk = 24
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

    buildFeatures{
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

    implementation("com.caverock:androidsvg:1.4")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.android.gms:play-services-basement:18.2.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation (libs.lottie)

    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation ("org.robolectric:robolectric:4.10.3")

    implementation(libs.firebase.firestore)
    implementation(libs.volley)
    implementation(libs.firebase.storage)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)

    androidTestImplementation(libs.espresso.core)
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
}
/*
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}*/
