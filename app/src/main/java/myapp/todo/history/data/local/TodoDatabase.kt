package myapp.todo.history.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import myapp.todo.history.data.Actor
import myapp.todo.history.data.remote.ActorApi

@Database(entities = [Actor::class], version = 2, exportSchema = true)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun actorDao(): ActorDao

    companion object {
        @Volatile
        private var INSTANCE: TodoDatabase? = null

        //        @kotlinx.coroutines.InternalCoroutinesApi()
        fun getDatabase(context: Context, scope: CoroutineScope): TodoDatabase {
            val inst = INSTANCE
            if (inst != null) {
                return inst
            }
            val instance =
                Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_db"
                )
                    .addCallback(WordDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
            INSTANCE = instance
            return instance
        }

        private class WordDatabaseCallback(private val scope: CoroutineScope) :
            RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.actorDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(actorDao: ActorDao) {
            actorDao.deleteAll()
            val actors = ActorApi.service.find()
            for (actor in actors) {
                actorDao.insert(actor)
            }
        }
    }

}
