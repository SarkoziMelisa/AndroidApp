package myapp.todo.history.actor

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import myapp.todo.history.data.local.TodoDatabase
import myapp.core.Result
import myapp.core.TAG
import myapp.todo.history.data.Actor
import myapp.todo.history.data.ActorRepository

class ActorEditViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val fetching: LiveData<Boolean> = mutableFetching
    val fetchingError: LiveData<Exception> = mutableException
    val completed: LiveData<Boolean> = mutableCompleted

    val actorRepository: ActorRepository

    init {
        val actorDao = TodoDatabase.getDatabase(application, viewModelScope).actorDao()
        actorRepository = ActorRepository(actorDao)
    }

    fun getItemById(itemId: String): LiveData<Actor> {
        Log.v(TAG, "getItemById...")
        return actorRepository.getById(itemId)
    }

    fun saveOrUpdateActor(actor: Actor) {
        viewModelScope.launch {
            Log.v(TAG, "saveOrUpdateActor...");
            mutableFetching.value = true
            mutableException.value = null
            val result: Result<Actor>
            if (actor._id.isNotEmpty()) {
                result = actorRepository.update(actor)
            } else {
                result = actorRepository.save(actor)
            }
            when(result) {
                is Result.Success -> {
                    Log.d(TAG, "saveOrUpdateActor succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "saveOrUpdateActor failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableCompleted.value = true
            mutableFetching.value = false
        }
    }

    fun deleteActor(actor: Actor) {
        viewModelScope.launch {
            Log.v(TAG, "deleteActor...");
            mutableFetching.value = true
            mutableException.value = null
            val result: Result<Actor>
            if (actor._id.isNotEmpty()) {
                result = actorRepository.delete(actor)
                when (result) {
                    is Result.Success -> {
                        Log.d(TAG, "deleteActor succeeded");
                    }
                    is Result.Error -> {
                        Log.w(TAG, "deleteActor failed", result.exception);
                        mutableException.value = result.exception
                    }
                }
                mutableCompleted.value = true
                mutableFetching.value = false
            }
        }
    }
}