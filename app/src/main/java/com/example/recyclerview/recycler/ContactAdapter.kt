package com.example.recyclerview.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.recyclerview.databinding.ContactItemBinding

class ContactAdapter(
    private val onClickAction: (ContactModel) -> Unit
) : ListAdapter<ContactModel, ContactAdapter.ContactViewHolder>(ContactDiffUtil) {

    private var isDeleting = false
    var deleteList: MutableList<Int> = mutableListOf()

    fun setIsDeleting(value: Boolean) {
        isDeleting = value
    }

    fun getIsDeleting() = isDeleting

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ContactItemBinding.inflate(inflater, parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model)

        holder.binding.contactDeleteChecking.isChecked = deleteList.contains(getItem(position).contactId)
        holder.binding.contactDeleteChecking.isVisible = isDeleting

        holder.itemView.setOnClickListener {
            if(isDeleting) {
                if (deleteList.contains(holder.binding.contactId.text.toString().toInt())) {
                    deleteList.removeAll { it == holder.binding.contactId.text.toString().toInt() }
                    holder.binding.contactDeleteChecking.isChecked = false
                } else {
                    deleteList.add(holder.binding.contactId.text.toString().toInt())
                    holder.binding.contactDeleteChecking.isChecked = true
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

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val list = currentList.toMutableList()
        val fromItem = list[fromPosition]
        list.removeAt(fromPosition)
        if (toPosition < fromPosition) {
            list.add(toPosition + 1 , fromItem)
        } else {
            list.add(toPosition - 1, fromItem)
        }

        submitList(list)
    }
}