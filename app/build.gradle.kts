import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.secrets.gradle.plugin)
}

android {
    namespace = "com.example.miformacionctma"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.miformacionctma"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("androidTest") {
            assets.directories.add("$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    flavorDimensions += "environment"

    val secretsFile = rootProject.file("secrets.properties")
    val secretsProps = Properties()
    if (secretsFile.exists()) {
        secretsFile.inputStream().use { stream ->
            secretsProps.load(stream)
        }
    }

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "ENVIRONMENT", "\"DEV\"")
            val url = (secretsProps.getProperty("SUPABASE_URL_DEV") ?: secretsProps.getProperty("SUPABASE_URL"))?.trim('"', '\'')
            if (url != null) {
                buildConfigField("String", "SUPABASE_URL", "\"$url\"")
            }
            val key = (secretsProps.getProperty("SUPABASE_KEY_DEV") ?: secretsProps.getProperty("SUPABASE_PUBLISHABLE_KEY"))?.trim('"', '\'')
            if (key != null) {
                buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"$key\"")
            }
        }
        create("stage") {
            dimension = "environment"
            applicationIdSuffix = ".stage"
            versionNameSuffix = "-stage"
            buildConfigField("String", "ENVIRONMENT", "\"STAGE\"")
            val url = (secretsProps.getProperty("SUPABASE_URL_STAGE") ?: secretsProps.getProperty("SUPABASE_URL"))?.trim('"', '\'')
            if (url != null) {
                buildConfigField("String", "SUPABASE_URL", "\"$url\"")
            }
            val key = (secretsProps.getProperty("SUPABASE_KEY_STAGE") ?: secretsProps.getProperty("SUPABASE_PUBLISHABLE_KEY"))?.trim('"', '\'')
            if (key != null) {
                buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"$key\"")
            }
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "ENVIRONMENT", "\"PROD\"")
            val url = (secretsProps.getProperty("SUPABASE_URL_PROD") ?: secretsProps.getProperty("SUPABASE_URL"))?.trim('"', '\'')
            if (url != null) {
                buildConfigField("String", "SUPABASE_URL", "\"$url\"")
            }
            val key = (secretsProps.getProperty("SUPABASE_KEY_PROD") ?: secretsProps.getProperty("SUPABASE_PUBLISHABLE_KEY"))?.trim('"', '\'')
            if (key != null) {
                buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"$key\"")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    testOptions {
        unitTests {
            all {
                it.jvmArgs(
                    "-XX:+EnableDynamicAgentLoading",
                    "-Xshare:off"
                )
            }
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

secrets {
    // Configura el nombre del archivo de secretos
    propertiesFileName = "secrets.properties"
    // Archivo de ejemplo para control de versiones
    defaultPropertiesFileName = "secrets.properties.example"
}



ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.sqlite)
    implementation(libs.androidx.room3.testing)
    implementation(libs.androidx.sqlite.bundled)
    testImplementation(libs.kotlinx.coroutines.test)
    ksp(libs.androidx.room3.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.mockk)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
