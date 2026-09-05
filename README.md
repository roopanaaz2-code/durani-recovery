# DURANI RECOVERY — Android Forensic Media Recovery

A transparent, zero-simulation forensic media recovery and deleted file carving application built with Kotlin and Jetpack Compose.

---

## 🚀 How to Download the APK via GitHub Actions

A continuous integration workflow is configured in `.github/workflows/android.yml`.

### Step 1: Push to GitHub or Run Manually
Whenever you push code to GitHub:
1. Go to your repository on **GitHub**.
2. Click on the **Actions** tab at the top.
3. You will see the workflow: **"Build Durani Recovery APK"**.
   - If you want to trigger it manually without pushing, click on **Build Durani Recovery APK** in the left sidebar, click **Run workflow**, and select your branch.

### Step 2: Download Your Built APK
1. Click on the latest workflow run.
2. Scroll down to the **Artifacts** section at the bottom of the summary page.
3. Click on **`Durani-Recovery-Debug-APK`** to download the ZIP file containing your installable `.apk`.
4. Extract the ZIP and install the `.apk` on your Android phone or tablet.

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
