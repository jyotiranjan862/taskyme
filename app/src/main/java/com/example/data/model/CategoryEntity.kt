package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
) {
    companion object {
        fun defaultCategories(): List<CategoryEntity> = listOf(
            CategoryEntity(id = 1, name = "Work", colorHex = "#4F75FF", iconName = "Work", isDefault = true, sortOrder = 0),
            CategoryEntity(id = 2, name = "Personal", colorHex = "#00D2B4", iconName = "Person", isDefault = true, sortOrder = 1),
            CategoryEntity(id = 3, name = "Learning", colorHex = "#FFB800", iconName = "School", isDefault = true, sortOrder = 2),
            CategoryEntity(id = 4, name = "Health", colorHex = "#27C278", iconName = "FitnessCenter", isDefault = true, sortOrder = 3),
            CategoryEntity(id = 5, name = "Finance", colorHex = "#A259FF", iconName = "AccountBalance", isDefault = true, sortOrder = 4),
            CategoryEntity(id = 6, name = "Other", colorHex = "#8A98A8", iconName = "Category", isDefault = true, sortOrder = 5)
        )
    }
}
