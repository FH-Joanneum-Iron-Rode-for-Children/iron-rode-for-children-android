# Iron Road For Children Android App (IRFC)

## Getting started

The project uses libraries which make use of code generation with `ksp` (Ktorfit, Room,
ComposeDestinations). When building the project the needed files will be generated automatically.
To initially generate all the files without building the whole project you can also run
`./gradlew kspDebugKotlin`. This may also be necessary after you updated the git repository
(e.g. pull, switch branch, ...). In case some classes/functions etc. can not be found run this
command to be sure that all the generated files are up to date.

## Code Style

The project uses ktlint and detekt to ensure a consistent code style. On first gradle sync git
pre-commit and pre-push hooks are generated. When committing ktlintFormat is run and the formatted
files are added back to the commit. When pushing ktlintCheck and detekt is executed. If there is a
issue with formatting then the push gets rejected. The issues are listed in the console and also can
be found in the build/reports folder. The listed issues must be fixed before you can push to remote.

The hooks can be skipped by using the `--no-verify` flag with the git commands. However the CI will
validate the style again and merging will not be allowed if the checks do not pass.

The tasks can also be executed manually using `./gradlew ktlintCheck` (or `./gradlew ktlintFormat`
to auto format all files) and `./gradlew detekt`.

## Release

1. Create a new release branch (can be skipped when already releasing from a non protected branch)
2. Run the release gradle task and specify the new version `./gradlew release -Pversion=x.x.x-bx` (
   e.g. ./gradlew release -Pversion=2.0.0-b10)
    - Will increase the version specified in gradle.properties and commit this change
    - Create and push a version tag
    - Trigger the publish to internal Testing CI/CD workflow
3. Go and grab a coffee while waiting till the CI/CD pipeline builds and publishes the app
4. Download the new version from Google Play (you have to be part of the internal testers group)
5. Test if the app works properly
6. If the app should be released to the public then go to the Google Play console and promote the
   just released version from `internalTesting` to `release` track
    - Do not forget to add a Version note!
7. Depending on the release type:
    - **Production release**: Merge the branch where the release was made from into main and main
      back into develop.
    - **Otherwise**: Merge the branch where the release was made into develop.

## Further References: Language, Libraries & Tools

- [Gradle](https://gradle.org/) Build tool
    - [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) Fixed Gradle
      version in all environments
    - [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html) Type safe
      Gradle build configs
- [Kotlin](https://kotlinlang.org/) Main language
    - [Coroutines, Suspending Functions & Flow](https://kotlinlang.org/docs/coroutines-guide.html)
    - [KSP](https://kotlinlang.org/docs/ksp-overview.html) Kotlin Symbol Processing
- [Ktorfit](https://foso.github.io/Ktorfit/) HttpClient abstraction
    - [Ktor](https://ktor.io/) HttpClient
    - [Kotlinx Serialization](https://kotlinlang.org/docs/serialization.html) JSON serializer
- [Room](https://developer.android.com/training/data-storage/room) Database abstraction
- [Koin](https://insert-koin.io/) Dependency Injection
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
    - [Compose Destinations](https://composedestinations.rafaelcosta.xyz/) Type safe navigation
    - [Coil](https://coil-kt.github.io/coil/) Async image loading and caching
    - [Material 3](https://developer.android.com/jetpack/compose/designsystems/material3)
- [Ktlint](https://pinterest.github.io/ktlint/) Kotlin linter
    - [ktlint-gradle](https://github.com/JLLeitschuh/ktlint-gradle) Ktlint gradle plugin
- [Detekt](https://detekt.dev/) Kotlin static code analyzer