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

## Database Schema

The database schema is described in `AppDatabase.sq` using the SQDelight library. After changes to this file, run
`./gradlew generateCommonMainAppDatabaseInterface` to have the changes reflected in the generated Kotlin entities. For
more information, please [check here](https://cashapp.github.io/sqldelight/2.0.2/multiplatform_sqlite/).