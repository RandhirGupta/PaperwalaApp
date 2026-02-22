# Paperwala

> Your morning paper, delivered digitally.

Paperwala is a curated morning news briefing app inspired by the **Indian newspaper wala** who delivers papers door-to-door every morning. Instead of an infinite feed, you get a single daily "edition" — curated to your interests, summarized by AI, and designed to feel like reading a real newspaper.

---

## Screenshots

> *Coming soon — Phase 1 MVP in development*

---

## The Concept

Every morning, a newspaper wala in India delivers your paper to your doorstep. You pick it up with your chai, flip through the sections you care about, and you're done. No infinite scroll. No algorithmic rabbit holes. Just your morning briefing.

**Paperwala** brings that experience to your phone:

- **Morning Edition** — A new "edition" is curated and ready each morning at your preferred time
- **Above the Fold** — Top 3 headlines presented like a real newspaper front page
- **Sections Unfold** — Scroll down and sections (Technology, Sports, Business...) unfold with a 3D newspaper animation
- **AI Summaries** — Each article is summarized to 2-3 sentences by an on-device LLM
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
| **Local LLM** | llama.cpp (Phi-3-mini Q4) |
| **Animations** | Compose Animation + Compottie |

---

## Project Structure

```
Paperwala/
├── shared/                          # KMP shared module
│   └── src/
│       ├── commonMain/              # All business logic + Compose UI
│       │   ├── data/                # API services, repositories, mappers
│       │   ├── domain/              # Models + use cases
│       │   ├── presentation/        # Screens, components, theme, animations
│       │   ├── llm/                 # LLM curation engine (Phase 3)
│       │   └── di/                  # Koin modules
│       ├── androidMain/             # Android-specific (OkHttp, SQLite driver)
│       └── iosMain/                 # iOS-specific (Darwin, Native driver)
├── androidApp/                      # Android host app
├── iosApp/                          # iOS host app (Xcode)
└── gradle/libs.versions.toml       # Version catalog
```

---

## News Sources

### Indian Newspapers (RSS)
The Hindu · Indian Express · Hindustan Times · Mint · NDTV

### International
BBC · The Guardian · Reuters

### APIs
NewsAPI.org · GNews API · Newsdata.io

All sources are normalized into a unified `Article` model via `ArticleMapper` with automatic deduplication.

---

## Key Features

### The Morning Drop UI
The main screen uses a newspaper-inspired layout:
- **Masthead** — "PAPERWALA" in serif font with date and greeting
- **Above the Fold** — Hero article with full-width image + two-column sub-articles
- **Unfolding Sections** — Each section animates in with a 3D `rotationX` spring animation as you scroll, like unfolding a newspaper
- **Horizontal Cards** — Articles within each section scroll horizontally

### Onboarding (4 Steps)
1. **Topics** — Pick 3+ interests from 12 categories
2. **Sources** — Choose Indian + international newspapers
3. **Reading Time** — 5 / 10 / 15 / 20 / 30 minutes
4. **Delivery Time** — When should your paper be ready (5-10 AM)

### AI Curation (Phase 3)
- On-device LLM (Phi-3-mini, ~2.3 GB) summarizes articles and scores relevance
- Fallback chain: Local LLM → Claude Haiku API → Rule-based scoring
- Battery and memory guards prevent drain on low-resource devices

---

## Development Roadmap

- [x] **Phase 1: MVP Foundation** — Project setup, data layer, basic UI, onboarding
- [ ] **Phase 2: Morning Drop UI** — Lottie animations, multi-source RSS, background sync
- [ ] **Phase 3: Local LLM** — On-device summarization, relevance scoring, cloud fallback
- [ ] **Phase 4: Habits** — Reading streaks, notifications, bookmarks, share
- [ ] **Phase 5: Polish** — Dark/sepia mode, accessibility, app store release

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
   ```
4. Sync Gradle and run on emulator/device

### API Keys (Free Tiers)
- [NewsAPI.org](https://newsapi.org/) — 1,000 calls/month
- [GNews.io](https://gnews.io/) — 100 requests/day
- [Newsdata.io](https://newsdata.io/) — 200 credits/day

---

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed diagrams covering:
- System architecture
- Data flow pipeline
- Screen navigation
- Database schema
- LLM integration
- Module dependencies

---

## License

MIT

---

Built with Kotlin Multiplatform + Compose Multiplatform
