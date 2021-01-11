package myapp.todo.history.actors

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.labam_android.R
import kotlinx.android.synthetic.main.fragment_actor_list.*
import myapp.auth.data.AuthRepository
import myapp.core.TAG

class ActorListFragment : Fragment() {
    private lateinit var actorListAdapter: ActorListAdapter
    private lateinit var actorsModel: ActorListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_actor_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        if (!AuthRepository.isLoggedIn) {
            findNavController().navigate(R.id.container)
            return;
        }
        setupActorList()
        fab.setOnClickListener {
            Log.v(TAG, "add new item")
            findNavController().navigate(R.id.fragment_actor_edit)
        }
    }

    private fun setupActorList() {
        actorListAdapter = ActorListAdapter(this)
        actor_list.adapter = actorListAdapter
        actorsModel = ViewModelProvider(this).get(ActorListViewModel::class.java)
        actorsModel.actors.observe(viewLifecycleOwner) { actors ->
            Log.v(TAG, "update items")
            actorListAdapter.actors = actors
        }
        actorsModel.loading.observe(viewLifecycleOwner) { loading ->
            Log.i(TAG, "update loading")
            progress.visibility = if (loading) View.VISIBLE else View.GONE
        }
        actorsModel.loadingError.observe(viewLifecycleOwner) { exception ->
            if (exception != null) {
                Log.i(TAG, "update loading error")
                val message = "Loading exception ${exception.message}"
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        }
        actorsModel.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")
    }
}