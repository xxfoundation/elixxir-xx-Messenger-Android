package io.xxlabs.messenger.bindings.wrapper.groups.id

class IdListBindings(val list: List<ByteArray>) : IdListBase {
    private val _list = list.toMutableList()
    override fun add(id: ByteArray) {
        _list.add(id)
    }

    override fun get(id: Long): ByteArray {
        return _list[id.toInt()]
    }

    override fun len(): Long {
        return _list.size.toLong()
    }
}