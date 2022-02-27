package me.squeezymo.core.ui.util

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import java.util.*

class TextHighlighter(
    private val highlightRegex: Regex
) {

    fun createHighlightableText(
        text: String,
        highlightSpan: (str: String) -> Any
    ): Spannable {
        val tokens = TextHighlighter(highlightRegex).tokenize(text)
        val builder = SpannableStringBuilder()

        tokens.forEach { token ->
            when (token) {
                is Token.Plain -> {
                    builder.append(token.str)
                }
                is Token.Highlighted -> {
                    val firstIndex = builder.length

                    builder.append(token.str)
                    builder.setSpan(
                        highlightSpan(token.str),
                        firstIndex,
                        firstIndex + token.str.length,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }

        return builder
    }

    private fun tokenize(text: String): List<Token> {
        val matchResults = highlightRegex.findAll(text).toList()

        if (matchResults.isEmpty()) {
            return listOf(Token.Plain(text))
        }

        val tokens = LinkedList<Token>()
        var lastIndex = 0

        matchResults.forEachIndexed { matchResultIndex, matchResult ->
            val groups = matchResult.groups

            val extGroup = groups[0]!!
            val intGroup = groups[1]!!

            val extRange = extGroup.range
            val intRange = intGroup.range

            if (extRange.first > lastIndex) {
                tokens.add(Token.Plain(text.substring(lastIndex, extRange.first)))
            }

            tokens.add(Token.Highlighted(text.substring(intRange)))
            lastIndex = extRange.last + 1

            if (matchResultIndex == matchResults.lastIndex && lastIndex < text.lastIndex) {
                tokens.add(Token.Plain(text.substring(lastIndex, text.length)))
            }
        }

        return tokens
    }

    private sealed class Token {

        abstract val str: String

        data class Plain(
            override val str: String
        ) : Token()

        data class Highlighted(
            override val str: String
        ) : Token()

    }

}
