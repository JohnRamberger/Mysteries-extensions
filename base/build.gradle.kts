plugins {
    id("com.android.library")
    alias(libs.plugins.ktlint)
}

android {
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    namespace = "com.jramberger.mysteries.extension.base"

    sourceSets {
        named("main") {
            manifest.srcFile("AndroidManifest.xml")
            res.setSrcDirs(listOf("res"))
        }
    }

    buildFeatures {
        resValues = false
        shaders = false
    }
}
