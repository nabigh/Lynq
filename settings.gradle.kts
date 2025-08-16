// settings.gradle.kts

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // This is the key setting
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // Add any other repositories you need here
    }
}

rootProject.name = "SimpleChatApp" // Replace with your actual project name
include(":app") // Or other modules you have