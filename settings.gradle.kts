pluginManagement {
    repositories {
        google()               // ✅ androidx 등 모두 포함됨
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()               // ✅ androidx.compose.compiler도 포함됨
        mavenCentral()
    }
}

rootProject.name = "QuantityCalendar"
include(":app")
