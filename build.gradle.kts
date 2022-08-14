plugins {
	id("maven-publish")
	kotlin("jvm") version "1.7.10"
}

repositories {
	mavenCentral()
	maven("https://jitpack.io")
	maven("https://mvn.mchv.eu/repository/mchv/")
	mavenLocal()
}

dependencies {
	implementation(kotlin("stdlib"))
	implementation("org.junit.jupiter:junit-jupiter:5.8.2")
	testImplementation(kotlin("test-junit5"))
	implementation("org.mongodb:mongodb-driver-sync:4.6.0")
	implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.0.7")
    implementation(kotlin("script-runtime"))
}

dependencies {
	implementation(platform("it.tdlight:tdlight-java-bom:1.0.0.0-SNAPSHOT"))
	implementation("it.tdlight:tdlight-java:1.0.0.0-SNAPSHOT")
	implementation("it.tdlight:tdlight-natives-osx-aarch64:4.0.0-SNAPSHOT")
	implementation("org.slf4j:slf4j-api:1.7.36")
	implementation("org.slf4j:slf4j-simple:1.7.36")
}

// we need to specify following sourceSets because we store main and test not in default
//  location (which is module_path/src/main and module_path/src/test)
sourceSets.main {
	java.srcDirs("src/main")
}

sourceSets.test {
	java.srcDirs("src/test")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
	useJUnitPlatform()
	maxParallelForks = 8
}
