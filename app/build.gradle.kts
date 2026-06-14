import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.google.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.junit6)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.graytsar.livewallpaper"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.graytsar.livewallpaper"
        minSdk = 28
        targetSdk = 37
        versionCode = 25
        versionName = "1.1.11"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    testOptions {
        unitTests {
            unitTests.all {
                it.useJUnitPlatform()
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

base {
    archivesName = "rei"
}

dependencies {
    //android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.work.runtime.ktx)

    //compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.compose.hilt.navigation)
    ksp(libs.kotlin.metadata.jvm)

    //datastore
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.serialization.protobuf)

    //lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    //navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    //hilt
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.navigation.fragment)
    implementation(libs.androidx.hilt.work)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.hilt.compiler)

    //ads
    implementation(libs.play.services.ads)

    //test
    testImplementation(platform(libs.junit6.bom))
    testImplementation(libs.junit6.api)
    testRuntimeOnly(libs.junit6.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.assertions.core)
    //test instrumented
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation(libs.junit6.api)
    androidTestImplementation(libs.junit6.android.core)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.kotest.assertions.core)
    androidTestRuntimeOnly(libs.junit6.android.runner)
    //test coroutines
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)


    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
    implementation(project(":core:repository"))
}
