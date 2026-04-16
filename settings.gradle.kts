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
    }
}

rootProject.name = "wish_list"
include(":app")

include(":core")
include(":core:navigation")
include(":feature:wishlist:api")
include(":feature:wishlist:domain")
include(":feature:wishlist:data")
include(":feature:wishlist:ui")
include(":feature:public-wishlist:api")
include(":feature:public-wishlist:domain")
include(":feature:public-wishlist:data")
include(":feature:public-wishlist:ui")
include(":feature:reservation:api")
include(":feature:reservation:domain")
include(":feature:reservation:data")
include(":feature:reservation:ui")
