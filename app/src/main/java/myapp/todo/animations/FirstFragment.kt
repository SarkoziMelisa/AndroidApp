package myapp.todo.animations

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.labam_android.R
import kotlinx.android.synthetic.main.animations_fragment_first.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.animations_fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        revealAndHideView()
        changeViewPositionByValueAnimator()
        changeViewPositionByObjectAnimator()
    }

    private fun changeViewPositionByValueAnimator() {
        ValueAnimator.ofFloat(0f, 100f).apply {
            duration = 5000
            start()
            addUpdateListener {
                text_hello.translationY = -1 * it.animatedValue as Float
            }
        }
    }

    private fun changeViewPositionByObjectAnimator() {
        ObjectAnimator.ofFloat(view, "translationX", 50f).apply {
            duration = 5000
            start()
        }
    }

    private fun revealAndHideView() {
        button_reveal.setOnClickListener {
            text_hello.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .setDuration(5000)
                    .setListener(null)
            }
        }
        button_hide.setOnClickListener {
            text_hello.apply {
                alpha = 1f
                visibility = View.VISIBLE
                animate()
                    .alpha(0f)
                    .setDuration(5000)
                    .setListener(null)
            }
        }
    }
}