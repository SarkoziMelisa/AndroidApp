package myapp.todo.history.actors

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.labam_android.R
import kotlinx.android.synthetic.main.view_actor.view.*
import myapp.core.TAG
import myapp.todo.history.data.Actor
import myapp.todo.history.actor.ActorEditFragment

class ActorListAdapter(
    private val fragment: Fragment
) : RecyclerView.Adapter<ActorListAdapter.ViewHolder>() {

    var actors = emptyList<Actor>()
        set(value) {
            field = value
            notifyDataSetChanged();
        }

    private var onActorClick: View.OnClickListener;

    init {
        onActorClick = View.OnClickListener { view ->
            val actor = view.tag as Actor
            fragment.findNavController().navigate(R.id.fragment_actor_edit, Bundle().apply {
                putString(ActorEditFragment.ACTOR_ID, actor._id)
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_actor, parent, false)
        Log.v(TAG, "onCreateViewHolder")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.v(TAG, "onBindViewHolder $position")
        val actor = actors[position]
        holder.itemView.tag = actor
        holder.nameView.text = actor.toString()
        holder.itemView.setOnClickListener(onActorClick)
    }

    override fun getItemCount() = actors.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.name
    }
}
