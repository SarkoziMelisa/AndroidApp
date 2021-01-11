package myapp.todo.history.actors

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import myapp.todo.history.data.local.TodoDatabase
import myapp.core.Result
import myapp.core.TAG
import myapp.todo.history.data.Actor
import myapp.todo.history.data.ActorRepository

class ActorListViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val actors: LiveData<List<Actor>>
    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException

    val actorRepository: ActorRepository

    init {
        val actorDao = TodoDatabase.getDatabase(application, viewModelScope).actorDao()
        actorRepository = ActorRepository(actorDao)
        actors = actorRepository.actors
    }

    fun refresh() {
        viewModelScope.launch {
            Log.v(TAG, "refresh...");
            mutableLoading.value = true
            mutableException.value = null
            when (val result = actorRepository.refresh()) {
                is Result.Success -> {
                    Log.d(TAG, "refresh succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "refresh failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableLoading.value = false
        }
    }
}
