package com.example.myapplication

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// 1. DAO (Data Access Object): Emirleri verdiğimiz yer
@Dao
interface FavoriteDao {
    // Favorilere ekle (Zaten varsa üzerine yaz)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(blogItem: BlogItem)

    // Favorilerden sil
    @Delete
    suspend fun removeFavorite(blogItem: BlogItem)

    // Tüm favorileri getir (Canlı veri olarak)
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<BlogItem>>
}

// 2. Database: Veritabanının kendisi
@Database(entities = [BlogItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        // Tekil (Singleton) yapı: Her yerden aynı veritabanına ulaşmak için
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_blog_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}