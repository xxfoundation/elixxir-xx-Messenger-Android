package io.elixxir.feature.home

import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val INTENT_NOTIFICATION_CLICK = "nav_bundle"
        const val INTENT_PRIVATE_CHAT = "private_message"
        const val INTENT_GROUP_CHAT = "group_message"
        const val INTENT_REQUEST = "request"
        const val INTENT_INVITATION = "invitation"
    }
}