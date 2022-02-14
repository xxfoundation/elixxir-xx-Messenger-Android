package io.xxlabs.messenger.test.utils

import android.text.Editable
import android.text.InputFilter
import java.util.stream.IntStream

internal class MockEditable(private val str: String) : Editable {
    override fun toString(): String {
        return str
    }

    override fun chars(): IntStream? {
        return null
    }

    override val length: Int
        get() = str.length

    override fun get(index: Int): Char {
        return str[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return str.subSequence(startIndex, endIndex)
    }

    override fun replace(i: Int, i1: Int, charSequence: CharSequence, i2: Int, i3: Int): Editable {
        return this
    }

    override fun replace(i: Int, i1: Int, charSequence: CharSequence): Editable {
        return this
    }

    override fun insert(i: Int, charSequence: CharSequence, i1: Int, i2: Int): Editable {
        return this
    }

    override fun insert(i: Int, charSequence: CharSequence): Editable {
        return this
    }

    override fun delete(i: Int, i1: Int): Editable {
        return this
    }

    override fun append(charSequence: CharSequence): Editable {
        return this
    }

    override fun append(charSequence: CharSequence, i: Int, i1: Int): Editable {
        return this
    }

    override fun append(c: Char): Editable {
        return this
    }

    override fun clear() {}
    override fun clearSpans() {}
    override fun setFilters(filters: Array<InputFilter>) {}
    override fun getFilters(): Array<InputFilter?> {
        return arrayOfNulls(0)
    }

    override fun getChars(i: Int, i1: Int, chars: CharArray, i2: Int) {}
    override fun setSpan(o: Any, i: Int, i1: Int, i2: Int) {}
    override fun removeSpan(o: Any) {}
    override fun <T> getSpans(i: Int, i1: Int, aClass: Class<T>): Array<T>? {
        return null
    }

    override fun getSpanStart(o: Any): Int {
        return 0
    }

    override fun getSpanEnd(o: Any): Int {
        return 0
    }

    override fun getSpanFlags(o: Any): Int {
        return 0
    }

    override fun nextSpanTransition(i: Int, i1: Int, aClass: Class<*>?): Int {
        return 0
    }
}