package doist.ffs.ext

import kotlin.collections.toMutableList

fun <T> Collection<T>?.toMutableList(): MutableList<T> = orEmpty().toMutableList()
