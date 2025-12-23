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


@Dao
interface FavoriteDao {
  
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(blogItem: BlogItem)

   
    @Delete
    suspend fun removeFavorite(blogItem: BlogItem)

    
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<BlogItem>>
}


@Database(entities = [BlogItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        
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
