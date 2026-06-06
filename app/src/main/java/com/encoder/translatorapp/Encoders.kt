package com.encoder.translatorapp

import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder

object Encoders {

    fun toBase64(input: String): String {
        return Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    fun toUrlEncoding(input: String): String {
        return URLEncoder.encode(input, "UTF-8")
    }

    fun toUnicodeEscape(input: String): String {
        return input.map { char ->
            if (char.code > 127) {
                String.format("\\u%04X", char.code)
            } else {
                char.toString()
            }
        }.joinToString("")
    }

    fun toHtmlEntities(input: String): String {
        return input.map { char ->
            if (char.code > 127) {
                "&#x${Integer.toHexString(char.code).uppercase()};"
            } else {
                char.toString()
            }
        }.joinToString("")
    }

    fun toXml(input: String): String {
        return buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<request>")
            appendLine("    <content>$input</content>")
            append("</request>")
        }
    }

    fun toMorseCode(input: String): String {
        val morseMap = mapOf(
            'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..",
            'E' to ".", 'F' to "..-.", 'G' to "--.", 'H' to "....",
            'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
            'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.",
            'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
            'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
            'Y' to "-.--", 'Z' to "--..",
            '0' to "-----", '1' to ".----", '2' to "..---",
            '3' to "...--", '4' to "....-", '5' to ".....",
            '6' to "-....", '7' to "--...", '8' to "---..",
            '9' to "----.",
            ' ' to "/", '.' to ".-.-.-", ',' to "--..--",
            '?' to "..--..", '!' to "-.-.--", '\'' to ".----.",
            '"' to ".-..-.", ':' to "---...", ';' to "-.-.-.",
            '=' to "-...-", '+' to ".-.-.", '-' to "-....-",
            '/' to "-..-.", '(' to "-.--.", ')' to "-.--.-",
            '@' to ".--.-."
        )

        return input.map { char ->
            val upper = char.uppercaseChar()
            if (upper in morseMap) {
                morseMap[upper]!!
            } else {
                val bytes = char.toString().toByteArray(Charsets.UTF_8)
                bytes.joinToString(" ") { byte ->
                    val bits = String.format("%8s", Integer.toBinaryString(byte.toInt() and 0xFF)).replace(' ', '0')
                    bits.map { if (it == '1') '-' else '.' }.joinToString("")
                }
            }
        }.joinToString(" ")
    }

    // ===== 解码方法 =====

    fun fromBase64(input: String): String {
        return String(Base64.decode(input.trim(), Base64.NO_WRAP), Charsets.UTF_8)
    }

    fun fromUrlEncoding(input: String): String {
        return URLDecoder.decode(input, "UTF-8")
    }

    fun fromUnicodeEscape(input: String): String {
        val regex = Regex("""\\u([0-9A-Fa-f]{4})""")
        return regex.replace(input) { match ->
            val code = match.groupValues[1].toInt(16)
            code.toChar().toString()
        }
    }

    fun fromHtmlEntities(input: String): String {
        val hexRegex = Regex("""&#x([0-9A-Fa-f]+);""")
        val decRegex = Regex("""&#(\d+);""")
        var result = hexRegex.replace(input) { match ->
            val code = match.groupValues[1].toInt(16)
            code.toChar().toString()
        }
        result = decRegex.replace(result) { match ->
            val code = match.groupValues[1].toInt()
            code.toChar().toString()
        }
        return result
    }

    fun fromXml(input: String): String {
        val regex = Regex("""<content>(.*?)</content>""", RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(input)
        return match?.groupValues?.get(1)?.trim() ?: "无法解析XML内容"
    }

    fun fromMorseCode(input: String): String {
        val reverseMorseMap = mapOf(
            ".-" to 'A', "-..." to 'B', "-.-." to 'C', "-.." to 'D',
            "." to 'E', "..-." to 'F', "--." to 'G', "...." to 'H',
            ".." to 'I', ".---" to 'J', "-.-" to 'K', ".-.." to 'L',
            "--" to 'M', "-." to 'N', "---" to 'O', ".--." to 'P',
            "--.-" to 'Q', ".-." to 'R', "..." to 'S', "-" to 'T',
            "..-" to 'U', "...-" to 'V', ".--" to 'W', "-..-" to 'X',
            "-.--" to 'Y', "--.." to 'Z',
            "-----" to '0', ".----" to '1', "..---" to '2',
            "...--" to '3', "....-" to '4', "....." to '5',
            "-...." to '6', "--..." to '7', "---.." to '8',
            "----." to '9',
            "/" to ' ', ".-.-.-" to '.', "--..--" to ',',
            "..--.." to '?', "-.-.--" to '!', ".----." to '\'',
            ".-..-." to '"', "---..." to ':', "-.-.-." to ';',
            "-...-" to '=', ".-.-." to '+', "-....-" to '-',
            "-..-." to '/', "-.--." to '(', "-.--.-" to ')',
            ".--.-." to '@'
        )

        return input.split(" ").map { code ->
            reverseMorseMap[code]?.toString() ?: "?"
        }.joinToString("")
    }
}
