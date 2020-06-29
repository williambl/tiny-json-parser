package com.williambl.tinyjsonparser

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

    //TODO: readBoolean

    //TOOO: readNull

    //TODO: readNumber

    //TODO: readString
}

fun isWhitespace(input: Char): Boolean {
    return input == ' ' || input == '\r' || input == '\n' || input == '\t'
}