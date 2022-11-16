package io.xxlabs.messenger.data.datatype

enum class FactType(val value: Long) {
    USERNAME(0),
    EMAIL(1),
    PHONE(2),
    NICKNAME(3);

    companion object  {
        fun from(value: Long): FactType? = values().firstOrNull { it.value == value }
    }
}