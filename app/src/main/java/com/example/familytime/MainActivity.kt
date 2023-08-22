package com.example.familytime

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.familytime.databinding.ActivityMainBinding
import com.example.familytime.models.LocationData
import com.example.familytime.other.Resource
import com.example.familytime.other.Utils.currentUserUID
import com.example.familytime.viewmodels.auth.AuthViewModel
import com.example.familytime.viewmodels.auth.AuthViewModelFactory
import com.example.familytime.viewmodels.chats.ChatsViewModel
import com.example.familytime.viewmodels.chats.ChatsViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    @Inject
    lateinit var authViewModelFactory: AuthViewModelFactory
    private val authViewModel by viewModels<AuthViewModel> { authViewModelFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!authViewModel.checkLoginSession()) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        } else {
            currentUserUID = authViewModel.getCurrentUser().uid
        }
        val radius = resources.getDimension(R.dimen.cornerSize)

        val shapeDrawable: MaterialShapeDrawable =
            binding.navView.background as MaterialShapeDrawable
        shapeDrawable.shapeAppearanceModel = shapeDrawable.shapeAppearanceModel
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, radius)
            .build()

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_chats || destination.id == R.id.navigation_profile_set_up) {
                binding.navView.visibility = View.GONE
            } else {
                binding.navView.visibility = View.VISIBLE

            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (!authViewModel.checkLoginSession()) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        } else {
            currentUserUID = authViewModel.getCurrentUser().uid
        }
    }

    override fun onStart() {
        super.onStart()
        if (!authViewModel.checkLoginSession()) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        } else {
            currentUserUID = authViewModel.getCurrentUser().uid
        }
    }

    override fun onPause() {
        super.onPause()
        if (!authViewModel.checkLoginSession()) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        } else {
            currentUserUID = authViewModel.getCurrentUser().uid
        }
    }
}




