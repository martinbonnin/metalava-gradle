plugins {
    `java-library`
    id("me.tylerbwong.gradle.metalava")
}

metalava {
    filename = "api/$name-api.txt"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.10")
}
