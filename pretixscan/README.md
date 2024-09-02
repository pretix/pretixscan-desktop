This is a Kotlin Multiplatform project targeting Desktop.

## Requirements

The project is being developed using JDK 21. Some dependencies require JDK 11 to be available on the system to build.

You can run the desktop application in full local runtime (ideal for development):

```shell
./gradlew run
```

To run the final binary image for the current platform:

```shell
./gradlew runDistributable
```

## Packaging

https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Native_distributions_and_local_execution

```shell
./gradlew packageDistributionForCurrentOS
```

By default, packages are created under `pretixscan/composeApp/build/compose/binaries`.
