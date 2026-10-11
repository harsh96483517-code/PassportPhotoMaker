# Passport Photo Maker & Print

An Android app for making ID / passport / visa photos and printing several copies
on a single sheet. Built with **Kotlin + Jetpack Compose** and a premium
"Liquid Glass" inspired interface (frosted translucent cards, blue gradients,
rounded corners, spring animations, and a floating glass bottom navigation).

All image processing happens **on the device** — photos are never uploaded to any server.

> **Compliance note:** This app does **not** claim official compliance with any
> document authority's requirements. The size presets follow widely published
> conventions; always confirm the current requirements with the issuing authority
> before submitting a photo.

---

## Features

- **Onboarding** screen introducing the app.
- **Home dashboard** with *Take Photo*, *Choose from Gallery*, popular sizes, and quick tools.
- **Camera capture** using CameraX, with a face-alignment guide overlay.
- **Photo editor** with:
  - Crop (pinch to zoom, drag to reposition) locked to the chosen ID size,
  - Rotate and flip,
  - Brightness / contrast / saturation adjustments (live preview),
  - Background removal + solid background colour replacement.
- **ID photo size selection** — Passport (51×51 mm), Visa (35×45 mm), Driving
  Licence (30×40 mm), PAN (20×20 mm), Aadhaar (35×45 mm), Student ID (25×30 mm),
  plus a **custom size**.
- **Preview** of the finished, correctly sized photo.
- **Print studio** — lays out as many copies as fit on **4R (4×6")**, **A4**, or
  **Letter** at selectable DPI (Standard / High / Ultra), colour or grayscale:
  - **Print Now** through the Android print framework, or
  - **Save as PDF** for printing later at a studio or home printer.
- **My Photos** gallery with category filters, share, and delete.
- **Settings** — light/dark/system theme, default paper, default quality, face guide toggle.
- Photos are stored **locally** (app-private storage). Nothing leaves the device.

---

## Tech stack

| Area | Choice |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Build | Android Gradle Plugin 8.7.3, Gradle Kotlin DSL, Gradle 8.9 |
| Camera | CameraX 1.4.0 |
| Navigation | Navigation Compose |
| Images | Coil, Android Bitmap / Canvas APIs |
| Preferences | DataStore (Preferences) |
| Printing | AndroidX PrintHelper + `android.graphics.pdf.PdfDocument` |
| CI | GitHub Actions |

- **compileSdk / targetSdk:** 35
- **minSdk:** 24
- **Package name:** `com.passportphotomaker.print`

---

## Project structure

```
.
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/
├── .github/workflows/build-apk.yml
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/passportphotomaker/print/
        │   ├── MainActivity.kt
        │   ├── PassportApp.kt
        │   ├── data/          # models, local repository, settings
        │   ├── navigation/    # NavHost + routes
        │   ├── ui/components/ # glass UI building blocks
        │   ├── ui/screens/    # onboarding, home, camera, editor, sizes, preview, print, gallery, settings
        │   ├── ui/theme/      # colours, typography, shapes, theme
        │   ├── util/          # image processing, background remover, print layout, PDF export
        │   └── viewmodel/     # shared AppViewModel
        └── res/               # strings, themes, icons, file paths
```

---

## Build & run in Android Studio

1. Install **Android Studio** (Ladybug or newer) with **JDK 17**.
2. `File → Open…` and select the project root (the folder containing `settings.gradle.kts`).
3. Let Gradle sync (it will download dependencies on first run).
4. Connect a device or start an emulator (API 24+), then press **Run ▶**.

From the command line you can also run:

```bash
./gradlew assembleDebug      # build a debug APK
./gradlew installDebug       # build + install on a connected device
```

The debug APK is written to:

```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Build the APK automatically with GitHub Actions

A workflow at `.github/workflows/build-apk.yml` builds the APK for you.

1. Push the project to a GitHub repository (the `main` branch).
2. The workflow runs automatically on every push to `main` (and can be started
   manually from the **Actions** tab → **Build APK** → **Run workflow**).
3. When it finishes, open the run and download the **`app-debug-apk`** artifact
   from the **Artifacts** section — that ZIP contains `app-debug.apk`.
4. Transfer the APK to your phone and install it (enable "Install unknown apps"
   for your file manager / browser if prompted).

No signing keys or secrets are used — the workflow produces an unsigned **debug**
APK, which is fine for sideloading and testing. For a Play Store release you would
add your own signing configuration and use `assembleRelease`.

---

## Honest limitations

- **Background removal** uses a colour-distance (chroma-key style) algorithm. It
  works well for photos taken against a plain, evenly lit wall but will not cleanly
  cut a subject out of a busy or multi-coloured background. For those, use the solid
  background colour option, or capture a new photo against a plain wall.
- **Face detection / auto-alignment** is not implemented — the camera shows a manual
  alignment guide only. Automatic face detection would require an ML Kit dependency.
- Sizes are **presets based on common conventions** and are not guaranteed to match
  the latest official specifications of any authority.
- The app produces a **debug APK** via CI; release signing is left to you.

---

## Privacy

All photos are processed and stored locally in the app's private storage. The app
has no network permission and does not upload any data.
