import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

ksp {
    // Guarda el esquema de la base de datos para poder hacer migraciones en el futuro.
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.example.macros"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.macros"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Firma de release: los datos (y la contraseña) se leen de un archivo FUERA del repo.
    // Ruta por defecto en el PC de Mikel; se puede cambiar con -Pmacros.firma=ruta.
    // Si el archivo no existe, la release sale sin firmar (debug no se ve afectado).
    val archivoFirma = file(
        providers.gradleProperty("macros.firma").getOrElse("C:/Users/mikel/claves/macros-release.properties"),
    )
    val firma = if (archivoFirma.exists()) {
        Properties().apply { archivoFirma.inputStream().use { load(it) } }
    } else {
        null
    }
    signingConfigs {
        if (firma != null) {
            create("release") {
                storeFile = file(firma.getProperty("storeFile"))
                storePassword = firma.getProperty("storePassword")
                keyAlias = firma.getProperty("keyAlias")
                keyPassword = firma.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (firma != null) signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.vico.compose.m3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
