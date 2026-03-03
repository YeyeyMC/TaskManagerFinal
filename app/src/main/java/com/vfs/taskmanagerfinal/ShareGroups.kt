package com.vfs.taskmanagerfinal

fun shareGroups (group: Group)
{
    // Simply move the group to a global "public_groups" node
    Cloud.db.getReference("public_groups")
        .child(group.name)
        .setValue(group)
}
