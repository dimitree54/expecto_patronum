import org.apache.tools.ant.taskdefs.condition.Os.*

plugins {
	kotlin("jvm") version "1.7.10"
}

repositories {
	mavenCentral()
	mavenLocal()
	maven("https://jitpack.io")
	maven("https://mvn.mchv.eu/repository/mchv/")
}

dependencies {
	implementation(kotlin("stdlib"))
	implementation("org.junit.jupiter:junit-jupiter:5.8.2")
	testImplementation(kotlin("test-junit5"))
	implementation("org.mongodb:mongodb-driver-sync:4.7.1")
	implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.0.7")
	implementation(kotlin("script-runtime"))
	implementation("org.slf4j:slf4j-api:1.7.36")
	implementation("org.slf4j:slf4j-simple:1.7.36")
}

if (isFamily(FAMILY_MAC) && isArch("aarch64")){
	dependencies {
		implementation(platform("it.tdlight:tdlight-java-bom:1.0.0.0-SNAPSHOT"))
		implementation("it.tdlight:tdlight-java:1.0.0.0-SNAPSHOT")
		implementation("it.tdlight:tdlight-natives-osx-aarch64:4.0.0-SNAPSHOT")
	}
}
else{
	dependencies {
		implementation(platform("it.tdlight:tdlight-java-bom:2.8.4.1"))
		implementation("it.tdlight:tdlight-java:2.8.4.1")
		implementation("it.tdlight:tdlight-natives-linux-amd64:4.0.265")
	}
}

// we need to specify following sourceSets because we store main and test not in default
//  location (which is module_path/src/main and module_path/src/test)
sourceSets.main {
	java.srcDirs("src/main")
}

sourceSets.test {
	java.srcDirs("src/test")
}

tasks{
	jar {
		manifest {
			attributes["Main-Class"] = "we.rashchenko.patronum.MainKt"
		}
		from(
			Callable {
				configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) }
			}
		)
		duplicatesStrategy = DuplicatesStrategy.INCLUDE
	}
}
