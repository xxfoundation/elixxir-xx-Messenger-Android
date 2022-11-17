package io.xxlabs.messenger.data.datatype

enum class FactType(val value: Int) {
    USERNAME(0),
    EMAIL(1),
    PHONE(2),
    NICKNAME(3);

    companion object  {
        fun from(value: Int): FactType? = values().firstOrNull { it.value == value }
    }
}