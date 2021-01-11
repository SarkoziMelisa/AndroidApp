package myapp.todo.history.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import myapp.todo.history.data.Actor

@Dao
interface ActorDao {
    @Query("SELECT * from actors ORDER BY name ASC")
    fun getAll(): LiveData<List<Actor>>

    @Query("SELECT * FROM actors WHERE _id=:id ")
    fun getById(id: String): LiveData<Actor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(actor: Actor)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(actor: Actor)

    @Query("DELETE FROM actors")
    suspend fun deleteAll()

    @Query("DELETE FROM actors WHERE _id=:id")
    suspend fun delete(id: String)
}