package io.xxlabs.messenger.bindings.wrapper.report

import com.google.gson.Gson
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListMock
import io.xxlabs.messenger.support.util.Utils
import kotlin.random.Random

class SendReportMock : SendReportBase {
    override fun getMessageId(): ByteArray {
        return Random.nextBytes(32)
    }

    override fun getRoundList(): RoundListBase {
        return RoundListMock()
    }

    override fun getTimestampMs(): Long {
        return Utils.getCurrentTimeStamp()
    }

    override fun getTimestampNano(): Long {
        return Utils.getCurrentTimeStampNano()
    }

    override fun marshal(): ByteArray {
        val gson = Gson()
        val json = gson.toJson(this)
        return json.encodeToByteArray()
    }

    override fun getRoundUrl(): String = "www.google.com"
}