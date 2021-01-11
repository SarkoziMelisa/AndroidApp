package myapp.todo.history.actor

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
import kotlinx.android.synthetic.main.fragment_actor_edit.*
import myapp.core.TAG
import myapp.todo.history.data.Actor

class ActorEditFragment : Fragment() {
    companion object {
        const val ACTOR_ID = "ACTOR_ID"
    }

    private lateinit var viewModel: ActorEditViewModel
    private var actorId: String? = null
    private var actor: Actor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
        arguments?.let {
            if (it.containsKey(ACTOR_ID)) {
                actorId = it.getString(ACTOR_ID).toString()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_actor_edit, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        setupViewModel()
        fab.setOnClickListener {
            Log.v(TAG, "save item")
            val act = actor
            if (act != null) {
                act.name = actor_name.text.toString()
                act.wikipediaLink = actor_link.text.toString()
                act.matchingPercentage = actor_percentage.text.toString().toFloat()
                viewModel.saveOrUpdateActor(act)
            }
        }
        fab_delete.setOnClickListener {
            Log.v(TAG, "delete item")
            val act = actor
            if (act != null) {
                viewModel.deleteActor(act)
            }
        }

    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(ActorEditViewModel::class.java)
        viewModel.fetching.observe(viewLifecycleOwner) { fetching ->
            Log.v(TAG, "update fetching")
            progress.visibility = if (fetching) View.VISIBLE else View.GONE
        }
        viewModel.fetchingError.observe(viewLifecycleOwner) { exception ->
            if (exception != null) {
                Log.v(TAG, "update fetching error")
                val message = "Fetching exception ${exception.message}"
                val parentActivity = activity?.parent
                if (parentActivity != null) {
                    Toast.makeText(parentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.completed.observe(viewLifecycleOwner) { completed ->
            if (completed) {
                Log.v(TAG, "completed, navigate back")
                findNavController().popBackStack()
            }
        }
        val id = actorId
        if (id == null) {
            actor = Actor("", "", "", 0f)
        } else {
            viewModel.getItemById(id).observe(viewLifecycleOwner) {
                Log.v(TAG, "update items")
                if (it != null) {
                    actor = it
                    actor_name.setText(it.name)
                    actor_link.setText(it.wikipediaLink)
                    actor_percentage.setText(it.matchingPercentage.toString())
                }
            }
        }
    }
}
