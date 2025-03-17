package eu.bsinfo.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.aallam.similarity.JaroWinkler
import eu.bsinfo.components.CardFormattableEntity
import eu.bsinfo.data.Customer
import eu.bsinfo.data.Reading
import kotlin.jvm.JvmName

private val separator = "\\s+".toRegex()

private val comparator = JaroWinkler()

@JvmName("searchCustomer")
fun Iterable<Customer>.search(query: String) = asSequence()
    .map { it to comparator.similarity(query, it.fullName) }
    .sortedByDescending { (_, score) -> score }
    .map { (customer) -> customer }
    .toList()

@JvmName("searchReading")
fun Iterable<Reading>.search(meterId: String) = asSequence()
    .map { it to comparator.similarity(meterId, it.meterId) }
    .sortedByDescending { (_, score) -> score }
    .map { (customer) -> customer }
    .toList()

@Composable
fun CardFormattableEntity.matchingName(matchingWith: String) = title.matching(matchingWith)

@Composable
fun Reading.matching(matchingWith: String) = meterId.matching(matchingWith)

@Composable
fun String.matching(matchingWith: String) = buildAnnotatedString {
    val highlighter = SpanStyle(background = MaterialTheme.colorScheme.tertiary)
    append(this@matching)

    matchingWith.split(separator).forEach {
        if (it.isNotBlank()) {
            it.toRegex(RegexOption.IGNORE_CASE).findAll(this@matching).forEach { match ->
                addStyle(highlighter, match.range.first, match.range.last + 1)
            }
        }
    }
}