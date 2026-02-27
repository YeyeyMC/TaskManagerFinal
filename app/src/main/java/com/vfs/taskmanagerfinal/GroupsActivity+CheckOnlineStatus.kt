package com.vfs.taskmanagerfinal

import android.graphics.Color

fun GroupsActivity.checkOnlineStatus ()
{
    statusButton.text = "We are offline"
    statusButton.setBackgroundColor(Color.YELLOW)

    Cloud.auth.currentUser?.let {
        val name = if (it.displayName.isNullOrEmpty()) it.email else it.displayName
        statusButton.text = "$name is Online"
        statusButton.setBackgroundColor(Color.GREEN)
    }
}