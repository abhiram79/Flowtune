@file:Suppress("UnstableApiUsage")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
}

rootProject.name = "Flowtune"
include(":app")
include(":flowtube")
include(":providers:kugou")
include(":providers:lrclib")
include(":providers:material-color-utilities")
include(":providers:kizzy")
