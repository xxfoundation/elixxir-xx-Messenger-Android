package io.xxlabs.messenger.support.misc

import com.google.gson.JsonParser
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.appContext
import timber.log.Timber

class DummyGenerator {
    companion object {
        fun getMessageDummy(): String {
            var message = ""
            appContext().resources.openRawResource(R.raw.messages).bufferedReader().use {
                val jsonText = it.readText()
                val jsonArray = JsonParser
                    .parseString(jsonText)
                    .asJsonArray

                val jsonObj = jsonArray.get((0 until jsonArray.size())
                    .shuffled()
                    .random())
                    .asJsonObject

                message = jsonObj.toString()
            }

            Timber.v(message)
            return message
        }
    }
}