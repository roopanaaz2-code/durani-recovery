# DURANI RECOVERY — Android Forensic Media Recovery

A transparent, zero-simulation forensic media recovery and deleted file carving application built with Kotlin and Jetpack Compose.

---

## 🚀 How to Download & Install the APK on Android

### Method 1: Direct APK Download via GitHub Releases (Recommended)
1. Go to: **[https://github.com/roopanaaz2-code/durani-recovery/releases](https://github.com/roopanaaz2-code/durani-recovery/releases)**
2. Under the latest release, click on **`durani-recovery.apk`**.
3. The APK downloads directly to your Android device (no ZIP extraction needed).
4. Tap the downloaded file to install!

---

### Method 2: Download from GitHub Actions
1. Open your workflow run: [Workflow Runs](https://github.com/roopanaaz2-code/durani-recovery/actions)
2. Scroll to the **Artifacts** section at the bottom.
3. Tap **`Durani-Recovery-Debug-APK`** (downloads as a `.zip`).
4. On your phone, open your **Files** app, tap the `.zip` file, tap **Extract**, then tap **`app-debug.apk`** to install.

---

## 💻 Local Build Instructions

If you have cloned this repository to your local machine:

```bash
# Decode the debug keystore if not already present
base64 -d debug.keystore.base64 > debug.keystore

# Prepare environment file
cp .env.example .env

# Build the debug APK
./gradlew assembleDebug
```

The output APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```
