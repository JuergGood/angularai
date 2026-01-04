# Android Build Instructions

This document provides instructions on how to build and run the Android frontend of the AngularAI application locally.

## Prerequisites

1.  **Android Studio**: It is highly recommended to use the latest version of [Android Studio](https://developer.android.com/studio).
2.  **JDK 17**: The project is configured to use Java 17.
3.  **Android SDK**: Ensure you have Android SDK 34 (UpsideDownCake) installed.

## Building with Android Studio (Recommended)

1.  Open Android Studio.
2.  Select **Open** and navigate to the `android/` directory in the project root.
3.  Wait for Android Studio to import the project and sync with Gradle.
4.  Once the sync is complete, you can build the project by going to **Build > Make Project**.
5.  To run the app, select a device (emulator or physical device) and click the **Run** (green play) icon.

## Building with Command Line

If you have Gradle installed on your machine, you can build the APK from the terminal:

1.  Open a terminal and navigate to the `android/` directory.
2.  Run the following command to build the debug APK:
    ```bash
    gradle assembleDebug
    ```
3.  The generated APK will be located at `android/app/build/outputs/apk/debug/app-debug.apk`.

*Note: If you don't have the Gradle wrapper files, opening the project in Android Studio for the first time will generate them for you.*

## Connecting to the Backend

The Android app needs to communicate with the Spring Boot backend. 

### Emulator (10.0.2.2)
If you are running the backend on `localhost:8080` and using the **Android Emulator**, the app is already configured to use `10.0.2.2:8080`, which is the special alias to your host loopback interface.

### Physical Device
If you are using a **physical device**, you must ensure both the device and your computer are on the same Wi-Fi network.
1.  Find your computer's local IP address (e.g., `192.168.1.15`).
2.  Update the `baseUrl` in `android/app/src/main/java/ch/goodone/angularai/android/di/NetworkModule.kt` to point to your computer's IP:
    ```kotlin
    .baseUrl("http://192.168.1.15:8080/")
    ```
3.  Ensure the backend's `SecurityConfig.java` allows CORS from your device's IP (it is currently configured to allow `192.168.*`).

## Troubleshooting

- **Gradle Sync Fails**: Ensure you have an active internet connection to download dependencies.
- **JDK Version**: If you get a "class file has wrong version" error, make sure Android Studio is configured to use JDK 17 in **Settings > Build, Execution, Deployment > Build Tools > Gradle**.
- **CORS Issues**: Check the backend logs to see if requests from the mobile device are being rejected.
