package com.williambl.tinyjsonparser

import platform.posix.pow

fun main() {
    val toParse = generateSequence(::readLine).joinToString("\n")
    print(Parser(toParse).readValue())
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

    fun hasNext(): Boolean {
        return cursor < input.length
    }

    fun read(predicate: (Char) -> Boolean): String {
        val result = StringBuilder()
        while (hasNext() && predicate(peek())) {
            result.append(step())
        }
        return result.toString()
    }

    fun read(pattern: CharSequence): String {
        val result = StringBuilder()
        var patternCursor = 0
        while (hasNext() && patternCursor < pattern.length && peek() == pattern[patternCursor++]) {
            result.append(step())
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
        val oldCursor = cursor
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
                if (hasNext() && peek() == '0')
                    integer = "0"
                else return null
            }

            if (hasNext() && peek() == '.') {
                skip()
                fraction = read(::isDigit)
            }
            if (hasNext() && peek().toLowerCase() == 'e') {
                skip()
                if (hasNext() && peek() == '-') {
                    exponentIsNegative = true
                    skip()
                } else if (hasNext() && peek() == '+') {
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
            cursor = oldCursor
            return null
        }
    }

    private fun readHexChar(): Char? {
        val oldCursor = cursor
        return try {
            read(::isHexDigit).toInt(16).toChar()
        } catch (e: NumberFormatException) {
            cursor = oldCursor
            null
        }
    }

    fun readString(): String? {
        val oldCursor = cursor
        val result = StringBuilder()
        if (peek() != '"')
            return null
        skip()

        while (hasNext() && peek() != '"') {
            val char = step()
            if (char == '\\') {
                when (val it = step()) {
                    '"' -> result.append('"')
                    '\\' -> result.append('\\')
                    '/' -> result.append('/')
                    'b' -> result.append('\b')
                    'f' -> result.append(0x0C.toChar())
                    'n' -> result.append('\n')
                    'r' -> result.append('\r')
                    't' -> result.append('\t')
                    'u' -> result.append(readHexChar())
                    else -> result.append(it)
                }
            } else {
                if (!isWhitespace(char) || char == ' ') {
                    result.append(char)
                } else {
                    cursor = oldCursor
                    return null
                }
            }
        }

        return result.toString()
    }

    fun readValue(): Any? {
        val oldCursor = cursor
        read(::isWhitespace)
        if (readNull()) {
            read(::isWhitespace)
            return null
        }
        val boolResult = readBoolean()
        if (boolResult != null) {
            read(::isWhitespace)
            return boolResult
        }
        val stringResult = readString()
        if (stringResult != null) {
            read(::isWhitespace)
            return stringResult
        }
        val numberResult = readNumber()
        if (numberResult != null) {
            read(::isWhitespace)
            return numberResult
        }
        cursor = oldCursor
        throw ParseException()
    }
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

fun isHexDigit(input: Char): Boolean {
    return input in '0'..'9' || input in 'a'..'f' || input in 'A'..'F'
}

class ParseException: Exception()