package com.vfs.taskmanagerfinal

import android.content.Intent
import android.view.Gravity
import androidx.appcompat.app.AlertDialog

fun GroupsActivity.showLoginRegisterModal ()
{
    val builder = AlertDialog.Builder(this)

    builder.setTitle("Login or Register")
    builder.setMessage("Would you like to login or register?")

    builder.setPositiveButton("Login") { _, _ ->
        val intent = Intent (this, LoginRegisterActivity::class.java)
        intent.putExtra("type", "login")
        startActivity(intent)
    }

    builder.setNeutralButton("Register") { _, _ ->
        val intent = Intent (this, LoginRegisterActivity::class.java)
        intent.putExtra("type", "registration")
        startActivity(intent)
    }

    builder.setNegativeButton("Cancel") { _, _ ->
    }

    val dialog = builder.create()
    dialog.show()

    dialog.window?.setGravity(Gravity.BOTTOM)
}