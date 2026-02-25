This is a Kotlin Multiplatform project targeting Desktop (JVM).

## Getting started

This project contains submodules, so the first command you execute should be:

```bash
git submodule update --init
```


## Requirements

The project is being developed using JDK 23. Some dependencies require JDK 11 to be available on the system to build.

Navigate to the root of the project source code `cd pretixscan`.

You can run the desktop application in full local runtime (ideal for development):

```bash
./gradlew run
```

To run the final binary image for the current platform:

```bash
./gradlew runDistributable
```

## Packaging

KMP provides for various native distributions to be created, please refer to the [JetBrains documentation](https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Native_distributions_and_local_execution).


For example, create a distribution package for the current OS:

```bash
./gradlew packageDistributionForCurrentOS -PappPackageName="pretixSCAN"
```

By default, packages are created under `pretixscan/composeApp/build/compose/binaries`.

## Translations

- Guidelines for localizing a KMP project can be
  found [here](https://kotlinlang.org/docs/multiplatform/compose-localize-strings.html#what-s-next).
- Base locale is stored at `pretixscan/composeApp/src/commonMain/composeResources/values/strings.xml`

To test a locale, you can start the application by adding the locale as a VM option:
`-Duser.language=de -Duser.region=DE`

## Acknowledgements

* Webcam support implementation was inspired by [akexorcist/backdrop](https://github.com/akexorcist/backdrop)
