package com.finflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadlineEpochDay: Long? = null,
    val icon: String = "🎯",
    val colorHex: String = "#6C63FF",
    val isArchived: Boolean = false,
)
