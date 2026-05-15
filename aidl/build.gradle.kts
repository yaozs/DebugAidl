plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    namespace = "com.siasun.ds.aidl"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        aidl = true
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.github.yaozs"
                artifactId = "DebugAidl"
                version = "0.5.0"

                // 直接指定 AAR 文件，替代 from(components["release"])
                artifact(tasks.named("bundleReleaseAar").map { it.outputs.files.single() })

                // 可选：同时发布 sources.jar
                artifact(tasks.named("sourcesJar").map { it.outputs.files.single() }) {
                    classifier = "sources"
                }
            }
        }
    }
}

// 如果希望包含源码，需要单独定义 sourcesJar 任务
tasks.register<Jar>("sourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}


dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}


