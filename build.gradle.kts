plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.google.hilt) apply false
    alias(libs.plugins.android.junit6) apply false
}

tasks.register("clean", Delete::class) {
    description = "clean build directory"
    delete(rootProject.layout.buildDirectory)
}