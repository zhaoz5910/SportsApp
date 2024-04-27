plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.zhangzhao.sportsapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zhangzhao.sportsapp"
        minSdk = 29
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    //hilt
    implementation(libs.dagger.hilt.android)
    implementation(libs.play.services.maps)
    kapt(libs.hilt.android.compiler)

    //高德地图
    implementation(fileTree("libs/AMap3DMap_10.0.600_AMapSearch_9.7.1_AMapLocation_6.4.3_20240314.aar"))
    //implementation(fileTree("libs/AMap2DMap_6.0.0_AMapSearch_9.7.1_AMapLocation_6.4.3_20240314.aar"))


    implementation(libs.androidx.lifecycle.service)
    // ViewModel相关依赖
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)

    //Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    //协程
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    //导航
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui.ktx)

    //Glide
    implementation(libs.github.glide.glide)
    ksp(libs.github.glide.compiler)

    //easy permissions
    implementation(libs.easypermissions)

    //timber
    implementation(libs.com.jakewharton.timber.timber5)

    //MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kapt {
    correctErrorTypes = true
}