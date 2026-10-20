package com.example.data.sample

import com.example.data.local.CodeSnippetEntity
import com.example.data.local.TranscriptionEntity

object SampleData {

    val sampleTranscriptions = listOf(
        TranscriptionEntity(
            platform = "Maven",
            rawText = "नमस्ते आज हम बैठक शुरू करने जा रहे हैं(laughter)कृपया सभी लोग अपना माइक म्यूट रखें.क्या आप मुझे सुन सकते हैं",
            cleanText = "नमस्ते आज हम बैठक शुरू करने जा रहे हैं [laughter] कृपया सभी लोग अपना माइक म्यूट रखें। क्या आप मुझे सुन सकते हैं?",
            tagsDetected = "laughter",
            status = "PENDING",
            errorCount = 3,
            notes = "Replaced parentheses tag (laughter) with [laughter], converted English period to danda, added terminal question mark."
        ),
        TranscriptionEntity(
            platform = "CrowdGen",
            rawText = "हाँ हाँ[applause]धन्यवाद महोदय. इस परियोजना का बजट हमने अंतिम रूप दे दिया है",
            cleanText = "हाँ [applause] धन्यवाद महोदय। इस परियोजना का बजट हमने अंतिम रूप दे दिया है।",
            tagsDetected = "applause",
            status = "PENDING",
            errorCount = 3,
            notes = "Removed stutter duplicate 'हाँ हाँ', added spaces around [applause], appended terminal danda."
        ),
        TranscriptionEntity(
            platform = "Poliwag",
            rawText = "पीछे से बहुत शोर आ रहा है[background-noise]शायद निर्माण कार्य चल रहा है.क्या हम दस मिनट बाद बात कर सकते हैं?",
            cleanText = "पीछे से बहुत शोर आ रहा है [background-noise] शायद निर्माण कार्य चल रहा है। क्या हम दस मिनट बाद बात कर सकते हैं?",
            tagsDetected = "background-noise",
            status = "APPROVED",
            errorCount = 2,
            notes = "Standard background noise tag format verified."
        ),
        TranscriptionEntity(
            platform = "Maven",
            rawText = "वक्ता 2 ने कहा कि रिपोर्ट कल तक तैयार हो जाएगी[cough]लेकिन मुझे संदेह है.",
            cleanText = "वक्ता 2 ने कहा कि रिपोर्ट कल तक तैयार हो जाएगी [cough] लेकिन मुझे संदेह है।",
            tagsDetected = "cough",
            status = "PENDING",
            errorCount = 2,
            notes = "Spacing around [cough] adjusted, period changed to danda."
        )
    )

    val sampleCodeSnippets = listOf(
        CodeSnippetEntity(
            title = "Unsafe In-Memory Event Cache (Memory Leak & Race Condition)",
            category = "Events & Delegates",
            safetyScore = 35,
            issuesSummary = "Static Dictionary accessed without lock; Event subscribed with anonymous lambda causing permanent GC anchor.",
            aiAdvice = "Replace static Dictionary with ConcurrentDictionary<int, CacheEntry>. Replace lambda event subscription with a named handler and unsubscribe (-=) in IDisposable.Dispose().",
            csharpCode = """
public class CustomerEventCache
{
    // CRITICAL: Shared mutable static collection without synchronization
    private static readonly Dictionary<int, string> _cache = new Dictionary<int, string>();
    
    public void RegisterListener(OrderService publisher)
    {
        // CRITICAL: Anonymous lambda subscribed to long-lived publisher event.
        // Causes this CustomerEventCache instance to never be garbage collected!
        publisher.OrderPlaced += (sender, args) =>
        {
            _cache[args.OrderId] = args.CustomerName;
        };
    }
}
            """.trimIndent()
        ),
        CodeSnippetEntity(
            title = "Lock-Free Producer-Consumer Pipeline (High Throughput)",
            category = "Concurrency",
            safetyScore = 98,
            issuesSummary = "Passes all thread safety guidelines; uses System.Threading.Channels bounded buffer with cancellation.",
            aiAdvice = "Exemplary production-grade concurrency pattern. Thread-safe, zero lock contention, supports backpressure.",
            csharpCode = """
public class WorkDispatcher<T> : IAsyncDisposable
{
    private readonly Channel<T> _channel;
    private readonly CancellationTokenSource _cts = new CancellationTokenSource();

    public WorkDispatcher(int capacity = 1000)
    {
        var options = new BoundedChannelOptions(capacity)
        {
            FullMode = BoundedChannelFullMode.Wait,
            SingleReader = false,
            SingleWriter = false
        };
        _channel = Channel.CreateBounded<T>(options);
    }

    public async ValueTask PublishAsync(T item, CancellationToken ct = default)
    {
        await _channel.Writer.WriteAsync(item, ct).ConfigureAwait(false);
    }

    public IAsyncEnumerable<T> ReadAllAsync(CancellationToken ct = default)
    {
        return _channel.Reader.ReadAllAsync(ct);
    }

    public async ValueTask DisposeAsync()
    {
        _channel.Writer.Complete();
        _cts.Cancel();
        _cts.Dispose();
    }
}
            """.trimIndent()
        ),
        CodeSnippetEntity(
            title = "Broken Double-Checked Locking Singleton Pitfall",
            category = "Concurrency",
            safetyScore = 40,
            issuesSummary = "Missing volatile keyword on singleton instance field allows CPU instruction reordering; uses lock(this) anti-pattern.",
            aiAdvice = "In .NET, double-checked locking requires the field to be declared 'volatile' to prevent CPU memory reordering. Alternatively, use Lazy<T> for thread-safe deferred initialization.",
            csharpCode = """
public class AppConfigurationManager
{
    // WARNING: Non-volatile field can expose half-initialized object
    private static AppConfigurationManager _instance;
    private static readonly object _lock = new object();

    private AppConfigurationManager() { /* Read expensive settings */ }

    public static AppConfigurationManager Instance
    {
        get
        {
            if (_instance == null)
            {
                lock (_lock)
                {
                    if (_instance == null)
                    {
                        _instance = new AppConfigurationManager();
                    }
                }
            }
            return _instance;
        }
    }
}
            """.trimIndent()
        ),
        CodeSnippetEntity(
            title = "Weak Event Manager Pattern (Leak-Free Observer)",
            category = "Memory & IDisposable",
            safetyScore = 95,
            issuesSummary = "Uses WeakReference to prevent publisher from holding subscriber in memory.",
            aiAdvice = "Safe pattern for event-driven systems where subscribers have shorter lifespans than the event publisher.",
            csharpCode = """
public class SafeSubscriber : IDisposable
{
    private readonly EventPublisher _publisher;
    private bool _disposed;

    public SafeSubscriber(EventPublisher publisher)
    {
        _publisher = publisher ?? throw new ArgumentNullException(nameof(publisher));
        // Named method subscription
        _publisher.DataReceived += OnDataReceived;
    }

    private void OnDataReceived(object sender, DataEventArgs e)
    {
        Console.WriteLine($"Processed: {e.Payload}");
    }

    public void Dispose()
    {
        if (!_disposed)
        {
            // Explicit unsubscription guarantees subscriber collection
            _publisher.DataReceived -= OnDataReceived;
            _disposed = true;
        }
    }
}
            """.trimIndent()
        )
    )
}
