plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false // ← 修正ポイント
    id("com.google.devtools.ksp") version "1.9.22-1.0.18" apply false // Kotlinと合わせるのが安全
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    if (name == "isar_flutter_libs") {
        afterEvaluate {
            plugins.withId("com.android.library") {
                // ✅ namespace を設定
                extensions.configure<com.android.build.gradle.LibraryExtension>("android") {
                    namespace = "com.github.isar_flutter_libs"
                }
            }

            // ✅ AndroidManifest.xml の package 属性を削除
            val manifestFile = file("src/main/AndroidManifest.xml")
            if (manifestFile.exists()) {
                val original = manifestFile.readText()
                val modified = original.replace("""package\s*=\s*"dev\.isar\.isar_flutter_libs"""", "")
                manifestFile.writeText(modified)
            }
        }
    }
}

val newBuildDir: Directory = rootProject.layout.buildDirectory.dir("../../build").get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}
subprojects {
    project.evaluationDependsOn(":app")
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
