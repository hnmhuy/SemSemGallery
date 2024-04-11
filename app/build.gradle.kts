plugins {
        alias(libs.plugins.androidApplication)
    id("ly.img.android.sdk").version("10.9.0")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

IMGLY.configure() {
    pesdk {
        enabled(true)
        licensePath(null)
    }

    modules {
        include("ui:core")
        include("ui:text")
        include("ui:focus")
        include("ui:frame")
        include("ui:brush")
        include("ui:filter")
        include("ui:sticker")
        include("ui:overlay")
        include("ui:transform")
        include("ui:adjustment")
        include("ui:text-design")

        include("backend:serializer")
        include("backend:headless")
        include("assets:font-basic")
        include("assets:frame-basic")
        include("assets:filter-basic")
        include("assets:overlay-basic")
        include("assets:sticker-shapes")
        include("assets:sticker-emoticons")
        include("backend:sticker-smart")
    }
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
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        kotlinOptions {
            jvmTarget = "17"
        }
        buildFeatures {
            viewBinding = true
        }

    }

    dependencies {

        val fragment_version = "1.6.2"
        implementation("androidx.fragment:fragment:$fragment_version")
        implementation ("com.google.android.gms:play-services-auth:21.0.0")
        implementation("com.squareup.picasso:picasso:2.5.2")
        implementation("com.google.android.material:material:1.3.0-alpha02")
        implementation("androidx.recyclerview:recyclerview:1.3.2")
        implementation("de.hdodenhof:circleimageview:3.1.0")
        implementation("com.github.Kunzisoft:Android-SwitchDateTimePicker:2.1")
        implementation("com.google.mlkit:text-recognition:16.0.0");         // To recognize Latin script
        implementation("com.google.mlkit:text-recognition-korean:16.0.0");  //To recognize Korean script (ja)
        implementation("com.google.mlkit:text-recognition-chinese:16.0.0"); // To recognize Chinese script (ko)
        implementation("com.google.mlkit:language-id:17.0.5");
        implementation("com.google.mlkit:image-labeling:17.0.8");
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation("com.github.chrisbanes:PhotoView:2.0.0")
        implementation(libs.constraintlayout)
        implementation(libs.firebase.firestore)
        implementation(libs.firebase.storage)
        implementation(libs.lifecycle.livedata.ktx)
        implementation(libs.lifecycle.viewmodel.ktx)
        implementation(libs.navigation.fragment)
        implementation(libs.navigation.ui)
        implementation(libs.firebase.auth)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
        implementation(libs.glide)
    }