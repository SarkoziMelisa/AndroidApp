package myapp.todo.history.data

import androidx.lifecycle.LiveData
import myapp.todo.history.data.local.ActorDao
import myapp.core.Result
import myapp.todo.history.data.remote.ActorApi

class ActorRepository(private val actorDao: ActorDao) {

    val actors = actorDao.getAll()

    suspend fun refresh(): Result<Boolean> {
        try {
            val actors = ActorApi.service.find()
            for (actor in actors) {
                actorDao.insert(actor)
            }
            return Result.Success(true)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    fun getById(itemId: String): LiveData<Actor> {
        return actorDao.getById(itemId)
    }

    suspend fun save(actor: Actor): Result<Actor> {
        try {
            val createdItem = ActorApi.service.create(actor)
            actorDao.insert(createdItem)
            return Result.Success(createdItem)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun update(actor: Actor): Result<Actor> {
        try {
            val updatedItem = ActorApi.service.update(actor._id, actor)
            actorDao.update(updatedItem)
            return Result.Success(updatedItem)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun delete(actor: Actor): Result<Actor> {
        try {
            val deletedItem = ActorApi.service.delete(actor._id)
            actorDao.delete(actor._id)
            return Result.Success(deletedItem)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }
}