# EvalDesk & Code Vault — Implementation Plan

A dedicated dual-hub workstation for Android: an AI-assisted Hindi audio transcription evaluation desk (tailored for platforms like Maven, CrowdGen, and Poliwag) paired with a C# architectural and concurrency pattern vault with automated thread-safety and delegate memory leak audits.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed based on your answers:
> - **Primary Focus**: Dual-module application combining **EvalDesk** (Hindi Transcription Evaluation) and **Code Vault** (C# Architectural Safety & Patterns).
> - **Analysis Engine**: **Hybrid Analysis** — instant, offline rule-based heuristics (regex, tag matching, punctuation linting, syntax flags) paired with on-demand **Gemini AI** (`gemini-3.5-flash`) for deep semantic, linguistic, and concurrency audits.
> - **Data Ingestion**: Multi-modal inputs including direct text pasting, structured JSON/CSV batch file imports/exports, and quick-load realistic sample task suites.

---

### 1. Overview & Core Concept

**EvalDesk & Code Vault** unites two demanding professional workflows into a unified, high-density Android application:

1. **Transcription EvalDesk (Hindi QA Specialist Tool)**:
   - Evaluates raw Hindi transcriptions against standard QA speech-to-text guidelines (Maven / CrowdGen / Poliwag).
   - Instant local heuristics detect missing full-stops/danda (`।`), misplaced punctuation, invalid noise tags (e.g., `[laughter]`, `[applause]`, `[cough]`, `[inaudible]`, `[music]`, `[background-noise]`), English loan word transliteration issues, and speaker turn inconsistencies.
   - Deep Gemini AI evaluation provides one-click clean formatted text, audio tag validation, and accuracy scoring.
   - Daily throughput tracking, rejection/approval rate metrics, and bulk JSON/CSV export.

2. **C# Code Vault (Concurrency & Architectural Safety Hub)**:
   - Stores and catalogs complex C# snippets (asynchronous pipelines, custom synchronization primitives, weak event patterns, lock-free structures).
   - Automated code safety scanner specifically checking:
     - **Thread-Safety & Race Conditions**: Unsynchronized shared mutable fields, unsafe `List<T>`/`Dictionary<K,V>` access across threads, double-checked locking flaws, thread-pool starvation.
     - **Delegate & Event Memory Leaks**: Event subscription (`+=`) without corresponding unsubscription (`-=`), lambda closures holding onto instance lifecycles, missing `WeakEventManager` / `IWeakEventListener`.
     - **Resource Lifecycle**: Missing `IDisposable` or `IAsyncDisposable` implementations, unclosed streams, `CancellationToken` omission.
   - Formatted syntax-highlighted editor with copy shortcuts, AI vulnerability annotations, and suggested fixes.

3. **Analytics & Performance Dashboard**:
   - Daily throughput counters, QA accuracy percentages, common error distributions, and session history cached securely in Room Database.

---

### 2. User Experience & Visual Design

#### Aesthetic Direction: High-Density Engineer Workstation (Dark Slate & Electric Cyan)
- **Palette**: Deep slate background (`#0B0F17` / `#131B2A`), high-contrast surface panels (`#1E293B`), electric cyan primary accents (`#06B6D4`), emerald green for passing QA/safe patterns (`#10B981`), amber warning tokens (`#F59E0B`), and crimson alert badges (`#EF4444`).
- **Typography**: Clean monospace styling for code blocks and tag inspectors; clean, readable typography with full Hindi Unicode font support for Devanagari text.
- **Layout**: Top app bar with quick mode switching, action pills, and status chips; scrollable data-dense cards with expandable inspection panes and one-touch action buttons.

#### Key User Flows:
1. **Hindi Transcription Workflow**:
   - User pastes a Hindi snippet or taps **"Load Sample Batch"** / **"Import JSON/CSV"**.
   - The editor immediately displays real-time lint flags (e.g. "Missing danda before tag", "Unclosed bracket `[applause`", "Trailing whitespace").
   - User taps **"Deep AI Audit"**: Gemini analyzes linguistic fidelity, produces clean formatted Hindi output, and highlights corrections.
   - User can accept corrections with one tap, copy the sanitized result, or mark the task as "Approved" / "Rejected" to log stats.

2. **C# Code Vault Workflow**:
   - User inputs or pastes a C# code snippet (or selects from preloaded patterns: *Weak Event Pattern*, *Thread-Safe Producer-Consumer Channel*, *ConcurrentDictionary Gotchas*, *Async Lock*).
   - App runs immediate static heuristic checks (detects `+=` without `-=`, non-thread-safe static collections, missing `using` or `Dispose`).
   - User taps **"Scan Safety & Patterns"**: Gemini performs in-depth concurrency and lifecycle review, rating safety score (1–100) and generating annotated recommendations.
   - User saves snippet to vault with tags (e.g., `#Concurrency`, `#Memory`, `#Events`).

3. **Batch Import & Stats View**:
   - Supports uploading or pasting JSON/CSV task rows.
   - Visual dashboard displays: Today's Tasks Completed, Accuracy Rate %, Error Breakdown (Tags, Punctuation, Concurrency, Delegates), and Export options.

---

### 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Why & Trade-Offs |
|---|---|---|
| **Analysis Architecture** | Hybrid: Local Regex + Gemini AI | Sub-millisecond instant feedback while typing without network lag or API quota consumption; deep AI reasoning triggered on-demand. |
| **Local Persistence** | Room Database | Stores evaluation logs, daily throughput stats, and saved C# snippets locally on device without requiring cloud accounts or network sync. |
| **Batch Format Handling** | Flexible JSON & CSV Parsers + Preloaded Presets | Users can test immediately with rich sample tasks without needing external files, while still supporting real-world Maven/CrowdGen export imports. |
| **AI Secret Management** | `BuildConfig.GEMINI_API_KEY` via Secrets Plugin | Secure API key injection with graceful local heuristic fallback if no key is configured in the environment. |

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
├──────────────────────────────────┬─────────────────────────────────────┤
│      EvalDesk Composable         │        Code Vault Composable        │
│  - Hindi Text / Tag Editor       │  - C# Syntax & Pattern Editor       │
│  - Instant Heuristic Linter      │  - Thread-Safety / Leak Inspector   │
│  - Gemini QA Audit & Diff View   │  - AI Recommendation & Code Fix     │
├──────────────────────────────────┴─────────────────────────────────────┤
│                     Dashboard & Batch Manager                          │
│  - Daily Throughput & Accuracy Charts | JSON & CSV Import/Export       │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                       Workstation ViewModel                            │
│  - Manages UI State Flow (EvalState, VaultState, DashboardState)       │
│  - Coordinates Local Rule Engine & Gemini Coroutines                   │
└──────────────────┬──────────────────────────────────┬──────────────────┘
                   │                                  │
                   ▼                                  ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────┐
│        Local Analysis Engine         │  │     Gemini Network Layer     │
│  - Hindi Punctuation & Danda Linter  │  │  - Retrofit / OkHttp Client  │
│  - Audio Tag Syntax Validator        │  │  - Prompt Orchestrator:      │
│  - C# Delegate & Leak Heuristics     │  │    * Hindi Transcription QA  │
│  - C# Concurrency Pattern Matcher    │  │    * C# Thread-Safety Audit  │
└──────────────────┬───────────────────┘  └──────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         Room Persistence Layer                         │
│  - TranscriptionTaskEntity (history, raw text, clean text, status)     │
│  - CodeSnippetEntity (title, code, tags, safetyScore, warnings)        │
│  - DailyMetricEntity (date, completedCount, passRate, tagErrors)       │
└────────────────────────────────────────────────────────────────────────┘
```

#### Room Entities & Data Model
- `TranscriptionRecord`: ID, raw text, clean text, task platform (Maven/CrowdGen/Poliwag), tags detected, status (Pending, Approved, Rejected), error count, timestamp.
- `CodeSnippetRecord`: ID, title, csharpCode, category (Concurrency, Delegates, Memory, Architecture), safetyScore, detectedIssuesJson, aiAdvice, isFavorite, timestamp.
- `DailyMetricRecord`: Date string (YYYY-MM-DD), tasksEvaluated, tasksApproved, codeSnippetsAudited, commonErrorsSummary.

---

### Next Steps & Verification

Upon approval:
1. Update `metadata.json` and `res/values/strings.xml` to match app identity.
2. Configure Room database entities, DAOs, and repository.
3. Implement the local rule engines for Hindi QA and C# code audits.
4. Integrate Gemini API REST client with OkHttp and data models.
5. Build the Compose UI with high-density tabs (EvalDesk, Code Vault, Analytics, Batch Manager).
6. Verify build cleanly using `compile_applet`.
