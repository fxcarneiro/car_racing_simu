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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/fxcarneiro/car_racing_simu")
            credentials {
                username = System.getenv("GITHUB_USERNAME") ?: "<seu-usuario-github>"
                password = System.getenv("GITHUB_TOKEN") ?: "<seu-token-github>"
            }
        }
    }
}

rootProject.name = "My Application"
include(":app")
include(":mylibrary")
