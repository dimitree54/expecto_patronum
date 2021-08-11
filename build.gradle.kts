plugins {
	kotlin("jvm") version "1.5.21"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(kotlin("stdlib"))

	testImplementation(kotlin("test-junit5"))
}

tasks.test {
	useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
}

// we need to specify following sourceSets because we store main and test not in default
//  location (which is module_path/src/main and module_path/src/test)
sourceSets.main {
	java.srcDirs("main")
}

sourceSets.test {
	java.srcDirs("test")
}
