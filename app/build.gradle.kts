    plugins {
        alias(libs.plugins.androidApplication)
    }

    android {
        namespace = "com.example.semsemgallery"
        compileSdk = 34

        defaultConfig {
            applicationId = "com.example.semsemgallery"
            minSdk = 28
            targetSdk = 34
            versionCode = 1
            versionName = "1.0"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }

    dependencies {
        implementation("com.squareup.picasso:picasso:2.5.2")
        implementation ("com.google.android.material:material:1.3.0-alpha02")
        implementation("androidx.recyclerview:recyclerview:1.3.2")
        implementation("de.hdodenhof:circleimageview:3.1.0")
        implementation("com.github.Kunzisoft:Android-SwitchDateTimePicker:2.1")
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation("com.github.chrisbanes:PhotoView:2.0.0")
        implementation(libs.constraintlayout)
        implementation(libs.firebase.firestore)
        implementation(libs.firebase.storage)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
        implementation(libs.glide)
    }