# RedLight

**RedLight** is a minimalist Android app that displays a full-screen red screen with an adjustable brightness slider and an elegant close button.  
Designed for use as a non-intrusive night light, screen warmer, or for relaxation, it respects privacy and avoids any network access or background tracking.

By emitting red light only, it helps illuminate dark environments at night without disrupting the body’s natural melatonin production — a key factor in maintaining healthy circadian rhythms and falling back asleep more easily.  
This makes it especially useful during brief moments of nighttime wakefulness when one needs to navigate their surroundings without fully waking up or overstimulating the senses.  
It is also ideal for situations where one needs to quietly re-enter a shared bedroom — for instance, to join a sleeping partner — without waking them or straining their own eyes.

---

## 📱 Features

- 🔴 Full-screen red screen, no distractions
- 🌡️ Brightness slider with saved state
- ❌ Elegant “X” button to close the app
- 🛡️ No internet access, ads, or analytics
- 💾 All data stored locally (SharedPreferences)
- 📴 Runs without permissions (except optional vibrator)
- ☕ Lightweight and opens instantly

---

## 🛠️ Technical

- **Language:** Kotlin
- **Min SDK:** 31 (Android 12)
- **Target SDK:** 34+
- **No Compose or Jetpack dependencies**
- **No APIs, no cloud**

---

## 📦 Installation

You can build the APK from source using Android Studio (recommended) or download the latest prebuilt APK (e.g. `RedLight_v0.10.1.apk`) from the [Releases](https://github.com/solostudiosss/RedLight/releases) section.


To sideload:
1. Enable *Install from unknown sources* on your Android device.
2. Transfer and open the APK file.

---

## 🚫 Permissions

The app requires **no permissions** by default.  
If vibration is enabled in a future release, it may request `VIBRATE`, but this is optional.

---

## ☕ Author

Made with obsessive minimalism and night-time utility in mind.

**Eugene Gutin**  
📧 solostudios@gmail.com  
🧠 [Licensed under the MIT License](./LICENSE)

---

## 📚 License

This project is licensed under the MIT License — see the [LICENSE](./LICENSE) file for details.
