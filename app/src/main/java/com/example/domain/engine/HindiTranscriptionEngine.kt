package com.example.domain.engine

data class TranscriptionIssue(
    val type: IssueType,
    val severity: Severity,
    val message: String,
    val suggestion: String,
    val startIndex: Int = -1,
    val endIndex: Int = -1
)

enum class IssueType {
    PUNCTUATION,
    TAG_SYNTAX,
    TAG_SPACING,
    REPETITION,
    SCRIPT_MIXING,
    WHITESPACE
}

enum class Severity {
    CRITICAL,
    WARNING,
    INFO
}

data class TranscriptionAnalysisResult(
    val issues: List<TranscriptionIssue>,
    val cleanSuggestion: String,
    val detectedTags: List<String>,
    val wordCount: Int,
    val characterCount: Int,
    val accuracyScore: Int // 0-100
)

object HindiTranscriptionEngine {

    val VALID_TAGS = listOf(
        "laughter",
        "applause",
        "cough",
        "music",
        "background-noise",
        "inaudible",
        "screaming",
        "throat-clearing",
        "sigh",
        "crying",
        "overlap",
        "singing",
        "whispering",
        "bell",
        "applause-laughter"
    )

    private val TAG_CORRECTIONS = mapOf(
        "laugh" to "laughter",
        "clapping" to "applause",
        "clap" to "applause",
        "bgm" to "music",
        "bg-noise" to "background-noise",
        "bg_noise" to "background-noise",
        "noise" to "background-noise",
        "unintelligible" to "inaudible",
        "coughs" to "cough"
    )

    fun analyze(text: String): TranscriptionAnalysisResult {
        val trimmed = text.trim()
        val issues = mutableListOf<TranscriptionIssue>()
        val detectedTags = mutableListOf<String>()

        if (trimmed.isEmpty()) {
            return TranscriptionAnalysisResult(
                issues = emptyList(),
                cleanSuggestion = "",
                detectedTags = emptyList(),
                wordCount = 0,
                characterCount = 0,
                accuracyScore = 100
            )
        }

        // 1. Whitespace checks
        if (text.startsWith(" ") || text.endsWith(" ")) {
            issues.add(
                TranscriptionIssue(
                    type = IssueType.WHITESPACE,
                    severity = Severity.INFO,
                    message = "Transcript has leading or trailing whitespace.",
                    suggestion = "Trim whitespace from start and end."
                )
            )
        }
        if (Regex("\\s{2,}").containsMatchIn(text)) {
            issues.add(
                TranscriptionIssue(
                    type = IssueType.WHITESPACE,
                    severity = Severity.WARNING,
                    message = "Multiple consecutive spaces detected.",
                    suggestion = "Normalize multiple spaces into a single space."
                )
            )
        }

        // 2. English period instead of danda (।)
        val englishPeriodRegex = Regex("(?<!\\d)\\.(?!\\d)")
        if (englishPeriodRegex.containsMatchIn(trimmed)) {
            issues.add(
                TranscriptionIssue(
                    type = IssueType.PUNCTUATION,
                    severity = Severity.CRITICAL,
                    message = "English period (.) found in Hindi transcript. Hindi speech guidelines require danda (।).",
                    suggestion = "Replace '.' with Hindi full stop '।'"
                )
            )
        }

        // 3. Space before punctuation
        if (Regex("\\s+[।?!,]").containsMatchIn(trimmed)) {
            issues.add(
                TranscriptionIssue(
                    type = IssueType.PUNCTUATION,
                    severity = Severity.WARNING,
                    message = "Unnecessary space preceding punctuation mark.",
                    suggestion = "Remove space immediately before '।', '?', or ','."
                )
            )
        }

        // 4. Missing terminal punctuation
        val lastChar = trimmed.last()
        val endsWithTag = trimmed.endsWith("]")
        if (!endsWithTag && lastChar != '।' && lastChar != '?' && lastChar != '!' && lastChar != ',') {
            issues.add(
                TranscriptionIssue(
                    type = IssueType.PUNCTUATION,
                    severity = Severity.WARNING,
                    message = "Transcription ends without standard terminal punctuation (। or ?).",
                    suggestion = "Append '।' or '?' at sentence boundary."
                )
            )
        }

        // 5. Parentheses instead of square brackets for tags: e.g. (laughter)
        val roundBracketTagRegex = Regex("\\(([a-zA-Z\\-]+)\\)")
        roundBracketTagRegex.findAll(trimmed).forEach { match ->
            val content = match.groupValues[1].lowercase()
            if (VALID_TAGS.contains(content) || TAG_CORRECTIONS.containsKey(content)) {
                issues.add(
                    TranscriptionIssue(
                        type = IssueType.TAG_SYNTAX,
                        severity = Severity.CRITICAL,
                        message = "Parentheses used for tag: '(${match.groupValues[1]})'. Guidelines require square brackets.",
                        suggestion = "Change to '[${TAG_CORRECTIONS[content] ?: content}]'"
                    )
                )
            }
        }

        // 6. Unclosed brackets
        val openCount = trimmed.count { it == '[' }
        val closeCount = trimmed.count { it == ']' }
        if (openCount != closeCount) {
            issues.add(
                TranscriptionIssue(
                    type = IssueType.TAG_SYNTAX,
                    severity = Severity.CRITICAL,
                    message = "Mismatched brackets detected ($openCount '[' vs $closeCount ']').",
                    suggestion = "Ensure every tag is properly closed with square brackets."
                )
            )
        }

        // 7. Validate square bracket tags
        val squareTagRegex = Regex("\\[([^\\]]+)\\]")
        squareTagRegex.findAll(trimmed).forEach { match ->
            val tagContent = match.groupValues[1].trim().lowercase()
            detectedTags.add(tagContent)

            if (TAG_CORRECTIONS.containsKey(tagContent)) {
                val correct = TAG_CORRECTIONS[tagContent]
                issues.add(
                    TranscriptionIssue(
                        type = IssueType.TAG_SYNTAX,
                        severity = Severity.WARNING,
                        message = "Non-standard tag '[$tagContent]'.",
                        suggestion = "Use standard platform tag '[$correct]'"
                    )
                )
            } else if (!VALID_TAGS.contains(tagContent)) {
                issues.add(
                    TranscriptionIssue(
                        type = IssueType.TAG_SYNTAX,
                        severity = Severity.WARNING,
                        message = "Unrecognized noise tag: '[$tagContent]'.",
                        suggestion = "Verify against guideline tags: ${VALID_TAGS.take(5).joinToString { "[$it]" }}..."
                    )
                )
            }
        }

        // 8. Missing space around tags: e.g. "शब्द[laughter]" or "[applause]शब्द"
        val tagNoSpaceBefore = Regex("(?<=[^\\s\\[\\]])\\[([a-zA-Z\\-]+)\\]")
        if (tagNoSpaceBefore.containsMatchIn(trimmed)) {
            issues.add(
                TranscriptionIssue(
                    type = IssueType.TAG_SPACING,
                    severity = Severity.WARNING,
                    message = "Missing space before tag bracket.",
                    suggestion = "Add a space before '['."
                )
            )
        }
        val tagNoSpaceAfter = Regex("\\[([a-zA-Z\\-]+)\\](?=[^\\s\\[\\]।?!,])")
        if (tagNoSpaceAfter.containsMatchIn(trimmed)) {
            issues.add(
                TranscriptionIssue(
                    type = IssueType.TAG_SPACING,
                    severity = Severity.WARNING,
                    message = "Missing space after tag bracket.",
                    suggestion = "Add a space after ']' before the next word."
                )
            )
        }

        // 9. Word repetition (stutter/duplication)
        val words = trimmed.split(Regex("\\s+"))
        for (i in 0 until words.size - 1) {
            val w1 = words[i].replace(Regex("[।?!,\\[\\]]"), "")
            val w2 = words[i + 1].replace(Regex("[।?!,\\[\\]]"), "")
            if (w1.isNotEmpty() && w1.length > 1 && w1 == w2) {
                issues.add(
                    TranscriptionIssue(
                        type = IssueType.REPETITION,
                        severity = Severity.INFO,
                        message = "Consecutive duplicated word: '$w1 $w2'.",
                        suggestion = "Verify whether this is genuine stuttering or duplicate typing error."
                    )
                )
            }
        }

        // 10. Generate clean suggestion
        var clean = trimmed
        // Replace english dots with danda
        clean = clean.replace(Regex("(?<!\\d)\\.(?!\\d)"), "।")
        // Fix round brackets for tags
        roundBracketTagRegex.findAll(clean).forEach { match ->
            val raw = match.value
            val content = match.groupValues[1].lowercase()
            val valid = TAG_CORRECTIONS[content] ?: content
            clean = clean.replace(raw, "[$valid]")
        }
        // Normalize known tag abbreviations
        TAG_CORRECTIONS.forEach { (bad, good) ->
            clean = clean.replace("[$bad]", "[$good]")
        }
        // Fix spacing around tags
        clean = clean.replace(Regex("(?<=[^\\s])\\["), " [")
        clean = clean.replace(Regex("\\](?=[^\\s।?!,])"), "] ")
        // Fix spaces before punctuation
        clean = clean.replace(Regex("\\s+([।?!,])"), "$1")
        // Normalize spaces
        clean = clean.replace(Regex("\\s{2,}"), " ")
        clean = clean.trim()
        // Terminal punctuation if missing
        if (clean.isNotEmpty() && !clean.endsWith("]") && !clean.endsWith("।") && !clean.endsWith("?") && !clean.endsWith("!")) {
            clean += "।"
        }

        val wordCount = words.filter { it.isNotBlank() }.size
        val charCount = trimmed.length

        val penalty = issues.sumOf {
            when (it.severity) {
                Severity.CRITICAL -> 25
                Severity.WARNING -> 12
                Severity.INFO -> 4
            }
        }
        val accuracyScore = (100 - penalty).coerceIn(0, 100)

        return TranscriptionAnalysisResult(
            issues = issues,
            cleanSuggestion = clean,
            detectedTags = detectedTags.distinct(),
            wordCount = wordCount,
            characterCount = charCount,
            accuracyScore = accuracyScore
        )
    }
}
