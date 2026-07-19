# Awan Aaram ☁️⚡

**Awan Aaram** ("Cloud Comfort") is a modern, highly polished, and offline-first Android application designed to help homeowners track domestic utilities, simulate appliance energy consumption, and estimate electricity bills with local tariff policies.

Built using **Kotlin, Jetpack Compose, and Room Database**, Awan Aaram combines a stunning Material 3 interface with professional-grade local utility engineering.

---

## 🎨 Visual Identity & Theme
Awan Aaram employs an eye-safe, premium **Energy Savings & Cloud Comfort** theme:
*   **Color Palette**: Primary deep Teal (`#008577`), light Teal (`#00BFA5`), with modern Emerald (`#2ECC71`), Amber (`#F1C40F`), and soft Crimson (`#E74C3C`) accent alerts.
*   **Responsive Layouts**: Designed around generous Material 3 spacing guidelines, responsive negative space, custom visual gauges, and progress bars.

---

## ✨ Features

### 1. Domestic Bill Estimator (Calculator)
*   **Dual-Policy Calculation**: Estimate costs using either standard flat-rate pricing or a custom local **domestic tiered slab** policy (e.g., 0-100 units @ \$0.10, next 200 units @ \$0.15, etc.).
*   **Live Metrics**: Watch base energy costs, taxes/surcharges, and total estimated bills update in real-time as you type your previous and current meter readings.
*   **Visual Gauge**: Displays a color-coded gauge (Green, Yellow, Red) based on consumption thresholds to quickly rate energy usage.

### 2. Appliance Consumption Simulator
*   **Inventory Database**: Manage a detailed local database of domestic electronic appliances (name, wattage, daily hours of operation, and quantity).
*   **Dynamic Usage Bars**: Each appliance calculates its monthly consumption in kWh, showing its exact **percentage share** of overall household power consumption.
*   **Pre-populated defaults**: Instantly populates standard household appliances (AC, Refrigerator, LED light bulbs, TVs) on first-launch to keep onboarding seamless.

### 3. Billing History & Breakdown Log
*   **Room Database Integration**: Save estimated bills to local SQLite storage with custom metadata notes.
*   **Interactive Modal Details**: Tap any saved record to display a gorgeous, precise financial breakdown.
*   **Manage Logs**: Hold or click to safely delete historical logs once they are settled.

---

## 🏗️ Architecture & Stack

Awan Aaram follows strict **Modern Android Architecture (MVVM)** guidelines:

```
com.example
├── MainActivity.kt          # Single-Activity compose container hosting tabs and navigation
├── data
│   ├── Appliance.kt         # Room Entity for simulated appliances
│   ├── BillRecord.kt        # Room Entity for historical utility bills
│   ├── AppDao.kt            # Room Data Access Object defining SQL queries
│   ├── AppDatabase.kt       # Room Database builder with destructive migration fallback
│   └── AppRepository.kt     # Clean repository layer abstracting local data tasks
└── ui
    ├── BillViewModel.kt     # Core ViewModel managing calculation logic and state flows
    └── theme
        ├── Color.kt         # Centralized green-teal energy design palette
        ├── Theme.kt         # Material Theme builder with dark and light variants
        └── Type.kt          # Typographical configurations
```

### Core Technologies:
*   **UI Framework**: Jetpack Compose (Declarative UI)
*   **Design System**: Material Design 3 (M3)
*   **Database**: Room Database (SQLite abstraction)
*   **Asynchronous Flows**: Kotlin Coroutines & StateFlow

---

## 🚀 Setting Up & Running

### Building the Project
Import this folder into Android Studio:
1.  Verify that your Gradle Version corresponds to Java 17+.
2.  Press **Sync Project with Gradle Files**.
3.  Click **Run 'app'** to launch Awan Aaram in your emulator or physical Android device.

---

## 🧪 Testing

Awan Aaram contains local JVM tests to verify correctness without requiring a live emulator:
*   **Robolectric**: Standard tests to verify strings, states, and business logic.
*   **Roborazzi**: Screenshot regression testing to verify visual integrity on a virtual Pixel device.

Run the test suite using Gradle:
```bash
# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Verify UI layouts using Roborazzi screenshots
gradle :app:verifyRoborazziDebug
```

---

*Enjoy Cloud Comfort and Smart Savings with Awan Aaram!* ☁️⚡
