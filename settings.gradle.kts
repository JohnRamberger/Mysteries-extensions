pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
//dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {
//        google()
//        mavenCentral()
//        maven(url = "https://www.jitpack.io")
//    }
//}

rootProject.name = "Mysteries-extensions"
includeAllModules()

fun includeAllModules() {
    File(rootDir, "src").listFiles()?.forEach { languageDir ->
        if (languageDir.isDirectory) {
            languageDir.listFiles()?.forEach { moduleDir ->
                if (moduleDir.isDirectory && File(moduleDir, "build.gradle.kts").exists()) {
                    include(":src:${languageDir.name}:${moduleDir.name}")
                }
            }
        }
    }
}
