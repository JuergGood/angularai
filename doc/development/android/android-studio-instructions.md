### Android Development and Testing with Gemini AI

To complete the build, run tests, and fix issues in Android Studio using Gemini AI, follow these steps:

#### 1. Setup and Build
- **Open Project**: Launch Android Studio and open the `android` folder as a project.
- **Sync Project with Gradle Files**: Click the "Elephant" icon or go to `File > Sync Project with Gradle Files`.
- **Build Project**: Use `Build > Make Project` (Ctrl+F9) to verify that the implementation compiles correctly.

#### 2. Running Tests
- **Unit Tests**:
    - Right-click on `android/app/src/test/java` and select `Run 'Tests in 'ch.goodone...'`.
    - These tests are fast and run on the JVM.
- **Android Instrumented Tests**:
    - Right-click on `android/app/src/androidTest/java` and select `Run 'Tests in 'ch.goodone...'`.
    - These require a running Android Emulator or a physical device.

#### 3. Using Gemini AI in Android Studio
- **Access Gemini**: Open the Gemini tool window (usually on the right sidebar).
- **Ask for Code Analysis**: If you encounter a build error or test failure, copy the error message and ask Gemini: *"Explain this error and suggest a fix for my current file."*
- **Generate Unit Tests**: Highlight a class or function and ask Gemini: *"Generate JUnit 5 unit tests for this class, covering edge cases."*
- **Optimize UI**: Highlight a Compose function and ask: *"How can I improve the performance or accessibility of this Composable?"*

#### 4. Debugging and Fixes
- **Logcat**: Use the `Logcat` window to monitor runtime logs. Filter by `package:mine` to see logs from the AngularAI app.
- **Debugger**: Set breakpoints by clicking in the gutter next to the line numbers and use `Run > Debug 'app'` to step through the code.
- **Apply Changes**: Use the "Apply Changes" (Ctrl+Alt+F10) or "Apply Code Changes" (Ctrl+F10) icons to see your edits without a full app restart.

#### 5. Common Android AI Tasks
- **Migration**: If you need to update libraries (e.g., Room or Compose), ask Gemini: *"What are the breaking changes in the latest version of Room, and how do I update my TaskEntity?"*
- **Refactoring**: Use `Refactor > Rename` or ask Gemini: *"Refactor this ViewModel to use StateFlow instead of mutableStateOf for better reactivity."*
