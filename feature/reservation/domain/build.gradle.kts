plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":feature:reservation:api"))
    implementation(project(":feature:wishlist:domain"))
    testImplementation(libs.junit)
}

