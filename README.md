A lightweight, privacy-first Android app locker. Protect any app on your phone with a **4-digit PIN** or **pattern** lock — no internet, no tracking, no nonsense.

---

## ✨ Features

- 🔐 **PIN or Pattern lock** — choose your preferred unlock method
- 📱 **Lock any installed app** — toggle per-app locking from a simple list
- 🚀 **Works after reboot** — protection stays active without relaunching the app
- 🌐 **Zero internet access** — the app physically cannot go online (no `INTERNET` permission)
- 🛡️ **Secure screen flag** — prevents screenshots of the lock screen
- ⚡ **Event-driven, not polling** — no battery drain from background timers

---

## 📸 Screenshots

> Lock screen keeps your chosen apps safe with PIN or pattern.

---

## 📦 Installation

### Option 1 — Download from GitHub Releases *(Recommended)*

1. Go to the [**Releases**](../../releases) page
2. Download the latest `AppLock-release.apk`
3. On your Android phone, go to **Settings → Security** and enable **Install from unknown sources**
4. Open the downloaded APK and tap **Install**
5. Open AppLock, set your PIN or pattern, then enable the **Accessibility Service** when prompted

### Option 2 — Build from Source

```bash
git clone https://github.com/YOUR_USERNAME/AppLock.git
cd AppLock
./gradlew assembleRelease
```

The signed APK will be at:
```
app/build/outputs/apk/release/app-release.apk
```

---

## 🔧 How It Works

AppLock uses Android's **Accessibility Service** API to detect when a locked app is brought to the foreground. When that happens, it instantly shows a lock screen — no polling, no background timers.

```
User opens locked app
        ↓
AccessibilityService fires (typeWindowStateChanged)
        ↓
LockActivity shown (PIN / Pattern challenge)
        ↓
Correct? → App opens    Wrong? → Stay locked
```

- **PIN** is stored as a salted SHA-256 hash — never in plain text
- **Pattern** is hashed the same way
- All data stays on-device in `SharedPreferences`

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Min SDK | Android 7.0 (API 24) |
| Target SDK | Android 14 (API 34) |
| Auth storage | SHA-256 + random salt |
| Lock trigger | AccessibilityService |
| Build | Gradle + GitHub Actions CI |

---

## 🔒 Permissions

| Permission | Why |
|---|---|
| `BIND_ACCESSIBILITY_SERVICE` | Detects which app is in the foreground |
| `RECEIVE_BOOT_COMPLETED` | Reloads locked apps after reboot |

> ❌ **No INTERNET permission** — this app cannot make network requests.

---

## 🤝 Contributing

Pull requests are welcome! Please open an issue first to discuss what you'd like to change.

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

