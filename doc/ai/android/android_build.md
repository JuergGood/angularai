To build the Android module locally, you have two primary options: using **Android Studio** (recommended) or the **Command Line**.

### 1. Using Android Studio (Recommended)
1.  **Open Android Studio**.
2.  Select **Open** and navigate to the `android/` directory in the project root.
3.  Wait for the IDE to sync with Gradle (this will automatically generate the missing Gradle wrapper files).
4.  Go to **Build > Make Project**.
5.  To run the app, select an emulator or physical device and click the **Run** icon.

### 2. Using the Command Line
If you have **Gradle** installed on your system:
1.  Open your terminal and navigate to the `android/` directory.
2.  Run the following command:
    ```bash
    gradle assembleDebug
    ```
3.  The generated APK will be available at:
    `android/app/build/outputs/apk/debug/app-debug.apk`

### Important: Connecting to the Backend
*   **Emulator**: The app is pre-configured to use `http://10.0.2.2:8080/` to connect to a backend running on your host's `localhost:8080`.
*   **Physical Device**: You will need to update the `baseUrl` in `android/app/src/main/java/ch/goodone/angularai/android/di/NetworkModule.kt` to your computer's local IP address (e.g., `http://192.168.1.15:8080/`).

Detailed instructions, including prerequisites and troubleshooting, can be found in the newly created documentation:
`doc/ai/android/android-build-instructions.md`

The main `README.md` has also been updated to include these instructions.