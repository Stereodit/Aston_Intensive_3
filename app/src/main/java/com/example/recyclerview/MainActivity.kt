package com.example.recyclerview

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
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
import com.example.recyclerview.delegate.CompositeAdapter
import com.example.recyclerview.delegate.DelegateAdapterItem
import com.example.recyclerview.recycler.ContactDelegateAdapter
import com.example.recyclerview.recycler.ContactModel


class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private val compositeAdapter by lazy {
        CompositeAdapter.Builder()
            .add(ContactDelegateAdapter { showInputInfoDialog(it) })
            .build()
    }
    private val itemTouchHelper by lazy {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                compositeAdapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            override fun isLongPressDragEnabled(): Boolean = true
        }
        ItemTouchHelper(itemTouchCallback)
    }

    private lateinit var menu: Menu

    private var isDeleting = false
    private var isDialogShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = getString(R.string.action_bar_title)

        recycler = findViewById(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        itemTouchHelper.attachToRecyclerView(recycler)

        recycler.adapter = compositeAdapter
        compositeAdapter.submitList(appContactList as List<DelegateAdapterItem>)

        if (savedInstanceState != null) {
            compositeAdapter.getDeletingExtension().deleteList = savedInstanceState.getIntegerArrayList("adapterDeleteList")!!
            if (savedInstanceState.getBoolean("isDeleting")) {
                refreshScreen(isDeleting = true, isOnCreate = true)
                isDeleting = true
            } else refreshScreen(isDeleting = false, isOnCreate = true)
            if(savedInstanceState.getBoolean("isDialogShowing")) {
                Toast.makeText(this, getString(R.string.input_info_rotate_warning), Toast.LENGTH_LONG).show()
                isDialogShowing = false
            }
        } else refreshScreen(isDeleting = false, isOnCreate = true)

        findViewById<Button>(R.id.cancel_button).setOnClickListener {
            refreshScreen(isDeleting = false)
        }

        findViewById<Button>(R.id.confirm_button).setOnClickListener {
            compositeAdapter.submitList(compositeAdapter.currentList.toMutableList()
                .filterNot { compositeAdapter.getDeletingExtension().deleteList.contains(it.id()) })
            refreshScreen(isDeleting = false)
        }

        findViewById<Button>(R.id.create_button).setOnClickListener {
            if (compositeAdapter.itemCount < 100) {
                showInputInfoDialog()
            } else Toast.makeText(this,
                getString(R.string.max_count_contact_warning), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.up_tool_bar, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu != null)
            this.menu = menu
        if (isDeleting)
            menu?.findItem(R.id.delete_button)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_button)
            refreshScreen(isDeleting = true)
        return true
    }

    private fun refreshScreen(isDeleting: Boolean, isOnCreate: Boolean = false) {
        findViewById<Button>(R.id.cancel_button).isVisible = isDeleting
        findViewById<Button>(R.id.confirm_button).isVisible = isDeleting
        findViewById<Button>(R.id.create_button).isVisible = !isDeleting
        if (!isOnCreate)
            menu.findItem(R.id.delete_button).isVisible = !isDeleting

        compositeAdapter.getDeletingExtension().setIsDeleting(isDeleting)
        if (!isDeleting)
            compositeAdapter.getDeletingExtension().deleteList.clear()
        compositeAdapter.notifyItemRangeChanged(0, compositeAdapter.itemCount)
    }

    private fun showInputInfoDialog(currentContact: ContactModel? = null) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.input_info_dialog, null)

        val editTextSurname = dialogLayout.findViewById<EditText>(R.id.create_dialog_surname)
        val editTextName = dialogLayout.findViewById<EditText>(R.id.create_dialog_name)
        val editTextPhone = dialogLayout.findViewById<EditText>(R.id.create_dialog_phone)

        if (currentContact != null) {
            editTextSurname.hint = currentContact.contactSurname
            editTextName.hint = currentContact.contactName
            editTextPhone.hint = currentContact.contactPhone
        }

        with(builder) {
            setTitle(if (currentContact != null) getString(R.string.dialog_title_new_info) else getString(R.string.dialog_title_info))
            setPositiveButton(getString(R.string.dialog_button_ok)) { _, _ ->
                val newContact = ContactModel(-1, editTextName.text.toString(), editTextSurname.text.toString(), editTextPhone.text.toString())
                if(currentContact != null)
                    editContact(newContact.copy(contactId = currentContact.contactId))
                else addNewContact(newContact)
                isDialogShowing = false
            }
            setNegativeButton(getString(R.string.dialog_button_cancel)) { _, _ -> isDialogShowing = false }
            setView(dialogLayout)
            show()
        }
        isDialogShowing = true
    }

    private fun addNewContact(newContact: ContactModel) {
        val intRange = (1..100).toMutableList()
        compositeAdapter.currentList.forEach { intRange.remove(it.id()) }
        compositeAdapter.submitList(compositeAdapter.currentList.toMutableList().also {
            it.add(intRange.first() - 1, newContact.copy(contactId = intRange.first()))
        })
        compositeAdapter.notifyItemRangeChanged(intRange.first() - 1, compositeAdapter.itemCount)
    }

    private fun editContact(newContact: ContactModel) {
        val index = compositeAdapter.currentList.indexOfFirst { it.id() == newContact.contactId }
        compositeAdapter.submitList(
            compositeAdapter.currentList.toMutableList().also {
                it[index] = newContact
            })
        compositeAdapter.notifyItemChanged(index)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isDeleting", compositeAdapter.getDeletingExtension().getIsDeleting())
        outState.putBoolean("isDialogShowing", isDialogShowing)
        outState.putIntegerArrayList("adapterDeleteList", ArrayList<Int>(compositeAdapter.getDeletingExtension().deleteList))
        appContactList = compositeAdapter.currentList as MutableList<ContactModel>
    }
}