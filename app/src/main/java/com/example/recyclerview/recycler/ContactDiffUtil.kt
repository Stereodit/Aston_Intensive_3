package com.example.recyclerview.recycler

import androidx.recyclerview.widget.DiffUtil

object ContactDiffUtil : DiffUtil.ItemCallback<ContactModel>() {

    override fun areItemsTheSame(oldItem: ContactModel, newItem: ContactModel): Boolean {
        return oldItem.contactId == newItem.contactId
    }

    override fun areContentsTheSame(oldItem: ContactModel, newItem: ContactModel): Boolean {
        return oldItem.contactSurname == newItem.contactSurname &&
                oldItem.contactName == newItem.contactName &&
                oldItem.contactPhone == newItem.contactPhone
    }
}