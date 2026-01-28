plugins {
    id("java-library")
}

group = "me.almana.hyvoltz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "hytale-release"
        url = uri("https://maven.hytale.com/release")
    }
    maven {
        name = "hytale-pre-release"
        url = uri("https://maven.hytale.com/pre-release")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.hypixel.hytale:Server:2026.01.22-6f8bdbdc4")
    
    compileOnly("com.hypixel.hytale:Server:2026.01.22-6f8bdbdc4")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}


