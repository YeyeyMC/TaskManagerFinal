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
        // Read private user groups
        Cloud.db.reference
            .child("users")
            .child(it.uid)
            .child("groups")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // We only clear if we are starting a fresh read or managing both nodes
                    // For simplicity, let's just clear and rebuild the list from both nodes
                    refreshAllGroups(adapter)
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // Read public groups
        Cloud.db.reference
            .child("public_groups")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    refreshAllGroups(adapter)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}

private fun refreshAllGroups(adapter: GroupsAdapter?) {
    val currentUser = Cloud.auth.currentUser ?: return
    
    // First, fetch private groups
    Cloud.db.reference.child("users").child(currentUser.uid).child("groups").get().addOnSuccessListener { privateSnap ->
        val allGroups = mutableListOf<Group>()
        
        if (privateSnap.exists()) {
            for (groupSnap in privateSnap.children) {
                parseGroup(groupSnap)?.let { allGroups.add(it) }
            }
        }
        
        // Then, fetch public groups
        Cloud.db.reference.child("public_groups").get().addOnSuccessListener { publicSnap ->
            if (publicSnap.exists()) {
                for (groupSnap in publicSnap.children) {
                    val publicGroup = parseGroup(groupSnap)
                    // Avoid duplicates if a private group was shared
                    if (publicGroup != null && allGroups.none { it.name == publicGroup.name }) {
                        allGroups.add(publicGroup)
                    }
                }
            }
            AppData.groups.clear()
            AppData.groups.addAll(allGroups)
            adapter?.notifyDataSetChanged()
        }
    }
}

private fun parseGroup(snapshot: DataSnapshot): Group? {
    val name = snapshot.child("name").getValue(String::class.java) ?: return null
    val tasks = mutableListOf<Task>()
    snapshot.child("tasks").children.forEach { taskSnap ->
        taskSnap.getValue(Task::class.java)?.let { tasks.add(it) }
    }
    return Group(name, tasks)
}

fun writeTasks (task: Task, group: Group)
{
    // Try updating in private user node
    Cloud.auth.currentUser?.let {
        Cloud.db.getReference("users")
            .child(it.uid)
            .child("groups")
            .child(group.name)
            .child("tasks")
            .child(task.name)
            .setValue(task)
    }
    // Also try updating in public node if it exists there
    Cloud.db.getReference("public_groups")
        .child(group.name)
        .child("tasks")
        .child(task.name)
        .setValue(task)
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
    
    val publicRef = Cloud.db.getReference("public_groups")
        .child(group.name)
        .child("tasks")
    if (task.name != oldTaskName) {
        publicRef.child(oldTaskName).removeValue()
    }
    publicRef.child(task.name).setValue(task)
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
    Cloud.db.getReference("public_groups")
        .child(group.name)
        .child("tasks")
        .child(task.name)
        .removeValue()
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
    Cloud.db.getReference("public_groups")
        .child(group.name)
        .child("tasks")
        .child(task.name)
        .child("completed")
        .setValue(task.completed)
}

fun clearAppData()
{
    AppData.groups.clear()
}
