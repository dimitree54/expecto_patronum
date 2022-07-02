plugins {
	id("maven-publish")
	kotlin("jvm") version "1.7.0"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(kotlin("stdlib"))
	testImplementation(kotlin("test-junit5"))
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

publishing {
	repositories {
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/dimitree54/expecto_patronum_library")
			credentials {
				username = System.getenv("GITHUB_ACTOR")
				password = System.getenv("GITHUB_TOKEN")
			}
		}
	}
}
