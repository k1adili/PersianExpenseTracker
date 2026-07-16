package com.rialtracker.expense.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * گروه (دسته‌بندی) مخارج، مثل خوراک، حمل‌ونقل، قبوض و ...
 * colorHex رنگ پاستیلی اختصاص‌یافته به این دسته برای نمودارها و لیست‌هاست.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val icon: String = "tag", // شناسه‌ی آیکون از میان مجموعه‌ی از پیش تعریف‌شده در UI
    val isDefault: Boolean = false
)

/** لیست دسته‌بندی‌های پیش‌فرض که در اولین اجرای برنامه ساخته می‌شوند */
object DefaultCategories {
    fun seed(): List<Category> = listOf(
        Category(name = "خوراک و سوپرمارکت", colorHex = "#FFD6BA", icon = "food", isDefault = true),
        Category(name = "حمل‌ونقل", colorHex = "#C7E9FF", icon = "car", isDefault = true),
        Category(name = "قبوض و اشتراک", colorHex = "#D9F2D0", icon = "bill", isDefault = true),
        Category(name = "خرید و پوشاک", colorHex = "#FBD6E5", icon = "shopping", isDefault = true),
        Category(name = "سلامت و درمان", colorHex = "#E3D6FF", icon = "health", isDefault = true),
        Category(name = "تفریح و رستوران", colorHex = "#FFF0B8", icon = "fun", isDefault = true),
        Category(name = "مسکن و اجاره", colorHex = "#D6EFF2", icon = "home", isDefault = true),
        Category(name = "آموزش", colorHex = "#E8E2D0", icon = "book", isDefault = true),
        Category(name = "متفرقه", colorHex = "#E6E6E6", icon = "other", isDefault = true)
    )
}
