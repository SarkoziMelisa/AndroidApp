package myapp.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.labam_android.R

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.history).setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_ActorListFragment)
        }

        view.findViewById<Button>(R.id.camera).setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_CameraFragment)
        }

        view.findViewById<Button>(R.id.animations).setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_AnimationFragment)
        }
    }

}