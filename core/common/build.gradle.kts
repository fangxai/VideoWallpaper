import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.android.junit6)
}

android {
    namespace = "com.graytsar.livewallpaper.core.common"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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

dependencies {
    implementation(libs.androidx.core.ktx)

    //testing
    testImplementation(platform(libs.junit6.bom))
    testImplementation(libs.junit6.api)
    testImplementation(libs.kotest.assertions.core)
    testRuntimeOnly(libs.junit6.engine)
    androidTestImplementation(libs.kotest.assertions.core)
    androidTestImplementation(libs.junit6.api)
    androidTestImplementation(libs.junit6.android.core)
    androidTestRuntimeOnly(libs.junit6.android.runner)
}