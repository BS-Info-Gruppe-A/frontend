# BSINFO frontend

## Requirements

- [JDK 23 or newer](https://adoptium.net/de/temurin/releases/?version=23)
- [Android SDK](https://developer.android.com/studio) (Only for Android app)
- [jextract](https://jdk.java.net/jextract/) (only for Desktop app)
- [Rust](https://rustup.rs/) (only for Desktop app)
- [XCode 16.2 or newer](https://developer.apple.com/xcode/) (only for iOS app; iOS sdk needs to be
  installed)
- [CocoaPods](https://guides.cocoapods.org/using/getting-started.html) (only for ios app)

## Modules

The project is divided into the following modules

### native_helper

Small Rust library and JVM glue code based on
[FFM](https://docs.oracle.com/en/java/javase/22/core/foreign-function-and-memory-api.html) for
interacting with native APIs

### common

THe shared UI code for all platforms, this also contains the API client for the provided REST API

### file_dialog

Shared code for file dialogs between [native_helper](#native_helper) and [common](#common)

### Android

Android launcher for the app

#### Building

In order to build the Android app the [local.properties] file needs to contain a `sdk.dir` property
pointing to the Android SDK location, using Android Studio does this automatically

Run current app: https://developer.android.com/studio/run#basic-build-run
Create package: `./gradlew android:assembleRelease`

### desktop

Desktop (Windows, Linux, macOS) launcher for the app

#### Building

> [!NOTE]  
> Building on macOS might need 3 tries, because Apple hates developers and forces you to run
> unsigned
> applications again after approving them. During the first build, you may get an error that
> `jextract` could not run, go to System Settings -> Privacy and Security -> run anyways
> then run the build again and do the same for `libclang.dylib`.
> After running a build the third time it works

Run current app: `./gradlew desktop:run`
Create package: `./gradlew desktop:packageReleaseDistributionForCurrentOS`

### iOS

iOS launcher for the app

In order to build the iOS app [cocoapods](#requirements) and the iPhone OS SDK needs to be installed

#### Building

Run current app: `open ios/ios.xcworkspace` -> click the run Button in the top left
Create package: `open ios/ios.xcworkspace` -> click on product -> archive

### web

web launcher for the app using WasmJS

In order to use the WebApp your browser needs to support Wasm and Wasm GC, check
[this](https://kotlinlang.org/docs/wasm-troubleshooting.html#browser-versions) guide on
how to enable those features for your browser

#### Building

Run current app: `./gradlew web:wasmJsBrowserRun`
Create package: `./gradlew web:wasmJsBrowserDistribution`
