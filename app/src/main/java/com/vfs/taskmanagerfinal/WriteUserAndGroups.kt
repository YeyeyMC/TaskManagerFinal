package com.vfs.taskmanagerfinal

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

fun writeUserObjAfterRegistration ()
{
    Cloud.auth.currentUser?.let { firebaseUser ->
        val groupsMap = AppData.groups.associate { group ->
            group.name to mapOf(
                "name" to group.name,
                "tasks" to group.tasks.associateBy { it.name }
            )
        }

        val userData = mapOf(
            "id" to firebaseUser.uid,
            "name" to firebaseUser.displayName,
            "email" to firebaseUser.email,
            "groups" to groupsMap
        )

        val completionListener = DatabaseReference.CompletionListener { error, _ ->
            if (error != null) {
                Log.e("CloudTag", "write failed: $error")
            }
        }

        Cloud.db.getReference("users")
            .child(firebaseUser.uid)
            .setValue(userData, completionListener)
    }
}

fun writeGroups (group: Group)
{
    Cloud.auth.currentUser?.let {
        val groupData = mapOf(
            "name" to group.name,
            "tasks" to group.tasks.associateBy { it.name }
        )
        
        Cloud.db.getReference("users")
            .child(it.uid)
            .child("groups")
            .child(group.name)
            .setValue(groupData)

        Cloud.db.getReference("groups")
            .child(group.name)
            .setValue(group)
    }
}

fun editGroups (group: Group, oldGroupName: String)
{
    Cloud.auth.currentUser?.let {
        val groupsRef = Cloud.db.getReference("users")
            .child(it.uid)
            .child("groups")

        if (group.name != oldGroupName) {
            groupsRef.child(oldGroupName).removeValue()
        }

        val groupData = mapOf(
            "name" to group.name,
            "tasks" to group.tasks.associateBy { it.name }
        )

        groupsRef.child(group.name).setValue(groupData)
    }
}

fun deleteGroups (group: Group)
{
    Cloud.auth.currentUser?.let {
        Cloud.db.getReference("users")
            .child(it.uid)
            .child("groups")
            .child(group.name)
            .removeValue()
    }
}

fun readGroups (adapter: GroupsAdapter? = null)
{
    Cloud.auth.currentUser?.let {
        Cloud.db.reference
            .child("users")
            .child(it.uid)
            .child("groups")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    AppData.groups.clear()

                    if (snapshot.exists()) {
                        for (groupSnapshot in snapshot.children) {
                            val name = groupSnapshot.child("name").getValue(String::class.java) ?: ""

                            val tasks = mutableListOf<Task>()
                            groupSnapshot.child("tasks").children.forEach { taskSnap ->
                                taskSnap.getValue(Task::class.java)?.let { tasks.add(it) }
                            }

                            AppData.groups.add(Group(name, tasks))
                        }
                    }
                    adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        Cloud.db.reference
            .child("groups")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        for (groupSnapshot in snapshot.children) {
                            val name = groupSnapshot.child("name").getValue(String::class.java) ?: ""

                            val tasks = mutableListOf<Task>()
                            groupSnapshot.child("tasks").children.forEach { taskSnap ->
                                taskSnap.getValue(Task::class.java)?.let { tasks.add(it) }
                            }

                            AppData.groups.add(Group(name, tasks))
                        }
                    }
                    adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}

fun writeTasks (task: Task, group: Group)
{
    Cloud.auth.currentUser?.let {
        Cloud.db.getReference("users")
            .child(it.uid)
            .child("groups")
            .child(group.name)
            .child("tasks")
            .child(task.name)
            .setValue(task)
    }
}

fun editTasks (task: Task, group: Group, oldTaskName: String)
{
    Cloud.auth.currentUser?.let {
        val myRef = Cloud.db.getReference("users")
            .child(it.uid)
            .child("groups")
            .child(group.name)
            .child("tasks")

        if (task.name != oldTaskName) {
            myRef.child(oldTaskName).removeValue()
        }

        myRef.child(task.name).setValue(task)
    }
}

fun deleteTasks (task: Task, group: Group)
{
    Cloud.auth.currentUser?.let {
        Cloud.db.getReference("users")
            .child(it.uid)
            .child("groups")
            .child(group.name)
            .child("tasks")
            .child(task.name)
            .removeValue()
    }
}

fun updateCheckBoxTask (task: Task, group: Group)
{
    Cloud.auth.currentUser?.let {
        Cloud.db.getReference("users")
            .child(it.uid)
            .child("groups")
            .child(group.name)
            .child("tasks")
            .child(task.name)
            .child("completed")
            .setValue(task.completed)
    }
}

fun clearAppData()
{
    AppData.groups.clear()
}
