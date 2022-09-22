package io.xxlabs.messenger

import kotlin.random.Random

val numPool = ('0'..'9').toList()
val capsPool = ('A'..'Z').toList()
val lowerPool = ('a'..'z').toList()
val charPool: List<Char> = lowerPool + capsPool + numPool

/**
 * Returns a random alphanumeric String of [length] characters.
 */
fun randomString(length: Int = 8): String =
    (1..length)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")

fun randomCaps(length: Int = 1): String =
    (1..length)
        .map { Random.nextInt(0, capsPool.size) }
        .map(capsPool::get)
        .joinToString("")
fun randomLower(length: Int = 1): String =
    (1..length)
        .map { Random.nextInt(0, lowerPool.size) }
        .map(lowerPool::get)
        .joinToString("")
fun randomNum(length: Int = 1): String =
    (1..length)
        .map { Random.nextInt(0, numPool.size) }
        .map(numPool::get)
        .joinToString("")