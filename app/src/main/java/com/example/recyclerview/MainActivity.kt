package com.example.recyclerview

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recyclerview.RecyclerViewApplication.Companion.appContactList
import com.example.recyclerview.recycler.ContactAdapter
import com.example.recyclerview.recycler.ContactModel


class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ContactAdapter
    private lateinit var recycler: RecyclerView
    private val itemTouchHelper by lazy {
        val itemTouchCallback = object: ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }
            override fun isLongPressDragEnabled(): Boolean = true
        }
        ItemTouchHelper(itemTouchCallback)
    }
    private var isDeleting = false
    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler = findViewById(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        itemTouchHelper.attachToRecyclerView(recycler)

        adapter = ContactAdapter { showEditDialog(it) }
        adapter.submitList(appContactList)
        recycler.adapter = adapter

        if(savedInstanceState?.getIntegerArrayList("adapterDeleteList") != null)
            adapter.deleteList = savedInstanceState.getIntegerArrayList("adapterDeleteList")!!

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "Contacts"

        findViewById<Button>(R.id.cancel_button).isVisible = false
        findViewById<RecyclerView>(R.id.confirm_button).isVisible = false

        if(savedInstanceState?.getBoolean("isDeleting") == true) {
            refreshScreen(isDeleting = true, isRestoreState = true)
            isDeleting = true
        }

        findViewById<Button>(R.id.cancel_button).setOnClickListener {
            refreshScreen(isDeleting = false)
        }

        findViewById<Button>(R.id.confirm_button).setOnClickListener {
            adapter.submitList(adapter.currentList.toMutableList().filterNot { adapter.deleteList.contains(it.contactId) })
            refreshScreen(isDeleting = false)
        }

        findViewById<Button>(R.id.create_button).setOnClickListener {
            if(adapter.itemCount < 100) {
                showCreateDialog()
            } else Toast.makeText(this, "No more than 100.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshScreen(isDeleting: Boolean, isRestoreState: Boolean = false) {
        findViewById<Button>(R.id.cancel_button).isVisible = isDeleting
        findViewById<Button>(R.id.confirm_button).isVisible = isDeleting
        findViewById<Button>(R.id.create_button).isVisible = !isDeleting
        if(!isRestoreState)
            menu.findItem(R.id.delete_button).isVisible = !isDeleting

        adapter.setIsDeleting(isDeleting)
        if(!isDeleting)
            adapter.deleteList.clear()
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    private fun showCreateDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.create_dialog_layout, null)
        val etSurname = dialogLayout.findViewById<TextView>(R.id.create_dialog_surname)
        val etName = dialogLayout.findViewById<TextView>(R.id.create_dialog_name)
        val etPhone = dialogLayout.findViewById<TextView>(R.id.create_dialog_phone)

        with(builder) {
            setTitle("Please, enter info:")
            setPositiveButton("Ok") { _, _ ->
                addNewContact(
                    ContactModel(
                        -1,
                        etName.text.toString(),
                        etSurname.text.toString(),
                        etPhone.text.toString()
                    )
                )
            }
            setNegativeButton("Cancel") { _, _ -> }
            setView(dialogLayout)
            show()
        }
    }

    private fun addNewContact(newContact: ContactModel) {
        val intRange = mutableListOf<Int>()
        repeat(100) { intRange.add(it + 1) }
        adapter.currentList.forEach { intRange.remove(it.contactId) }
        adapter.submitList(adapter.currentList.toMutableList().also {
            it.add(intRange.first() - 1, newContact.copy(contactId = intRange.first()))
        })
        adapter.notifyItemRangeChanged(intRange.first() - 1, adapter.itemCount)
    }

    private fun showEditDialog(currentContact: ContactModel) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.create_dialog_layout, null)
        val etSurname = dialogLayout.findViewById<TextView>(R.id.create_dialog_surname)
        val etName = dialogLayout.findViewById<TextView>(R.id.create_dialog_name)
        val etPhone = dialogLayout.findViewById<TextView>(R.id.create_dialog_phone)

        etSurname.hint = currentContact.contactSurname
        etName.hint = currentContact.contactName
        etPhone.hint = currentContact.contactPhone

        with(builder) {
            setTitle("Please, enter new info:")
            setPositiveButton("Ok") { _, _ ->
                editContact(
                    ContactModel(
                        currentContact.contactId,
                        etName.text.toString(),
                        etSurname.text.toString(),
                        etPhone.text.toString()
                    )
                )
            }
            setNegativeButton("Cancel") { _, _ -> }
            setView(dialogLayout)
            show()
        }
    }

    private fun editContact(newContact: ContactModel) {
        val index = adapter.currentList.indexOfFirst { it.contactId == newContact.contactId }
        adapter.submitList(
            adapter.currentList.toMutableList().also {
            it[index] = newContact
        })
        adapter.notifyItemChanged(index)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.up_tool_bar, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(isDeleting)
            menu?.findItem(R.id.delete_button)?.isVisible = false
        if(menu != null)
            this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_button)
            refreshScreen(isDeleting = true)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isDeleting", adapter.getIsDeleting())
        outState.putIntegerArrayList("adapterDeleteList", ArrayList<Int>(adapter.deleteList))
        appContactList = adapter.currentList
    }
}