package com.williambl.tinyjsonparser

import platform.posix.pow

fun main() {
    val toParse = generateSequence(::readLine).joinToString("\n")
    print(Parser(toParse).read(::isWhitespace))
}

class Parser(val input: CharSequence) {
    var cursor: Int = 0;

    fun step(): Char {
        return input[cursor++]
    }

    fun peek(): Char {
        return input[cursor]
    }

    fun skip() {
        cursor++
    }

    fun read(predicate: (Char) -> Boolean): String {
        val result = StringBuilder()
        while (predicate(peek())) {
            result.append(step())
        }
        return result.toString()
    }

    fun read(pattern: CharSequence): String {
        val result = StringBuilder()
        var patternCursor = 0
        while (peek() == pattern[patternCursor++]) {
            result.append(skip())
        }
        return result.toString()
    }

    fun readBoolean(): Boolean? {
        val oldCursor = cursor
        if (read("true") == "true")
            return true
        cursor = oldCursor
        if (read("false") == "false")
            return false
        cursor = oldCursor
        return null
    }

    fun readNull(): Boolean {
        val oldCursor = cursor
        if (read("null") == "null")
            return true
        cursor = oldCursor
        return false
    }

    fun readNumber(): Number? {
        try {
            var isNegative = false
            var integer: String
            var fraction: String? = null
            var exponentIsNegative = false
            var exponent: String? = null
            if (peek() == '-') {
                isNegative = true
                skip()
            }

            integer = read(::isOneToNine)
            if (integer != "") {
                integer += read(::isDigit)
            }
            if (integer == "") {
                if (peek() == '0')
                    integer = "0"
                return null
            }

            if (peek() == '.') {
                skip()
                fraction = read(::isDigit)
            }
            if (peek().toLowerCase() == 'e') {
                if (peek() == '-') {
                    exponentIsNegative = true
                    skip()
                } else if (peek() == '+') {
                    skip()
                }

                exponent = read(::isDigit)
            }

            if (fraction == null) {
                var result: Int = integer.toInt()
                if (isNegative)
                    result *= -1

                val exp = exponent?.toInt() ?: 0
                result *= pow(10.0, (if (exponentIsNegative) -1 * exp else exp).toDouble()).toInt()
                return result
            }

            var result = "$integer.$fraction".toDouble()
            if (isNegative)
                result *= -1

            val exp = exponent?.toInt() ?: 0
            result *= pow(10.0, (if (exponentIsNegative) -1 * exp else exp).toDouble())
            return result
        } catch (e: NumberFormatException) {
            return null
        }
    }

    //TODO: readString
}

fun isWhitespace(input: Char): Boolean {
    return input == ' ' || input == '\r' || input == '\n' || input == '\t'
}

fun isDigit(input: Char): Boolean {
    return input in '0'..'9'
}

fun isOneToNine(input: Char): Boolean {
    return input in '1'..'9'
}

class ParseException: Exception()