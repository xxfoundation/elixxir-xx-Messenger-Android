# xxmessenger (Android)

***Current Version:*** 2.7/598 (MainNet)<br>
***Device Orientation:*** Portrait<br>
***API Target:*** Android 26+ (Oreo)

## Setup
Clone this repository and import into **Android Studio**
```bash
git clone git@git.xx.network:elixxir/client-android.git
```

## Build variants
Use the Android Studio *Build Variants* button to choose between **local-ndf**,**mock-env**, **prod-ndf** and **betanet** flavors combined with debug and release build types.
- `mainNet:` Production build on MainNet
- `mainNetDebug:` Debuggable build for use during development on MainNet
- `releaseNet:` Debuggable build for use during development on the ReleaseNet network
- `mock:` Mock environment to test UI only

## Manual Deploy
### Android Studio
1. The app currently has a dependency on Firebase Crashlytics and Firebase Cloud Messaging. You will have to either set up your
own Firebase Project [here](https://firebase.google.com/docs/android/setup) or remove Firebase
from the project by follow the steps below.
2. In Android Studio Menu, click Build > Generate Signed Bundle/APK to create an APK you can deploy on an Android device. Create a new keystore if necessary.

### Removing Firebase dependency
1. Remove id("com.google.gms.google-services") from build.gradle.kts
2. Remove all Firebase references from build.gradle.kts
3. In Android Studio Menu, click Build > Make Project. The build will fail, as Firebase
references no longer compile. You may remove each reference that is found in Build Output.


## Architecture
1. This app uses [MVVM architecture](https://developer.android.com/jetpack/guide) with dependency injection via [Dagger2](https://github.com/google/dagger)
2. Each component of the app has its own package that includes UI using `fragments` and its `ViewModels`
3. A single `activity` is used as a NavHost to host for multiple `fragments`
4. Navigation uses [Navigation Component](https://developer.android.com/guide/navigation)
5. Dependency injection with [Mockito](https://github.com/mockito/mockito) is preferable for Unit Tests.

## Libraries used
### Core
[Kotlin Standard Library](https://kotlinlang.org/api/latest/jvm/stdlib/)\
[KotlinX Coroutines](https://github.com/Kotlin/kotlinx.coroutines)\
[Guava](https://github.com/google/guava)\
[Android KTX](https://developer.android.com/kotlin/ktx)\
[Android MultiDex](https://developer.android.com/studio/build/multidex)\
[Dagger2](https://github.com/google/dagger)

### Android Components and UI - Jetpack
[AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle)\
[AndroidX Navigation](https://developer.android.com/jetpack/androidx/releases/navigation)\
[AndroidX AppCompat](https://developer.android.com/jetpack/androidx/releases/appcompat)\
[AndroidX Biometrics](https://developer.android.com/jetpack/androidx/releases/biometric)\
[AndroidX RecyclerView](https://developer.android.com/jetpack/androidx/releases/recyclerview)\
[AndroidX Paging](https://developer.android.com/jetpack/androidx/releases/paging)\
[AndroidX CameraX](https://developer.android.com/training/camerax)\
[AndroidX SwipeToRefresh](https://developer.android.com/jetpack/androidx/releases/swiperefreshlayout)

### Android UI - Non Jetpack
[Android Material](https://developer.android.com/reference/com/google/android/material/packages)\
[Lottie](https://github.com/airbnb/lottie-android)

### Database
[Room](https://developer.android.com/jetpack/androidx/releases/room)

### Notifications
[Firebase](https://firebase.google.com/docs/android/setup)

### Reactive
[RxJava2](https://github.com/ReactiveX/RxJava)

### Logger
[Timber](https://github.com/JakeWharton/timber)

### Time
[Kronos](https://github.com/lyft/Kronos-Android)\
[Pretty Time](https://github.com/lyft/Kronos-Android)

### Image Loading and Caching
[Glide](https://github.com/bumptech/glide)

### Data Serialization
[Gson](https://github.com/google/gson)\
[Protobuf](https://developers.google.com/protocol-buffers)

### Testing
[Android Arch Core](https://developer.android.com/jetpack/androidx/releases/arch-core)\
[Truth](https://github.com/google/truth)\
[Mockito](https://github.com/mockito/mockito)\
[JUnit Jupiter](https://junit.org/junit5/docs/current/user-guide/)\
[Expresso](https://developer.android.com/training/testing/espresso)\
[Roboeletric](https://github.com/robolectric/robolectric)
