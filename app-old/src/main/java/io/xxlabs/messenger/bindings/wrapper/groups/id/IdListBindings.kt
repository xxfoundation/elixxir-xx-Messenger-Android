package io.xxlabs.messenger.bindings.wrapper.groups.id

import bindings.IdList

class IdListBindings(val idList: IdList) : IdListBase {
    override fun add(id: ByteArray) {
        idList.add(id)
    }

    override fun get(id: Long): ByteArray {
        return idList.get(id)
    }

    override fun len(): Long {
        return idList.len()
    }
}