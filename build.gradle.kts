// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    id("maven-publish")
}

// 添加一个 dummy publication，使 publishToMavenLocal 任务可以正常执行（实际发布由子模块完成）
publishing {
    publications {
        create<MavenPublication>("dummy") {
            groupId = "com.github.siasun-adm"
            artifactId = "root-dummy"
            version = "1.0.0"
            // 不需要真实的 artifact，这个 publication 只是为了让任务存在
        }
    }
}