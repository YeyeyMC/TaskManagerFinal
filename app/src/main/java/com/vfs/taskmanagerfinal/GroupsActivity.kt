package com.vfs.taskmanagerfinal

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class GroupsActivity : AppCompatActivity(), GroupListener
{
    lateinit var statusButton: Button
    lateinit var groupAdapter: GroupsAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.groups_layout)

        Cloud.auth = FirebaseAuth.getInstance()

        statusButton = findViewById(R.id.userStatusButton_id)

        statusButton.setOnClickListener {
            if (Cloud.auth.currentUser != null) {
                showLogoutModal()
            } else {
                showLoginRegisterModal()
            }
        }

        checkOnlineStatus()

        val groupsRv = findViewById<RecyclerView>(R.id.groupsRv_id)
        groupsRv.layoutManager = LinearLayoutManager(this)

        groupAdapter = GroupsAdapter(this)
        groupsRv.adapter = groupAdapter

        if (Cloud.auth.currentUser != null)
        {
            readGroups(groupAdapter)
        }
    }

    // Updates the adapter when the activity is resumed (example: after eliminating a task)
    override fun onResume() {
        super.onResume()
        // Call checkOnlineStatus to update the button when returning from Login/Register
        checkOnlineStatus()
        if (::groupAdapter.isInitialized) {
            groupAdapter.notifyDataSetChanged()
        }
    }

    // Creates a new group
    fun addNewGroup(v: View)
    {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("New Group")
        builder.setMessage("Enter a name for your new group")

        val nameEditText = EditText(this)
        builder.setView(nameEditText)

        builder.setPositiveButton("Add") {_, _ ->
            val newGroup = Group (nameEditText.text.toString(),
                mutableListOf())
            AppData.groups.add(newGroup)
            groupAdapter.notifyDataSetChanged()
            writeGroups(newGroup)
        }

        builder.setNegativeButton("Cancel") { _, _ -> }

        val dialog = builder.create()
        dialog.show()
    }

    // Pop-up menu for groups
    override fun groupLongClicked(index: Int, view: View, group: Group)
    {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        val editItem = popupMenu.menu.findItem(R.id.editTextView_id)
        val deleteItem = popupMenu.menu.findItem(R.id.deleteTextView_id)

        editItem.title = "Edit Group"
        deleteItem.title = "Delete Group"

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId)
            {
                R.id.editTextView_id ->
                {
                    val builder = AlertDialog.Builder(this)

                    builder.setTitle("Edit Group")
                    builder.setMessage("Enter the new name for your group")

                    val nameEditText = EditText(this)
                    builder.setView(nameEditText)

                    builder.setPositiveButton("Add") {_, _ ->
                        val oldGroupName = group.name

                        group.name = nameEditText.text.toString()
                        groupAdapter.notifyDataSetChanged()
                        editGroups(group, oldGroupName)
                    }

                    builder.setNegativeButton("Cancel") { _, _ -> }

                    val dialog = builder.create()
                    dialog.show()

                    true
                }
                R.id.deleteTextView_id ->
                {
                    AppData.groups.removeAt(index)

                    Cloud.db.getReference("users")
                        .child(Cloud.auth.currentUser!!.uid)
                        .child("groups")
                        .child(group.name)
                        .removeValue()
                        .addOnSuccessListener {
                            // Data deleted successfully
                            println("Data deleted successfully")
                            groupAdapter.notifyDataSetChanged()
                            deleteGroups(group)
                            true
                        }
                        .addOnFailureListener { error ->
                            // Handle any errors
                            println("Error deleting data: ${error.message}")
                            false
                        }
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // Goes to the tasks activity layout
    override fun groupClicked(index: Int)
    {
        val intent = Intent (this, TasksActivity::class.java)
        intent.putExtra("index", index) // send the index to the next activity
        startActivity(intent)
    }
}