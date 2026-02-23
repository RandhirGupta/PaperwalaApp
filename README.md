# Paperwala

> Your morning paper, delivered digitally.

Paperwala is a curated morning news briefing app inspired by the **Indian newspaper wala** who delivers papers door-to-door every morning. Instead of an infinite feed, you get a single daily "edition" — curated to your interests, summarized by AI, and designed to feel like reading a real newspaper.

---

## The Concept

Every morning, a newspaper wala in India delivers your paper to your doorstep. You pick it up with your chai, flip through the sections you care about, and you're done. No infinite scroll. No algorithmic rabbit holes. Just your morning briefing.

**Paperwala** brings that experience to your phone:

- **Morning Edition** — A new "edition" is curated and ready each morning at your preferred time
- **Above the Fold** — Top 3 headlines presented like a real newspaper front page
- **Sections Unfold** — Scroll down and sections (Technology, Sports, Business...) unfold with a 3D newspaper animation
- **AI Summaries** — Each article is summarized to 2-3 sentences (Gemini cloud API or your choice of on-device LLM)
- **Your Interests** — News is scored and ranked based on topics you chose during onboarding
- **Reading Streaks** — Build a daily reading habit with streak tracking

---

## Tech Stack

| Layer | Technology |
| --- | --- |
| **Language** | Kotlin 2.1.10 |
| **Framework** | Kotlin Multiplatform (KMP) |
| **UI** | Compose Multiplatform 1.7.3 |
| **Platforms** | Android (API 26+) · iOS (15+) |
| **Networking** | Ktor 3.1.0 |
| **Database** | SQLDelight 2.0.2 |
| **DI** | Koin 4.0.2 |
| **Navigation** | Voyager 1.1.0 |
| **Images** | Coil 3.1.0 |
| **Cloud AI** | Gemini 2.0 Flash (free tier) |
| **Local LLM** | llama.cpp (Phi-3-mini, Llama 3.2, Gemma 2) |
| **Animations** | Compose Animation + Compottie |

---

## Development Roadmap

### [x] Phase 1: MVP Foundation

- Kotlin Multiplatform project setup (Android + iOS shared module)
- SQLDelight database schema (Articles, Editions, Reading History, User Preferences)
- 4 news API integrations (NewsAPI, GNews, Newsdata, RSS feeds)
- ArticleMapper with deduplication and category inference
- Onboarding flow (topic selection, source picker, reading time, delivery time)
- Splash screen with Voyager navigation

### [x] Phase 2: Morning Drop UI

- "Above the Fold" front-page layout with hero article + 2 secondary cards
- Section-based scrolling with 3D unfold animation (`rotationX` spring)
- Lottie "Morning Drop" delivery animation (Compottie)
- Multi-source RSS feed parsing (The Hindu, Indian Express, NDTV, BBC, Reuters)
- Background sync with WorkManager (Android) / BGTaskScheduler (iOS)
- Newspaper-inspired typography (Playfair Display, Lora, JetBrains Mono)

### [x] Phase 3: AI Summaries + Model Selection

- 3-tier AI fallback chain: Local LLM → Gemini Cloud → Rule-based
- Gemini 2.0 Flash cloud API integration (free tier, 1,500 req/day)
- Extractive rule-based summarizer with word-frequency scoring
- On-device LLM via llama.cpp (expect/actual for Android JNI + iOS cinterop)
- 4 user-selectable GGUF models (Phi-3-mini, Llama 3.2 3B, Llama 3.2 1B, Gemma 2 2B)
- Settings screen with model picker, download progress, and AI status indicator
- AI relevance scoring integrated into edition pipeline
- 95 unit tests across 10 test classes (kotlin-test + coroutines-test)

### [ ] Phase 4: Habits

- Reading streak tracking and streak dashboard
- Push notifications for morning edition delivery
- Article bookmarking and reading list
- Share articles to other apps

### [ ] Phase 5: Polish

- Dark mode and sepia reading mode
- Accessibility improvements (content descriptions, font scaling)
- Performance optimization and offline-first refinements
- App store release (Google Play + App Store)

---

## Getting Started

### Prerequisites

- Android Studio (Ladybug or newer)
- Xcode 15+ (for iOS)
- JDK 17+

### Setup

1. Clone the repo

   ```bash
   git clone https://github.com/RandhirGupta/PaperwalaApp.git
   ```

2. Open in Android Studio
3. Add your API keys in `shared/src/commonMain/kotlin/com/paperwala/util/Constants.kt`:

   ```kotlin
   const val NEWS_API_KEY = "your-newsapi-key"
   const val GNEWS_API_KEY = "your-gnews-key"
   const val GEMINI_API_KEY = "your-gemini-key"  // optional, for AI summaries
   ```

4. Sync Gradle and run on emulator/device

### API Keys (Free Tiers)

- [NewsAPI.org](https://newsapi.org/) — 1,000 calls/month
- [GNews.io](https://gnews.io/) — 100 requests/day
- [Newsdata.io](https://newsdata.io/) — 200 credits/day

---

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed diagrams.

---

## License

```
Copyright 2026 Randhir Gupta

Licensed under the Apache License, Version 2.0
```

See [LICENSE](LICENSE) for details.

---

Built with Kotlin Multiplatform + Compose Multiplatform
