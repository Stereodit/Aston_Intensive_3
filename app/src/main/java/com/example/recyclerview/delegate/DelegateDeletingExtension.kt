package com.example.recyclerview.delegate

class DelegateDeletingExtension(
    private var isDeleting: Boolean = false,
    var deleteList: MutableList<Int> = mutableListOf()
) {
    fun setIsDeleting(value: Boolean) { isDeleting = value }
    fun getIsDeleting() = isDeleting
}