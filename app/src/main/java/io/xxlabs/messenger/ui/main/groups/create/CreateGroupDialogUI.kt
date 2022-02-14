package io.xxlabs.messenger.ui.main.groups.create

interface CreateGroupDialogUI {
    val body: String
    var name: String?
    var description: String?
    val buttonClick: (name: String?, description: String?) -> Unit
    val infoClick: () -> Unit

    companion object Factory {
        fun create(
            body: String,
            onCreateClicked: (String?, String?) -> Unit,
            onInfoClicked: () -> Unit
        ) : CreateGroupDialogUI {
            return object : CreateGroupDialogUI {
                override val body = body
                override val buttonClick: (String?, String?) -> Unit = onCreateClicked
                override val infoClick = onInfoClicked
                override var name: String? = null
                override var description: String? = null
            }
        }
    }
}