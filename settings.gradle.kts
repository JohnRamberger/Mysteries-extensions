// includeAllModules()
includeSingleModule("en", "novelfull")

fun includeAllModules() {
    File(rootDir, "src").listFiles()?.forEach { languageDir ->
        if (languageDir.isDirectory) {
            languageDir.listFiles()?.forEach { moduleDir ->
                if (moduleDir.isDirectory && File(moduleDir, "build.gradle").exists()) {
                    include(":src:${languageDir.name}:${moduleDir.name}")
                }
            }
        }
    }
}

fun includeSingleModule(
    language: String = "en",
    moduleName: String,
) {
    include(":src:$language:$moduleName")
}

include(":base")
