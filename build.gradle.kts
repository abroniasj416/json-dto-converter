plugins {
    id("java")
    application
}

application {
    mainClass.set("org.example.Main")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.25.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.example.Main"
    }

    // runtimeClasspath에 있는 모든 JAR들을 현재 JAR에 풀어 넣어서 Fat JAR 생성
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
}