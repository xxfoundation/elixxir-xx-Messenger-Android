package io.xxlabs.messenger.data.datatype

enum class MsgType(val value: Int) {
    NO_TYPE(0),
    TEXT_MESSAGE(2),
    FRIEND_REQUEST(45),
    DELETE_REQUEST(46)
}