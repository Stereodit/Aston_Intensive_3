package com.example.recyclerview.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.recyclerview.databinding.ContactItemBinding
import com.example.recyclerview.delegate.DelegateAdapter
import com.example.recyclerview.delegate.DelegateAdapterItem
import com.example.recyclerview.delegate.DelegateDeletingExtension

class ContactDelegateAdapter(
    private val onClickAction: (ContactModel) -> Unit
) : DelegateAdapter<ContactModel, ContactDelegateAdapter.ContactViewHolder>(ContactModel::class.java) {

    override fun createViewHolder(parent: ViewGroup): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ContactItemBinding.inflate(inflater, parent, false)
        return ContactViewHolder(binding)
    }

    override fun bindViewHolder(
        model: ContactModel,
        viewHolder: ContactViewHolder,
        payloads: List<DelegateAdapterItem.Payloadable>,
        deletingExtension: DelegateDeletingExtension
    ) {
        viewHolder.bind(model)

        viewHolder.binding.contactDeleteChecking.isChecked = deletingExtension.deleteList.contains(model.contactId)
        viewHolder.binding.contactDeleteChecking.isVisible = deletingExtension.getIsDeleting()

        viewHolder.itemView.setOnClickListener {
            if (deletingExtension.getIsDeleting()) {
                if (deletingExtension.deleteList.contains(viewHolder.binding.contactId.text.toString().toInt())) {
                    deletingExtension.deleteList.removeAll { it == viewHolder.binding.contactId.text.toString().toInt() }
                    viewHolder.binding.contactDeleteChecking.isChecked = false
                } else {
                    deletingExtension.deleteList.add(viewHolder.binding.contactId.text.toString().toInt())
                    viewHolder.binding.contactDeleteChecking.isChecked = true
                }
            } else onClickAction.invoke(model)
        }
    }

    inner class ContactViewHolder(val binding: ContactItemBinding) : ViewHolder(binding.root) {
        fun bind(model: ContactModel) {
            binding.contactTitle.text = "${model.contactSurname} ${model.contactName}"
            binding.contactPhone.text = model.contactPhone
            binding.contactId.text = model.contactId.toString()
        }
    }
}