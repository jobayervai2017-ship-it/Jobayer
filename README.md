# SyncCut Pro — Faceless Video Editor & Subtitle Studio

A modern CapCut-style video editor and automated caption studio built natively with **Kotlin & Jetpack Compose** for Android (with Samsung Exynos & MediaCodec hardware acceleration).

---

## 🚀 How to Open and Run in VS Code on Your PC

### Step 1: Export & Download the Project to Your PC
1. In Google AI Studio, click on **Settings** (or Project options) in the top-right / sidebar.
2. Select **Export to ZIP** (or push to GitHub and clone it).
3. Extract the ZIP file into a folder on your PC (e.g., `C:\Projects\SyncCut` or `~/projects/SyncCut`).

---

### Step 2: Prerequisites on Your PC
Make sure you have the following installed on your computer:
1. **VS Code** ([Download](https://code.visualstudio.com/))
2. **Java JDK 17 or 21** ([Adoptium Temurin](https://adoptium.net/) or Oracle JDK)
3. **Android SDK & Command-line Tools** (or Android Studio)
   - Set environment variable: `ANDROID_HOME` pointing to your Android SDK folder (e.g. `C:\Users\<YourUser>\AppData\Local\Android\Sdk` on Windows or `~/Library/Android/sdk` on macOS).

---

### Step 3: Open in VS Code
1. Open **VS Code**.
2. Click **File -> Open Folder...** and select the project root folder.
3. VS Code will detect `.vscode/extensions.json` and recommend the **Kotlin** and **Java** extensions. Click **Install**.

---

### Step 4: Build the APK in VS Code
You can build the APK directly using the built-in VS Code task or terminal:

#### Option A: 1-Click VS Code Task
- Press `Ctrl + Shift + B` (Windows/Linux) or `Cmd + Shift + B` (Mac).
- VS Code will execute **Build Debug APK**.

#### Option B: Terminal
Open the VS Code integrated terminal (`Ctrl + ` `~`):
- **On Windows (PowerShell / CMD):**
  ```bash
  .\gradlew.bat assembleDebug
  ```
- **On macOS / Linux:**
  ```bash
  ./gradlew assembleDebug
  ```

Once built, your installable APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

### Step 5: Install Directly to Your Phone from VS Code
1. Enable **Developer Options** and **USB Debugging** on your Android / Samsung phone.
2. Connect your phone to your PC via USB.
3. In the VS Code terminal, run:
   - Windows:
     ```bash
     .\gradlew.bat installDebug
     ```
   - Mac/Linux:
     ```bash
     ./gradlew installDebug
     ```
   *(Or press `Ctrl+Shift+P` -> `Tasks: Run Task` -> `Install Debug APK to connected phone`)*

The app will install and open immediately on your physical phone!

---

## 📁 Key Files & Codebase Map

| File / Directory | What it does |
|---|---|
| `.vscode/tasks.json` | 1-click build & install tasks for VS Code |
| `app/src/main/java/com/example/ui/screens/EditorScreen.kt` | Studio canvas preview, draggable text, timeline, zoom slider, export modal |
| `app/src/main/java/com/example/ui/screens/HomeScreen.kt` | Project manager, recent projects list, import/export buttons |
| `app/src/main/java/com/example/ui/components/TimelineTrack.kt` | Multi-track timeline, standard red playhead, video scenes, captions, audio |
| `app/src/main/java/com/example/ui/components/AudioVolumeDialog.kt` | Simplified audio menu (Volume booster slider & Delete) |
| `app/src/main/java/com/example/video/VideoRenderEffects.kt` | Canvas rendering (`BoxFit.contain`), transitions, text effects, filters |
| `app/src/main/java/com/example/video/HardwareVideoEncoder.kt` | Exynos MediaCodec MP4 hardware encoder & MediaStore gallery saver |
| `app/src/main/java/com/example/audio/AudioPlaybackManager.kt` | In-editor multi-track audio playback & scrubbing synchronization |
| `app/src/main/java/com/example/model/TextSyncModels.kt` | Subtitle styles, solid white fill with black outline presets |
| `app/src/main/java/com/example/data/ProjectManager.kt` | JSON project serialization & persistence |
| `FacelessVideoEditor_SamsungA53.apk` | Ready-to-install standalone APK |
| `FacelessVideoEditor_SamsungA53.zip` | Complete bundle with APK, README, and sync JSON configs |
