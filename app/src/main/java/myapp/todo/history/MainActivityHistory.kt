package myapp.todo.history

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.labam_android.R
import myapp.core.TAG

class MainActivityHistory : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        Log.i(TAG, "onCreate")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(actor: MenuItem): Boolean {
        return when (actor.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(actor)
        }
    }
}