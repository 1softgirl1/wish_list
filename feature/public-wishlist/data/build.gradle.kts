plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":feature:public-wishlist:api"))
    implementation(project(":feature:public-wishlist:domain"))
    implementation(project(":feature:wishlist:data"))
    testImplementation(libs.junit)
}

