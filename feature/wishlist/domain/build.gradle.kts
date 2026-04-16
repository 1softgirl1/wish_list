plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":feature:wishlist:api"))
    testImplementation(libs.junit)
}
