package io.xxlabs.messenger.support.selection

interface BaseSelectionObserver<K> {
    fun onItemStateChanged(key: K, selected: Boolean) {}
    fun onEnterChoiceMode() {}
    fun onExitChoiceMode() {}
    fun onSelectionCleared() {}
    fun onSelectionRefresh() {}
    fun onSelectionChanged(list: List<K>) {}
    fun onSelectionRestored() {}
}