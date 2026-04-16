plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":feature:reservation:api"))
    implementation(project(":feature:reservation:domain"))
    implementation(project(":feature:wishlist:data"))
    testImplementation(libs.junit)
}

