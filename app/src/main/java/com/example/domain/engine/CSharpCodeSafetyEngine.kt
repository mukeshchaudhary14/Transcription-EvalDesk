package com.example.domain.engine

data class CodeSafetyIssue(
    val category: CodeIssueCategory,
    val severity: Severity,
    val title: String,
    val description: String,
    val recommendation: String,
    val codeSnippet: String? = null
)

enum class CodeIssueCategory {
    THREAD_SAFETY,
    EVENT_DELEGATE_LEAK,
    MEMORY_LIFECYCLE,
    ASYNC_PITFALL,
    DESIGN_PATTERN
}

data class CodeSafetyAnalysisResult(
    val score: Int, // 0 - 100
    val issues: List<CodeSafetyIssue>,
    val detectedPatterns: List<String>,
    val cleanSnippetSuggestion: String? = null
)

object CSharpCodeSafetyEngine {

    fun analyze(code: String): CodeSafetyAnalysisResult {
        val trimmed = code.trim()
        val issues = mutableListOf<CodeSafetyIssue>()
        val patterns = mutableListOf<String>()

        if (trimmed.isEmpty()) {
            return CodeSafetyAnalysisResult(
                score = 100,
                issues = emptyList(),
                detectedPatterns = emptyList(),
                cleanSnippetSuggestion = null
            )
        }

        // --- 1. Thread Safety & Concurrency ---

        // Shared mutable static non-concurrent collections
        val staticCollectionRegex = Regex("static\\s+(?:readonly\\s+)?(?:List|Dictionary|HashSet|Queue|Stack|LinkedList)<([^>]+)>\\s+([a-zA-Z0-9_]+)")
        staticCollectionRegex.findAll(trimmed).forEach { match ->
            val colType = match.value
            issues.add(
                CodeSafetyIssue(
                    category = CodeIssueCategory.THREAD_SAFETY,
                    severity = Severity.CRITICAL,
                    title = "Thread-Unsafe Shared Static Collection",
                    description = "Detected `$colType`. Standard generic collections are not thread-safe for concurrent read/write operations and can throw `InvalidOperationException` or corrupt internal state.",
                    recommendation = "Use thread-safe collections from `System.Collections.Concurrent` (e.g., `ConcurrentDictionary`, `ConcurrentQueue`, `ConcurrentBag`) or synchronize access with `ReaderWriterLockSlim` or a dedicated lock object."
                )
            )
            patterns.add("Static Collection: ${match.groupValues[2]}")
        }

        // lock (this) or lock (typeof(...))
        if (Regex("lock\\s*\\(\\s*this\\s*\\)").containsMatchIn(trimmed)) {
            issues.add(
                CodeSafetyIssue(
                    category = CodeIssueCategory.THREAD_SAFETY,
                    severity = Severity.CRITICAL,
                    title = "Dangerous lock(this) Idiom",
                    description = "Locking on `this` exposes your synchronization monitor to external code, creating high deadlock risks if external callers also lock on your instance.",
                    recommendation = "Lock on a private, dedicated lock object: `private readonly object _syncRoot = new object();` or `private readonly Lock _lock = new();` in .NET 9+."
                )
            )
            patterns.add("lock(this)")
        }

        if (Regex("lock\\s*\\(\\s*typeof\\s*\\(").containsMatchIn(trimmed)) {
            issues.add(
                CodeSafetyIssue(
                    category = CodeIssueCategory.THREAD_SAFETY,
                    severity = Severity.CRITICAL,
                    title = "Dangerous lock(typeof(...)) Idiom",
                    description = "Locking on a `Type` object creates a process-wide lock across AppDomains/assemblies, which can lead to unpredictable lock contention and deadlocks.",
                    recommendation = "Use a private static lock object: `private static readonly object _typeLock = new object();`"
                )
            )
            patterns.add("lock(typeof)")
        }

        // Synchronous Task blocking: .Result, .Wait(), .GetAwaiter().GetResult()
        val syncTaskRegex = Regex("([a-zA-Z0-9_\\(\\)]+)\\.(Result|Wait\\(\\)|GetAwaiter\\(\\)\\.GetResult\\(\\))")
        if (syncTaskRegex.containsMatchIn(trimmed)) {
            issues.add(
                CodeSafetyIssue(
                    category = CodeIssueCategory.ASYNC_PITFALL,
                    severity = Severity.CRITICAL,
                    title = "Sync-Over-Async Blocking Detected",
                    description = "Invoking `.Result`, `.Wait()`, or `.GetAwaiter().GetResult()` blocks the calling thread while awaiting an asynchronous operation. In UI contexts or ASP.NET SynchronizationContexts, this causes deadlocks and thread pool starvation.",
                    recommendation = "Make the enclosing method `async Task` and use `await task.ConfigureAwait(false);` instead."
                )
            )
            patterns.add("Sync-over-Async")
        }

        // async void (unless event handler)
        val asyncVoidRegex = Regex("async\\s+void\\s+([a-zA-Z0-9_]+)")
        asyncVoidRegex.findAll(trimmed).forEach { match ->
            val methodName = match.groupValues[1]
            if (!methodName.endsWith("Handler") && !methodName.startsWith("On") && !methodName.contains("Click")) {
                issues.add(
                    CodeSafetyIssue(
                        category = CodeIssueCategory.ASYNC_PITFALL,
                        severity = Severity.CRITICAL,
                        title = "Avoid 'async void' Methods",
                        description = "Method `$methodName` returns `async void`. Exceptions thrown in `async void` methods cannot be caught by callers and will terminate the process via unhandled exceptions.",
                        recommendation = "Change signature to `public async Task $methodName`."
                    )
                )
            }
        }

        // --- 2. Delegate & Event Memory Leaks ---

        // Lambda in event subscription
        val lambdaEventRegex = Regex("([a-zA-Z0-9_\\.]+)\\s*\\+=\\s*\\([^)]*\\)\\s*=>")
        if (lambdaEventRegex.containsMatchIn(trimmed) || Regex("([a-zA-Z0-9_\\.]+)\\s*\\+=\\s*delegate").containsMatchIn(trimmed)) {
            issues.add(
                CodeSafetyIssue(
                    category = CodeIssueCategory.EVENT_DELEGATE_LEAK,
                    severity = Severity.CRITICAL,
                    title = "Anonymous Lambda Event Subscription (Memory Leak Hazard)",
                    description = "Subscribing to an event using an anonymous lambda (`+= (s,e) =>`) creates an unreachable delegate instance that cannot be unsubscribed with `-=`. If the publisher outlives the subscriber, the subscriber is pinned in memory forever.",
                    recommendation = "Use a named method handler so you can call `-=` in `Dispose()` or use `WeakEventManager` / `WeakReference<T>`."
                )
            )
            patterns.add("Lambda Event += Subscription")
        }

        // Event subscription without unsubscription
        val hasEventSub = Regex("[a-zA-Z0-9_\\.]+\\s*\\+=\\s*").containsMatchIn(trimmed)
        val hasEventUnsub = Regex("[a-zA-Z0-9_\\.]+\\s*-=\\s*").containsMatchIn(trimmed)
        if (hasEventSub && !hasEventUnsub && !trimmed.contains("WeakEventManager") && !trimmed.contains("WeakReference")) {
            issues.add(
                CodeSafetyIssue(
                    category = CodeIssueCategory.EVENT_DELEGATE_LEAK,
                    severity = Severity.WARNING,
                    title = "Event Subscription Without Unsubscription",
                    description = "The code subscribes to events using `+=` but lacks a corresponding `-=` unsubscription or Weak Event listener pattern.",
                    recommendation = "Implement `IDisposable` on the subscriber and unsubscribe (`-=`) in the `Dispose()` method, or use `System.Windows.WeakEventManager`."
                )
            )
            patterns.add("Unpaired Event Subscription")
        }

        // Static events
        if (Regex("public\\s+static\\s+event\\s+").containsMatchIn(trimmed)) {
            issues.add(
                CodeSafetyIssue(
                    category = CodeIssueCategory.EVENT_DELEGATE_LEAK,
                    severity = Severity.WARNING,
                    title = "Static Event Publisher",
                    description = "Static events persist for the lifetime of the process. Any instance subscribing to this static event will never be garbage collected unless explicitly unsubscribed.",
                    recommendation = "Ensure subscribers implement `IDisposable` or wrap the invocation in weak delegate proxies."
                )
            )
            patterns.add("Static Event")
        }

        // --- 3. Resource Lifecycle & IDisposable ---

        // Instantiating HttpClient in method
        if (Regex("new\\s+HttpClient\\s*\\(").containsMatchIn(trimmed)) {
            issues.add(
                CodeSafetyIssue(
                    category = CodeIssueCategory.MEMORY_LIFECYCLE,
                    severity = Severity.WARNING,
                    title = "Direct HttpClient Instantiation",
                    description = "Creating `new HttpClient()` instances on demand leads to socket exhaustion under load because closed sockets linger in `TIME_WAIT` state.",
                    recommendation = "Inject `IHttpClientFactory` or share a single static/singleton `HttpClient` instance."
                )
            )
            patterns.add("new HttpClient()")
        }

        // IDisposable field without IDisposable interface on class
        val hasDisposableField = Regex("(?:private|protected|public)\\s+(?:readonly\\s+)?(?:Timer|CancellationTokenSource|FileStream|SqlConnection|MemoryStream)\\s+").containsMatchIn(trimmed)
        val implementsDisposable = trimmed.contains(": IDisposable") || trimmed.contains(": System.IDisposable") || trimmed.contains("Dispose()")
        if (hasDisposableField && !implementsDisposable) {
            issues.add(
                CodeSafetyIssue(
                    category = CodeIssueCategory.MEMORY_LIFECYCLE,
                    severity = Severity.WARNING,
                    title = "Class Holds Disposable Field But Doesn't Implement IDisposable",
                    description = "Class owns disposable fields (such as `Timer`, `CancellationTokenSource`, or streams) but does not implement `IDisposable` to release them deterministically.",
                    recommendation = "Implement `IDisposable` and clean up fields in `public void Dispose()`."
                )
            )
            patterns.add("Undisposed Fields")
        }

        // Channel or Producer-Consumer patterns detected
        if (trimmed.contains("Channel.Create") || trimmed.contains("Channel<")) {
            patterns.add("System.Threading.Channels (High Performance)")
        }
        if (trimmed.contains("ConcurrentDictionary<")) {
            patterns.add("ConcurrentDictionary (Thread-Safe)")
        }
        if (trimmed.contains("Interlocked.")) {
            patterns.add("Interlocked Atomic Operations")
        }
        if (trimmed.contains("SemaphoreSlim")) {
            patterns.add("SemaphoreSlim Async Throttling")
        }

        // Calculate score
        val penalty = issues.sumOf {
            when (it.severity) {
                Severity.CRITICAL -> 25
                Severity.WARNING -> 12
                Severity.INFO -> 5
            }
        }
        val score = (100 - penalty).coerceIn(10, 100)

        return CodeSafetyAnalysisResult(
            score = score,
            issues = issues,
            detectedPatterns = patterns.distinct()
        )
    }
}
