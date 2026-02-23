# Paperwala - Architecture Documentation

> A curated morning news briefing app inspired by the Indian newspaper wala.
> Built with **Kotlin Multiplatform (KMP) + Compose Multiplatform**.

---

## 1. High-Level Architecture

```mermaid
%%{init: {'theme': 'base', 'themeVariables': {'fontSize': '18px', 'fontFamily': 'arial'}}}%%
graph TD
    A["Android App"] --> S["Shared Module (KMP)"]
    B["iOS App"] --> S

    S --> P["Presentation<br/>Compose Multiplatform"]
    S --> D["Domain<br/>Models + Use Cases"]
    S --> DA["Data<br/>Repositories + APIs"]
    S --> L["LLM<br/>Curation Engine"]

    style A fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px
    style B fill:#F5F5F5,stroke:#616161,stroke-width:2px
    style S fill:#E3F2FD,stroke:#1565C0,stroke-width:2px
    style P fill:#FFF3E0,stroke:#E65100,stroke-width:2px
    style D fill:#F3E5F5,stroke:#7B1FA2,stroke-width:2px
    style DA fill:#E0F7FA,stroke:#00838F,stroke-width:2px
    style L fill:#FCE4EC,stroke:#C62828,stroke-width:2px
```

---

## 2. Presentation Layer

```mermaid
%%{init: {'theme': 'base', 'themeVariables': {'fontSize': '16px'}}}%%
graph LR
    subgraph Screens
        Splash
        Onboarding
        MorningEdition
        ArticleDetail
        Settings
        Streaks
    end

    subgraph UI
        Components["Components<br/>ArticleCard, TopicChip,<br/>SectionHeader, StreakBadge"]
        Theme["Theme<br/>Newspaper Typography,<br/>Colors, Shapes"]
        Anim["Animations<br/>Unfold, Delivery,<br/>Section Slide"]
    end

    Nav["Voyager Navigation"] --> Screens
    Screens --> UI

    style Screens fill:#FFF3E0,stroke:#E65100,stroke-width:2px
    style UI fill:#FFF8E1,stroke:#F9A825,stroke-width:2px
    style Nav fill:#E3F2FD,stroke:#1565C0,stroke-width:2px
```

---

## 3. Domain Layer

```mermaid
%%{init: {'theme': 'base', 'themeVariables': {'fontSize': '16px'}}}%%
graph LR
    subgraph Models
        Article
        Edition
        Section
        UserPreferences
        ReadingStreak
        TopicCategory
    end

    subgraph UseCases["Use Cases"]
        GenEdition["GenerateMorningEdition"]
        FetchNews["FetchLatestNews"]
    end

    subgraph AI["AI Enhancers"]
        Factory["ArticleEnhancerFactory"]
        Cloud["CloudArticleEnhancer"]
        Local["LocalArticleEnhancer"]
        Rules["RuleBasedEnhancer"]
    end

    UseCases --> Models
    GenEdition --> Factory
    Factory --> Cloud
    Factory --> Local
    Factory --> Rules

    style Models fill:#F3E5F5,stroke:#7B1FA2,stroke-width:2px
    style UseCases fill:#EDE7F6,stroke:#512DA8,stroke-width:2px
```

---

## 4. Data Layer

```mermaid
%%{init: {'theme': 'base', 'themeVariables': {'fontSize': '16px'}}}%%
graph TD
    subgraph Repos["Repositories"]
        NR["NewsRepository"]
        ER["EditionRepository"]
        UR["UserRepository"]
    end

    subgraph Remote["Remote Sources (Ktor)"]
        NA["NewsAPI.org"]
        GN["GNews API"]
        ND["Newsdata.io"]
        RSS["RSS Feeds"]
    end

    subgraph Local["Local Storage"]
        SQL["SQLDelight<br/>Articles, Editions,<br/>Reading History"]
        DS["DataStore<br/>Preferences"]
    end

    AM["ArticleMapper<br/>Normalize + Deduplicate"]

    Remote --> AM
    AM --> Repos
    Repos --> Local

    style Repos fill:#E0F7FA,stroke:#00838F,stroke-width:2px
    style Remote fill:#E3F2FD,stroke:#1565C0,stroke-width:2px
    style Local fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px
    style AM fill:#FFF3E0,stroke:#E65100,stroke-width:2px
```

---

## 5. Daily Edition Pipeline

```mermaid
%%{init: {'theme': 'base', 'themeVariables': {'fontSize': '16px'}}}%%
flowchart TD
    A["1. FETCH<br/>NewsAPI + GNews + Newsdata + RSS"] --> B["2. NORMALIZE<br/>ArticleMapper unifies all sources"]
    B --> C["3. DEDUPLICATE<br/>URL hash + title similarity"]
    C --> D["4. STORE<br/>SQLDelight persists locally"]
    D --> E["5. AI SCORE & SUMMARIZE<br/>LLM rates relevance + summarizes"]
    E --> F["6. BUILD EDITION<br/>Top 3 above fold + sections by interest"]
    F --> G["7. DELIVER<br/>Notification + Morning Drop animation"]

    style A fill:#E3F2FD,stroke:#1565C0,stroke-width:2px
    style B fill:#E3F2FD,stroke:#1565C0,stroke-width:2px
    style C fill:#FFF3E0,stroke:#E65100,stroke-width:2px
    style D fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px
    style E fill:#FCE4EC,stroke:#C62828,stroke-width:2px
    style F fill:#F3E5F5,stroke:#7B1FA2,stroke-width:2px
    style G fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px
```

---

## 6. Screen Navigation Flow

```mermaid
%%{init: {'theme': 'base', 'themeVariables': {'fontSize': '16px'}}}%%
flowchart TD
    Start(["App Launch"]) --> Splash["Splash Screen"]
    Splash --> Check{"First Launch?"}

    Check -- Yes --> O1["1. Pick Topics"]
    O1 --> O2["2. Pick Newspapers"]
    O2 --> O3["3. Set Reading Time"]
    O3 --> O4["4. Set Delivery Time"]
    O4 --> Edition

    Check -- No --> Edition["Morning Edition<br/>(The Morning Drop)"]

    Edition --> Detail["Article Detail"]
    Edition --> Settings["Settings"]
    Detail --> Edition
    Settings --> Streaks["Streaks Dashboard"]

    style Start fill:#FCE4EC,stroke:#C62828,stroke-width:2px
    style Splash fill:#FCE4EC,stroke:#C62828,stroke-width:2px
    style Edition fill:#E3F2FD,stroke:#1565C0,stroke-width:3px
    style Detail fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px
    style Settings fill:#F3E5F5,stroke:#7B1FA2,stroke-width:2px
```

---

## 7. LLM Fallback Chain

```mermaid
%%{init: {'theme': 'base', 'themeVariables': {'fontSize': '18px'}}}%%
flowchart LR
    Input["Raw Articles"] --> Guard{"Battery > 20%?<br/>RAM OK?"}

    Guard -- Yes --> Local["LOCAL LLM<br/>User-selected model<br/>on device"]
    Guard -- No --> Cloud["CLOUD API<br/>Gemini 2.0 Flash"]
    Cloud -- Fails --> Rules["RULE-BASED<br/>Extractive + Scoring"]

    Local --> Output["Scored &<br/>Summarized"]
    Cloud --> Output
    Rules --> Output

    style Local fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px
    style Cloud fill:#E3F2FD,stroke:#1565C0,stroke-width:2px
    style Rules fill:#FFF3E0,stroke:#E65100,stroke-width:2px
    style Output fill:#F3E5F5,stroke:#7B1FA2,stroke-width:2px
```

### Available On-Device Models

Users can choose from 4 GGUF-quantized (Q4_K_M) models in Settings:

| Model | Size | Speed | Quality | RAM Required |
| --- | --- | --- | --- | --- |
| **Phi-3-mini** (default) | 2.3 GB | ~8 tok/s | Best quality | 3.5 GB |
| **Llama 3.2 3B** | 1.9 GB | ~12 tok/s | Fast + good | 3.0 GB |
| **Llama 3.2 1B** | 0.8 GB | ~30 tok/s | Fastest, basic | 1.5 GB |
| **Gemma 2 2B** | 1.5 GB | ~16 tok/s | Balanced | 2.5 GB |

The `LlmModel` enum in `domain/ai/LlmModel.kt` defines download URLs, file names, sizes, and RAM requirements. Selection is persisted in `UserPreferences` (SQLite `selected_llm_model` column).

---

## 8. Database Schema

```mermaid
%%{init: {'theme': 'base', 'themeVariables': {'fontSize': '14px'}}}%%
erDiagram
    ArticleEntity {
        TEXT id PK
        TEXT title
        TEXT summary
        TEXT source_url
        TEXT source_name
        TEXT image_url
        TEXT category
        REAL relevance_score
        INTEGER read_time_minutes
        INTEGER is_read
    }

    EditionEntity {
        TEXT id PK
        TEXT date
        TEXT headline
        INTEGER total_read_time_minutes
        INTEGER article_count
    }

    EditionArticleEntity {
        TEXT edition_id FK
        TEXT article_id FK
        TEXT section_category
        INTEGER display_order
    }

    ReadingHistoryEntity {
        INTEGER id PK
        TEXT article_id FK
        INTEGER read_at
    }

    UserPreferencesEntity {
        INTEGER id PK
        TEXT selected_topics
        TEXT preferred_sources
        INTEGER reading_time_minutes
        INTEGER delivery_time_hour
        INTEGER enable_local_llm
        TEXT selected_llm_model
    }

    EditionEntity ||--o{ EditionArticleEntity : has
    ArticleEntity ||--o{ EditionArticleEntity : belongs_to
    ArticleEntity ||--o{ ReadingHistoryEntity : tracks
```

---

## 9. News Sources

```mermaid
%%{init: {'theme': 'base', 'themeVariables': {'fontSize': '16px'}}}%%
graph TD
    subgraph Indian["Indian Newspapers (RSS)"]
        Hindu["The Hindu"]
        Express["Indian Express"]
        HT["Hindustan Times"]
        Mint["Mint"]
        NDTV["NDTV"]
    end

    subgraph International["International (RSS + API)"]
        BBC["BBC"]
        Guardian["The Guardian"]
        Reuters["Reuters"]
    end

    subgraph APIs["REST APIs"]
        NewsAPI["NewsAPI.org<br/>1000 calls/month"]
        GNews["GNews API<br/>100 req/day"]
        Newsdata["Newsdata.io<br/>200 credits/day"]
    end

    Indian --> Mapper["ArticleMapper"]
    International --> Mapper
    APIs --> Mapper
    Mapper --> DB["SQLDelight (Offline)"]

    style Indian fill:#FFF3E0,stroke:#E65100,stroke-width:2px
    style International fill:#E3F2FD,stroke:#1565C0,stroke-width:2px
    style APIs fill:#E0F7FA,stroke:#00838F,stroke-width:2px
    style Mapper fill:#FCE4EC,stroke:#C62828,stroke-width:2px
    style DB fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px
```

---

## 10. Module Dependencies

```mermaid
%%{init: {'theme': 'base', 'themeVariables': {'fontSize': '16px'}}}%%
graph TD
    Android["androidApp"] --> Shared
    iOS["iosApp"] --> Shared

    subgraph Shared["shared (KMP)"]
        Common["commonMain"]
        AndroidMain["androidMain"]
        iOSMain["iosMain"]
    end

    AndroidMain --> Common
    iOSMain --> Common

    Common --> Libs["Compose MP | Ktor | SQLDelight<br/>Koin | Voyager | Coil | kotlinx"]

    AndroidMain --> ALibs["OkHttp | Android SQLite<br/>WorkManager | llama.cpp JNI"]

    iOSMain --> ILibs["Darwin | Native SQLite<br/>BGTaskScheduler | llama.cpp Metal"]

    style Shared fill:#E3F2FD,stroke:#1565C0,stroke-width:2px
    style Android fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px
    style iOS fill:#F5F5F5,stroke:#616161,stroke-width:2px
```

---

## 11. "The Morning Drop" UI Layout

```
    ╔══════════════════════════════════╗
    ║         P A P E R W A L A        ║
    ║        Sunday, Feb 22, 2026      ║
    ╚══════════════════════════════════╝

    Good Morning, Randhir
    ~10 min read  |  12 stories

    ━━━━━━━━  ABOVE THE FOLD  ━━━━━━━━

    ┌──────────────────────────────────┐
    │          [HERO IMAGE]            │
    │                                  │
    │   Budget 2026: Major Tax         │
    │   Reforms Announced              │
    │                                  │
    │   The Union Budget 2026          │
    │   introduces significant         │
    │   income tax reforms...          │
    │                                  │
    │   THE HINDU  ·  3 min read       │
    └──────────────────────────────────┘

    ┌───────────────┐  ┌───────────────┐
    │   [image]     │  │   [image]     │
    │  Story #2     │  │  Story #3     │
    │  Summary...   │  │  Summary...   │
    │  HT · 2 min   │  │  BBC · 4 min  │
    └───────────────┘  └───────────────┘

    ━━━━━━━━  TECHNOLOGY  ━━━━━━━━━━━━

    ┌────────┐  ┌────────┐  ┌────────┐
    │ Card 1 │  │ Card 2 │  │ Card 3 │
    └────────┘  └────────┘  └────────┘
                                → scroll

    ━━━━━━━━  BUSINESS  ━━━━━━━━━━━━━━

    ┌────────┐  ┌────────┐
    │ Card 1 │  │ Card 2 │
    └────────┘  └────────┘

    ━━━━━━━━  SPORTS  ━━━━━━━━━━━━━━━━

    ┌────────┐  ┌────────┐  ┌────────┐
    │ Card 1 │  │ Card 2 │  │ Card 3 │
    └────────┘  └────────┘  └────────┘

    ══════════════════════════════════
    End of today's edition
    12 stories  ·  10 min read
```

Each section **unfolds with a 3D animation** (`graphicsLayer { rotationX }` with `spring(dampingRatio=0.65)`) as the user scrolls it into view — like unfolding a real newspaper.

---

## 12. Technology Stack

| Category | Technology | Version |
| --- | --- | --- |
| **Language** | Kotlin | 2.1.10 |
| **Framework** | Kotlin Multiplatform | - |
| **UI** | Compose Multiplatform | 1.7.3 |
| **Platforms** | Android (26+), iOS (15+) | - |
| **HTTP** | Ktor | 3.1.0 |
| **JSON** | kotlinx.serialization | 1.7.3 |
| **RSS/XML** | KSoup | 0.4.0 |
| **Images** | Coil 3 (KMP) | 3.1.0 |
| **Database** | SQLDelight | 2.0.2 |
| **Preferences** | DataStore | 1.1.3 |
| **DI** | Koin | 4.0.2 |
| **Navigation** | Voyager | 1.1.0-beta03 |
| **Async** | kotlinx.coroutines | 1.9.0 |
| **Date/Time** | kotlinx.datetime | 0.6.1 |
| **Local LLM** | llama.cpp bindings | - |
| **LLM Models** | Phi-3-mini, Llama 3.2, Gemma 2 (Q4_K_M GGUF) | 0.8-2.3 GB |
| **Cloud API** | Gemini 2.0 Flash (free tier) | - |
| **Animations** | Compose Animation | - |
| **Lottie** | Compottie | 2.0.0-rc01 |
| **Fonts** | Playfair Display, Lora, JetBrains Mono | - |
