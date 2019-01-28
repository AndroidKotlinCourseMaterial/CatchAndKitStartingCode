package edu.rosehulman.catchandkit

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseUser
import android.support.annotation.NonNull
import android.support.v4.app.FragmentActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.OnSuccessListener


class MainActivity :
    AppCompatActivity(),
    ThumbnailGridFragment.OnThumbnailListener {
    private val WRITE_EXTERNAL_STORAGE_PERMISSION = 2

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        checkPermissions()
    }

    // This is a very simple workflow for anonymous auth.
    // Storage requires a user to be authenticated, even if the
    // rules allow public access.
    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null) {
            switchToGridFragment()
        } else {
            signInAnonymously()
        }
    }

    private fun signInAnonymously() {
        auth.signInAnonymously().addOnSuccessListener(this) {
            switchToGridFragment()
        }.addOnFailureListener(this) { e ->
            Log.e(Constants.TAG, "signInAnonymously:FAILURE", e)
        }
    }

    private fun switchToGridFragment() {
        val ft = supportFragmentManager.beginTransaction()
        val orientation = resources.configuration.orientation
        val columns = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 3
        ft.replace(R.id.fragment_container, ThumbnailGridFragment.newInstance(columns), "list")
        ft.commit()

    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        val fragment = supportFragmentManager.findFragmentByTag("list")
        if (fragment != null) {
            val columns = if (newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 5
            (fragment as ThumbnailGridFragment).setAdapterWithColumns(columns)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkPermissions() {
        // Check to see if we already have permissions
        if (ContextCompat
                .checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If we do not, request them from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Log.d(Constants.TAG, "Permission granted")
                } else {
                    // permission denied
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onThumbnailSelected(thumbnail: Thumbnail) {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, ThumbnailDetailFragment.newInstance(thumbnail))
        ft.addToBackStack("List")
        ft.commit()
    }
}
