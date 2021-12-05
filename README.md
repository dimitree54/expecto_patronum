[![CI](https://github.com/dimitree54/ChNN-Library/actions/workflows/build_test_workflow.yml/badge.svg)](https://github.com/dimitree54/ChNN-Library/actions/workflows/build_test_workflow.yml)

# ChNN-Library (Chaotic neural network library)

A library for creation and evaluation chaotic (non-layered) neural networks. 
Also, that library provides quality estimation and evolution algorithms for such networks
 to allow search for the best neurons and architectures.

## About ChNN project

In short, ChNN project is a sandbox to play with different neurons and communication rules trying to achieve a synergy
 of a bunch of simple neurons. 
In the network structure we try to introduce as little restrictions as possible making useful properties to emerge from
 simple communication rules.
More info about ChNN project can be found on the [project page](https://dimitree54.github.io/ChNN/).

For faster search of neurons with such a collaborative properties, 
 anybody can suggest their own implementation of the neuron and try it out in the competition with other solutions.
More info about ChNN contest can be found on the [contest page](https://dimitree54.github.io/ChNN/contest/). 

That repository contains a library for chaotic neural networks creation and evaluation, the core of the ChNN project.

Other ChNN project parts:

[[ChNN-Neurons]](https://github.com/dimitree54/ChNN-Neurons) Neurons zoo. 
Visit it to suggest your own neuron implementation and take part in the contest.

[[ChNN-Client]](https://github.com/dimitree54/ChNN) Repository that contains contest evaluation code and Desktop client
to visualise ChNN.

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
Then add to your system environment variables `GITHUB_ACTOR` and `GITHUB_TOKEN`, for example:
```shell
export GITHUB_ACTOR=dimitree54
export GITHUB_TOKEN=ghp_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
```
Note that export adds variables to the environment just for one user session. 
To add them permanently you need to add them to `~/.profile` file (Linux) or follow guides for your OS.

You may want to [generate special GitHub](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) 
 token with limited permissions instead of using your real GitHub password.
But be aware, that such tokens can not be restored after generation, 
 so it is recommended to save them in environment variables permanently (in `~/.profile`).

2. Add the library itself as dependency specifying its full name:
```kotlin
dependencies {
    /*
	other dependencies that you need, for example `implementation(kotlin("stdlib"))`
     */
	implementation("we.rashchenko:chnn-library:v0.1.6")
}
```
You may check available version (and the number of the last version) on GitHub home page of the library.

## Documentation

To understand classes structure you can check [documentation](https://dimitree54.github.io/ChNN-Library/).

## Participate in contest

Visit [[ChNN-Neurons]](https://github.com/dimitree54/ChNN-Neurons) for more info.

## Contribute

We are happy to see contributors. Here some ideas how you can help to improve the project:
1. Code contributions: 
   1. check issues in any ChNN project repository. You will find some ideas what to improve.
   Or suggest your own code improvements by opening an issue, we will discuss that.
   2. Write in issue that you are going to work on it.
   3. Clone the repo. 
   4. Solve an issue in you branch.
   5. Create a pull request.
2. Participation: taking part in the contest is also a great way to push project forward. 
More participants we have - more chances to find great neurons.
3. Attention contribution: you can help by making more people know about the ChNN project by mentioning us in social networks.
4. Ideas contribution: if code contribution is too time-consuming for you, we would be happy if you share your expertise.
Check out discussions section of all ChNN repos, leave useful comments in issues or open your own discussions and issues.
We are looking for advices in 
 code architecture, 
 infrastructure (how to organise automatic submissions check, results publishing, simplify contribution pipeline),
 general ideas (how to improve ChNN model) 
 and so on.
5. Sponsorship: It would be great to make some prise founds for contest or to support contributors of the projects. 
For now it is not supported to help with money. But if you are interested in it, create an issue to add donation channels.
If that option popular, we will add it.

## Author

If you have questions or suggestion not covered by the README, you can reach me (Dmitrii Rashchenko) via email (dimitree54@gmail.com)
