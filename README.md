[![CI](https://github.com/dimitree54/ChNN-Library/actions/workflows/build_test_workflow.yml/badge.svg)](https://github.com/dimitree54/ChNN-Library/actions/workflows/build_test_workflow.yml)

# ChNN-Library

## About

That repository contains a library for chaotic neural networks creation and evaluation, the core of the ChNN project.
Other ChNN project parts:

[[ChNN-Client]](https://github.com/dimitree54/ChNN) Desktop client with visual tools for ChNN creation, training and
inference.

[[ChNN-Neurons]](https://github.com/dimitree54/ChNN-Neurons) Neurons zoo. Visit that repository to tak part in the
contest.

[[ChNN-Server]](https://github.com/dimitree54/ChNN-Server) Repository to discuss (and later implements) scalable ChNN
running environment.

[[Project page]](https://dimitree54.github.io/ChNN/)

## Installation

To use ChNN library in your JAVA/Kotlin project you need to add following to the `build.gradle.kts` file:
1. Import Maven repository where ChNN stored. For now, it is stored in GitHub registry, which is, unfortunately, 
 [does not support anonymous access](https://github.community/t/download-from-github-package-registry-without-authentication/14407).
 We hope that it will be changed in the future, but for now you need GitHub account to use that registry. 
 Add the following code to your `build.gradle.kts`:
 ```kotlin
repositories {
	/*
	other repositories that you need, for example mavenCentral
	 */
    maven {
        url = uri("https://maven.pkg.github.com/dimitree54/chnn-library")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}
```
Then add to your system environment variables `GITHUB_ACTOR` and `GITHUB_TOKEN`:
```shell
export GITHUB_ACTOR=dimitree54
export GITHUB_TOKEN=ghp_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
# Note that export adds variables to the environment just for one user session.
# To add them permanently you need to add them to `~/.profile` file
```
You may want to generate special GitHub token with limited permissions instead of using your real GitHub password.
2. Add the library itself as dependency specifying its full name:
```kotlin
dependencies {
    /*
	other dependencies that you need, for example `implementation(kotlin("stdlib"))`
     */
	implementation("we.rashchenko:chnn-library:v0.1.0")
}
```
You may check available version on GitHub home page of the library.
