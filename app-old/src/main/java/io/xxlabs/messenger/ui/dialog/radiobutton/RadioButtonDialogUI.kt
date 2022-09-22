package io.xxlabs.messenger.ui.dialog.radiobutton

import java.io.Serializable

interface RadioButtonDialogUI : Serializable {
    val title: String
    val options: List<RadioButtonDialogOption>

    companion object Factory {
        fun create(title: String, options: List<RadioButtonDialogOption>): RadioButtonDialogUI {
            return object: RadioButtonDialogUI {
                override val title = title
                override val options = options
            }
        }
    }
}

interface RadioButtonDialogOption : Serializable{
    val name: String
    val onClick: () -> Unit

    companion object Factory {
        fun create(name: String, onClick: () -> Unit) : RadioButtonDialogOption {
            return object : RadioButtonDialogOption {
                override val name = name
                override val onClick = onClick
            }
        }
    }
}