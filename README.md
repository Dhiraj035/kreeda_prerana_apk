# 🏅 Kreeda-Prerana Scout

**A modern Android sports coaching and athlete performance management application.**

Kreeda-Prerana Scout empowers coaches to discover, track, and develop athletic talent through a professional analytics-driven platform. Built with Kotlin, Jetpack Compose, and Firebase — designed for real-world grassroots sports scouting.

---

## ✨ Features

### 🔐 Authentication
- Email/password login and signup via Firebase Auth
- Coach-specific data isolation — each coach sees only their own athletes
- Animated splash screen with auto-login

### 🏃 Athlete Management
- **Add Athletes** — Register athletes with name, age, gender, sport, and school
- **Athlete List** — Searchable, filterable list with sport-based filter chips (11 sports supported)
- **Athlete Profile** — Detailed profile with performance timeline, line graph analytics, and quick stats
- **Athlete Stats** — Full analytics dashboard with improvement analysis, line graphs, and complete trial history

### ⏱️ Trial Logger
- **Stopwatch Mode** — Built-in animated stopwatch for timed events (sprints, swimming, etc.)
- **Manual Entry Mode** — Dynamic form for field events with sport-specific input fields:
  - Long Jump → Jump Distance (m)
  - High Jump → Jump Height (m)
  - Shot Put / Discus / Javelin → Throw Distance (m)
  - Triple Jump, Pole Vault, and more
- Automatic badge evaluation after each trial
- Toast confirmation + instant redirect to Home on save

### 🏆 Leaderboard
- **Sport-specific rankings** — separate leaderboard for each event
- **Smart ranking logic:**
  - Timer-based sports → lowest time wins
  - Distance/height sports → highest value wins
- Top 3 podium display with Gold/Silver/Bronze styling
- Dynamic filter chips generated from real performance data
- Trend indicators (↑ improvement / ↓ decline)

### 📊 Analytics
- Team-wide analytics dashboard
- Performance distribution charts
- Activity breakdown visualization

### 🏅 Badges
- Automatic badge evaluation based on performance milestones
- Badge display per athlete

---

## 🏗️ Architecture

```
MVVM (Model - View - ViewModel)
├── Data Layer
│   ├── Models (Athlete, Performance, Badge, Coach)
│   └── Repositories (Firestore CRUD + real-time flows)
├── ViewModel Layer
│   ├── AuthViewModel
│   ├── AthleteViewModel
│   ├── AthleteProfileViewModel
│   ├── TrialLoggerViewModel
│   ├── LeaderboardViewModel
│   ├── AnalyticsViewModel
│   ├── DashboardViewModel
│   └── BadgesViewModel
└── UI Layer (Jetpack Compose + Material 3)
    ├── Screens (13 screens)
    ├── Navigation (Navigation Compose)
    └── Theme (Custom Blue/Orange sports theme)
```

---

## 📁 Project Structure

```
app/src/main/java/com/example/kreedaprerana/
├── MainActivity.kt                  # Entry point, bottom nav, navigation host
├── data/
│   ├── model/
│   │   ├── Athlete.kt              # Athlete data model
│   │   ├── Performance.kt          # Trial/performance record model
│   │   ├── Badge.kt                # Achievement badge model
│   │   └── Coach.kt                # Coach profile model
│   └── repository/
│       ├── AthleteRepository.kt    # Athlete CRUD operations
│       ├── PerformanceRepository.kt # Performance logging + queries
│       ├── BadgeRepository.kt      # Badge management
│       └── AuthRepository.kt       # Firebase Auth operations
├── viewmodel/
│   ├── AuthViewModel.kt            # Login/signup state
│   ├── AthleteViewModel.kt         # Athlete list management
│   ├── AthleteProfileViewModel.kt  # Individual athlete data
│   ├── TrialLoggerViewModel.kt     # Stopwatch + trial logging
│   ├── LeaderboardViewModel.kt     # Sport-specific rankings
│   ├── AnalyticsViewModel.kt       # Team analytics
│   ├── DashboardViewModel.kt       # Home screen stats
│   └── BadgesViewModel.kt          # Badge display
├── navigation/
│   └── Navigation.kt               # Routes + NavHost setup
├── ui/
│   ├── screens/
│   │   ├── SplashScreen.kt         # Animated splash
│   │   ├── LoginScreen.kt          # Authentication
│   │   ├── SignupScreen.kt         # Registration
│   │   ├── HomeScreen.kt           # Dashboard
│   │   ├── AddAthleteScreen.kt     # Athlete registration form
│   │   ├── AthleteListScreen.kt    # Filterable athlete directory
│   │   ├── AthleteProfileScreen.kt # Athlete detail + line graph
│   │   ├── AthleteStatsScreen.kt   # Full analytics dashboard
│   │   ├── TrialLoggerScreen.kt    # Stopwatch + manual entry
│   │   ├── LeaderboardScreen.kt    # Sport-wise rankings
│   │   ├── AnalyticsScreen.kt      # Team analytics
│   │   ├── BadgesScreen.kt         # Achievement badges
│   │   └── ProfileSettingsScreen.kt # Coach profile settings
│   └── theme/
│       ├── Color.kt                # Color palette
│       ├── Theme.kt                # Material 3 theme
│       └── Type.kt                 # Typography
└── util/
    ├── StopwatchManager.kt         # Coroutine-based stopwatch
    └── BadgeEvaluator.kt           # Badge eligibility logic
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Design System** | Material 3 (Material You) |
| **Navigation** | Navigation Compose |
| **Backend** | Firebase Firestore (real-time) |
| **Authentication** | Firebase Auth |
| **Architecture** | MVVM + Repository Pattern |
| **Async** | Kotlin Coroutines + StateFlow |
| **Build System** | Gradle (Kotlin DSL) |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 17+
- Firebase project with Firestore and Authentication enabled

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/kreeda-prerana.git
   cd kreeda-prerana
   ```

2. **Add Firebase config**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a project and add an Android app with package name `com.example.kreedaprerana`
   - Download `google-services.json` and place it in `app/`

3. **Enable Firebase services**
   - Authentication → Enable Email/Password sign-in
   - Firestore Database → Create in test mode or apply security rules from `firestore.rules`

4. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open in Android Studio and click **Run**.

---

## 🔒 Firebase Security

Data isolation is enforced via `coachId` ownership:
- Each athlete and performance record is tagged with the coach's Firebase UID
- Firestore queries filter by `coachId` — coaches see only their own data
- Security rules in `firestore.rules` enforce read/write access per user

---

## 🎨 Design

- **Theme**: Custom Blue (#2563EB) and Orange (#F97316) sports palette
- **Typography**: Material 3 type scale
- **Cards**: Rounded 14–16dp corners with subtle elevation
- **Charts**: Custom Canvas-based line graphs (no third-party chart library)
- **Animations**: Stopwatch ring animation, smooth transitions

---

## 📱 Screens Overview

| Screen | Description |
|--------|------------|
| Splash | Animated brand intro with auto-login check |
| Login / Signup | Firebase email authentication |
| Home | Dashboard with quick stats and navigation cards |
| Add Athlete | Registration form with sport/gender dropdowns |
| Athlete List | Searchable list with 11 sport filter chips |
| Athlete Profile | Detail view with line graph + performance timeline |
| Athlete Stats | Full analytics: improvement %, line graphs, trial history |
| Trial Logger | Stopwatch mode + Manual Entry mode for field events |
| Leaderboard | Sport-specific rankings with podium + trend indicators |
| Analytics | Team-wide performance distribution |
| Badges | Achievement tracking |
| Profile Settings | Coach profile management |

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  <b>Kreeda-Prerana Scout</b> — Empowering grassroots sports coaching through technology.
</p>
