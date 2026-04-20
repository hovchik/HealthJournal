package com.hovchik.healthjournal.presentation.screen.legal

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hovchik.healthjournal.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class LegalDocument(@RawRes val rawRes: Int, val titleRes: Int) {
    PRIVACY_POLICY(R.raw.privacy_policy, R.string.legal_privacy_policy),
    TERMS_OF_USE(R.raw.terms_of_use, R.string.legal_terms_of_use),
    MEDICAL_DISCLAIMER(R.raw.medical_disclaimer, R.string.legal_medical_disclaimer);

    companion object {
        fun fromKey(key: String): LegalDocument = when (key) {
            "privacy" -> PRIVACY_POLICY
            "terms" -> TERMS_OF_USE
            "disclaimer" -> MEDICAL_DISCLAIMER
            else -> MEDICAL_DISCLAIMER
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDocumentScreen(docKey: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val doc = LegalDocument.fromKey(docKey)

    val text by produceState(initialValue = "", doc) {
        value = withContext(Dispatchers.IO) {
            context.resources.openRawResource(doc.rawRes).bufferedReader().use { it.readText() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(doc.titleRes), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = renderMarkdown(text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Minimal Markdown-ish renderer: supports `#`/`##`/`###` headings, `**bold**`,
 * and plain paragraphs. Intentionally tiny — the legal docs are ours, we
 * control the formatting, and pulling in a real Markdown lib for three files
 * isn't worth the size cost.
 */
private fun renderMarkdown(md: String) = buildAnnotatedString {
    val lines = md.lines()
    var inCodeBlock = false
    lines.forEachIndexed { index, raw ->
        if (raw.trimStart().startsWith("```")) {
            inCodeBlock = !inCodeBlock
            return@forEachIndexed
        }
        if (inCodeBlock) {
            append(raw)
            if (index < lines.lastIndex) append('\n')
            return@forEachIndexed
        }
        when {
            raw.startsWith("# ") -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = androidx.compose.ui.unit.TextUnit(22f, androidx.compose.ui.unit.TextUnitType.Sp)))
                append(raw.removePrefix("# "))
                pop()
            }
            raw.startsWith("## ") -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp)))
                append(raw.removePrefix("## "))
                pop()
            }
            raw.startsWith("### ") -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = androidx.compose.ui.unit.TextUnit(15f, androidx.compose.ui.unit.TextUnitType.Sp)))
                append(raw.removePrefix("### "))
                pop()
            }
            raw.startsWith("- ") -> {
                append("  •  ")
                appendInline(raw.removePrefix("- "))
            }
            raw.isBlank() -> Unit
            else -> appendInline(raw)
        }
        if (index < lines.lastIndex) append('\n')
    }
}

/** Handle inline `**bold**` spans; leave everything else as-is. */
private fun androidx.compose.ui.text.AnnotatedString.Builder.appendInline(line: String) {
    var i = 0
    while (i < line.length) {
        val boldStart = line.indexOf("**", i)
        if (boldStart < 0) {
            append(line.substring(i))
            return
        }
        append(line.substring(i, boldStart))
        val boldEnd = line.indexOf("**", boldStart + 2)
        if (boldEnd < 0) {
            append(line.substring(boldStart))
            return
        }
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        append(line.substring(boldStart + 2, boldEnd))
        pop()
        i = boldEnd + 2
    }
}
