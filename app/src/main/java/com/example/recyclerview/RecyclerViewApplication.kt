package com.example.recyclerview

import android.app.Application
import com.example.recyclerview.data.ContactCollection
import com.example.recyclerview.recycler.ContactModel

class RecyclerViewApplication : Application() {
    companion object {
        lateinit var appContactList: MutableList<ContactModel>
    }

    override fun onCreate() {
        super.onCreate()
        appContactList = ContactCollection.hardList.toMutableList()
    }
}