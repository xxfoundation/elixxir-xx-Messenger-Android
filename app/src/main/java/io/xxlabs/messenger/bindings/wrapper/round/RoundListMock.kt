package io.xxlabs.messenger.bindings.wrapper.round

class RoundListMock : RoundListBase {
    override fun get(index: Long): Long {
        return 0
    }

    override fun len(): Long {
        TODO("Not yet implemented")
    }

    override fun toList(): List<Long> {
        TODO("Not yet implemented")
    }
}