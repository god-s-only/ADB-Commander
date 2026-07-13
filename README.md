# ADB Commander

> Your Android device is now its own ADB workstation.

ADB Commander eliminates the laptop from your Android debugging workflow. Using Shizuku for privileged shell access, it runs ADB operations directly on the device — reading logcat, killing processes, inspecting apps, capturing screenshots, monitoring CPU and RAM — all without a USB cable or a PC.

---

## Screenshots

### Home
<p align="center">
  <img src="screenshots/home.png" width="220"/>
</p>

### App Manager
<p align="center">
  <img src="screenshots/app_manager.png" width="220"/>
</p>

### Logcat
<p align="center">
  <img src="screenshots/logcat.png" width="220"/>
</p>

### ADB Commands
<p align="center">
  <img src="screenshots/adb_commands.png" width="220"/>
</p>

### App Inspector
<p align="center">
  <img src="screenshots/app_inspector.png" width="220"/>
</p>

### Process Monitor
<p align="center">
  <img src="screenshots/process_monitor.png" width="220"/>
</p>

### Intent Sender
<p align="center">
  <img src="screenshots/intent_sender.png" width="220"/>
</p>

---

## Features
┌─────────────────────────────────────────────────────────────────────┐
│  Home                                                               │
│  ─────────────────────────────────────────────────────────────────  │
│  Auto-detects device IP, ADB port, and pairing port via Shizuku    │
│  Reads pairing code from system settings — no manual entry needed  │
│  Builds adb pair and adb connect commands ready to copy             │
│  One-tap copy for both commands                                     │
│  Test Connection pings 8.8.8.8 for network verification            │
│  Shizuku status card with live permission state                     │
│  Quick access to Capture, Processes, and Intent Sender tools        │
├─────────────────────────────────────────────────────────────────────┤
│  ADB Commands                                                       │
│  ─────────────────────────────────────────────────────────────────  │
│  28+ commands across 5 categories                                   │
│    Connection      adb pair, adb connect, adb disconnect            │
│    App Management  install, uninstall, clear, force-stop            │
│    Device/System   getprop, reboot, wm density, dumpsys             │
│    Capture         screencap, screenrecord                          │
│    Logs            logcat with filters                              │
│  Commands built dynamically from your device's actual IP/ports     │
│  Live search across title, command text, and hints                  │
│  Collapsible categories with command count badges                   │
│  One-tap copy with 1.5s confirmation flash                          │
│  Edit-needed badge on commands requiring custom input               │
├─────────────────────────────────────────────────────────────────────┤
│  App Manager                                                        │
│  ─────────────────────────────────────────────────────────────────  │
│  Lists all installed apps with correct system/user filtering        │
│  Toggle to include system apps                                      │
│  Search by app name or package name                                 │
│  Per-app actions via bottom sheet:                                  │
│    Launch      opens the app normally                               │
│    Kill        am force-stop via Shizuku                            │
│    Clear Data  pm clear via Shizuku                                 │
│    Extract APK copies APK to Downloads/ExtractedAPKs               │
│    Uninstall   pm uninstall via Shizuku                             │
│    Inspect     opens full App Inspector                             │
├─────────────────────────────────────────────────────────────────────┤
│  Device Info                                                        │
│  ─────────────────────────────────────────────────────────────────  │
│  System    model, manufacturer, Android version, API, build,        │
│            security patch date                                      │
│  Hardware  screen size, density, CPU ABI, total RAM                 │
│  Battery   live progress bar, health, temperature, voltage          │
│  Network   IP address, Wi-Fi state                                  │
│  Copy Device Profile exports everything as shareable plain text     │
│  Shimmer loading skeleton on first load                             │
├─────────────────────────────────────────────────────────────────────┤
│  Logcat                                                             │
│  ─────────────────────────────────────────────────────────────────  │
│  Live streaming via Kotlin Flow — process never exits               │
│  Level filter chips (V/D/I/W/E/F/S) applied as logcat flags        │
│  Tag filter applied server-side — restarts process with new flags   │
│  Text search applied client-side — no process restart               │
│  2000-line rolling buffer prevents OOM on long sessions             │
│  Auto-scroll toggle that follows new lines                          │
│  Save to file — timestamped .txt in Downloads/AdbCommander         │
│  Clear buffer and reset line counter                                │
│  Color-coded level badges per line                                  │
├─────────────────────────────────────────────────────────────────────┤
│  Capture                                                            │
│  ─────────────────────────────────────────────────────────────────  │
│  Screenshot via screencap — decoded in-app for instant preview      │
│  Save to gallery or share via system share sheet                    │
│  Screen recording via screenrecord with live elapsed timer          │
│  Pulsing REC dot animation during active recording                  │
│  SIGINT sent on stop to finalize MP4 cleanly before destroy()       │
│  Recordings saved to Movies/AdbCommander                           │
├─────────────────────────────────────────────────────────────────────┤
│  App Inspector                                                      │
│  ─────────────────────────────────────────────────────────────────  │
│  Full PackageManager inspection — no Shizuku or root needed         │
│  9 accordion sections:                                              │
│    Identity      package, version name/code, install/update dates   │
│    Build Info    target SDK, min SDK, compile SDK, debuggable flag  │
│    Storage       APK size, APK path, data directory                 │
│    Signing       certificate subject DN and SHA-256 fingerprint     │
│    Activities    all declared activities with exported badge         │
│    Services      all declared services with exported badge          │
│    Receivers     all broadcast receivers with exported badge        │
│    Providers     all content providers with exported badge          │
│    Permissions   split into Granted and Denied with icons           │
│  Copy icon on every single value                                    │
│  DEBUG badge on header if app is debuggable                         │
├─────────────────────────────────────────────────────────────────────┤
│  Process Monitor                                                     │
│  ─────────────────────────────────────────────────────────────────  │
│  Live CPU% and RAM per process updated every 1.5 seconds           │
│  CPU calculated from /proc/<pid>/stat tick deltas                   │
│  RAM read from VmRSS in /proc/<pid>/status                         │
│  Color-coded CPU bars — green, amber, red by threshold              │
│  User-only toggle hides system processes                            │
│  Search filter by process name                                      │
│  Sorted by CPU usage descending                                     │
├─────────────────────────────────────────────────────────────────────┤
│  Intent Sender                                                      │
│  ─────────────────────────────────────────────────────────────────  │
│  Build and fire any Android intent from a form UI                   │
│  Fields: Action, Data URI, Package, Class, Extras                   │
│  Extra types: String, Int, Boolean, Long, Float                     │
│  Quick-pick chips for 12 common actions                             │
│  Fires via context.startActivity() with live success/error result   │
│  Reset button clears the entire form                                │
├─────────────────────────────────────────────────────────────────────┤
│  Settings                                                           │
│  ─────────────────────────────────────────────────────────────────  │
│  Shizuku status card — running, permission needed, or not running   │
│  Grant Shizuku permission button                                    │
│  Current plan badge (Free / Pro)                                    │
│  Upgrade to Pro navigates to Paywall                                │
│  Restore Purchase re-verifies with Stripe server                    │
│  Rate on Play Store, Privacy Policy, Contact Support, GitHub        │
│  App version display                                                │
└─────────────────────────────────────────────────────────────────────┘

---

## Free vs Pro
Free                              Pro
────────────────────────────────  ────────────────────────────────────
View device IP and ports          Force Stop any app
Copy ADB commands                 Clear app data
Browse installed apps             Extract APK to Downloads
Launch any app                    Silent uninstall via shell
Stream logcat (basic)             Save logcat to file
View device info                  Logcat tag filter
Test network connection           Logcat level filter
Screenshot and share              Copy device profile
Record screen                     Shell terminal access
Fire intents
Inspect any app
Monitor processes

Pro is a one-time purchase. Payment is processed by Stripe and verified server-side before access is granted — the client-side PaymentSheet result alone never unlocks Pro.

---

## Tech Stack
Language            Kotlin 2.2.0
UI                  Jetpack Compose + Material 3
Architecture        MVVM + Clean Architecture
Domain / Data / Presentation layers
Dependency Injection Hilt + KSP 2.2.0-2.0.2
Navigation          Navigation Compose
Shell Access        Shizuku 13.1.5
Reflection on private newProcess() API
Async               Kotlin Coroutines + Flow
Persistence         DataStore Preferences
Payments            Stripe Android SDK 23.6.0
Backend             Node.js + Express deployed on Railway
Build System        AGP 8.13.2
Compose BOM         2025.05.00
Min SDK             26 (Android 8.0 Oreo)
Target SDK          36

---

## Project Structure
app/
├── core/
│   ├── Routes.kt                       Navigation route constants
│   ├── CaptureCommands.kt              Shell command builders for capture
│   └── ShizukuShellExecutor.kt         Reflection-based shell runner
│
├── data/
│   └── repository/
│       ├── AppInspectorRepositoryImpl.kt
│       ├── CaptureRepositoryImpl.kt
│       ├── LogcatRepositoryImpl.kt
│       ├── ProcessMonitorRepositoryImpl.kt
│       └── StripeBillingRepositoryImpl.kt
│
├── domain/
│   ├── billing/
│   │   ├── Feature.kt                  Feature enum with FREE/PRO sets
│   │   ├── FeatureManager.kt           Central feature gate
│   │   └── UserEntitlement.kt          Plan state model
│   ├── models/                         All domain data classes
│   ├── repository/                     Repository interfaces
│   └── usecase/                        One class per use case
│
└── presentation/
├── ui/
│   ├── components/
│   │   └── BottomNavItem.kt
│   └── features/
│       ├── home/
│       ├── appmanager/
│       ├── deviceinfo/
│       ├── logcat/
│       ├── capture/
│       ├── inspector/
│       ├── processmonitor/
│       ├── intentsender/
│       ├── settings/
│       └── paywall/
└── MainActivity.kt

---

## Shizuku Setup

ADB Commander uses [Shizuku](https://github.com/RikkaApps/Shizuku) to execute commands as the `shell` user. This is what enables `am force-stop`, `pm clear`, `screencap`, `/proc` filesystem reads, and system settings access — all on-device.

### Android 11 and above — no PC needed

Install Shizuku from the Play Store
Open Shizuku
Tap "Start via Wireless Debugging"
Open ADB Commander and grant the permission prompt


Shizuku restarts automatically on every reboot using Wireless Debugging. No PC ever needed after this.

### Android 10 and below — one-time setup

Connect phone to a PC once via USB
Run:  adb shell sh /sdcard/shizuku/start.sh
Disconnect the cable


Shizuku stays active until the device reboots. After a reboot, the command needs to be run again — or upgrade to Android 11.

---

## Payment Backend

A minimal Node.js + Express server handles Stripe operations. It is the only place the Stripe secret key lives.
POST /create-payment-intent   Creates PaymentIntent, returns client_secret
POST /verify-purchase         Verifies payment status directly with Stripe API
POST /webhook                 Handles Stripe server-side payment events
GET  /health                  Health check endpoint

### Required environment variables

```env
STRIPE_SECRET_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
PORT=3000
```

### Deploy to Railway

```bash
# 1. Create a GitHub repo with server.js, package.json, .gitignore
# 2. Go to railway.app → New Project → Deploy from GitHub
# 3. Add environment variables in the Railway dashboard
# 4. Copy the generated domain URL
```

---

## Configuration

Two values need to be set before building:

```kotlin
// StripeBillingRepositoryImpl.kt — companion object
private const val BASE_URL = "https://your-railway-url.up.railway.app"

// PaywallScreen.kt
private const val STRIPE_PUBLISHABLE_KEY = "pk_live_your_key_here"
```

---

## Build Setup

**`libs.versions.toml`**

```toml
[versions]
agp = "8.13.2"
kotlin = "2.2.0"
ksp = "2.2.0-2.0.2"
composeBom = "2025.05.00"
hilt = "2.56.2"
stripe = "23.6.0"
shizuku = "13.1.5"
datastore = "1.1.1"
navigationCompose = "2.9.0"
```

---

## Running the Project

```bash
git clone https://github.com/god-s-only/adb-commander
cd adb-commander
```

Open in Android Studio Hedgehog or later. Sync Gradle. Run on a **physical device** — Shizuku does not work on emulators.

---

## Adding Screenshots

Take screenshots of each screen on a real device and place them in a `screenshots/` folder at the root of the repository:
screenshots/
├── home.png
├── adb_commands.png
├── app_manager.png
├── device_info.png
├── logcat.png
├── capture.png
├── app_inspector.png
├── process_monitor.png
└── intent_sender.png

The README references them automatically once the files exist.

---

## License
MIT License
Copyright (c) 2025 Oneshioze
Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:
The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

---

## Author

**Oneshioze** — Native Android Developer
GitHub: [@god-s-only](https://github.com/god-s-only)
