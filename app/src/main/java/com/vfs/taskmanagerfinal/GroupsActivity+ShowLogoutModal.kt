package com.vfs.taskmanagerfinal

import android.view.Gravity
import androidx.appcompat.app.AlertDialog

fun GroupsActivity.showLogoutModal ()
{
    val builder = AlertDialog.Builder(this)

    builder.setTitle("Log Out")
    builder.setMessage("Are you sure you want to log out?")

    builder.setPositiveButton("Logout") { _, _ ->
        Cloud.auth.signOut()
        clearAppData()
        groupAdapter.notifyDataSetChanged()
        checkOnlineStatus()
    }

    builder.setNegativeButton("Cancel") { _, _ ->
    }

    val dialog = builder.create()
    dialog.show()

    dialog.window?.setGravity(Gravity.BOTTOM)
}