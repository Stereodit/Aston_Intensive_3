package com.example.recyclerview.recycler

import com.example.recyclerview.delegate.DelegateAdapterItem

data class ContactModel(
    val contactId: Int,
    val contactName: String,
    val contactSurname: String,
    val contactPhone: String
) : DelegateAdapterItem {

    override fun id(): Any = contactId
    override fun content(): Any = ContactContent(contactName, contactSurname, contactPhone)

    inner class ContactContent(
        val contactName: String,
        val contactSurname: String,
        val contactPhone: String,
    ) {
        override fun equals(other: Any?): Boolean {
            if (other is ContactContent) {
                return contactSurname == other.contactSurname &&
                        contactName == other.contactName &&
                        contactPhone == other.contactPhone
            }
            return false
        }
    }
}