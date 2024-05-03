package me.chamada.ft_hangouts

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import me.chamada.ft_hangouts.databinding.ActivityMainBinding
import java.text.DateFormat
import java.util.Date

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: ContactRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val baseColorStr = sharedPreferences.getString("appBarColor", "material_you")

        setThemeBaseColor(baseColorStr)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, _, _ ->
            binding.appbarLayout.setExpanded(true)
        }

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        repository = (applicationContext as ContactApplication).repository
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)
        val lifecyclePreferences = getSharedPreferences("lifecycle", MODE_PRIVATE)
        val lastStop = lifecyclePreferences.getLong("lastStop", -1)

        if (lastStop != -1L) {
            val dateFormat = DateFormat.getDateTimeInstance()
            val formattedTime = dateFormat.format(Date(lastStop))

            Toast.makeText(this, formattedTime, Toast.LENGTH_SHORT).show()

            lifecyclePreferences.edit {
                remove("lastStop")
            }
        }
    }

    override val defaultViewModelProviderFactory: ContactViewModel.Factory
        get() = ContactViewModel.Factory(repository)

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setThemeBaseColor(baseColor: String?) {
        if (baseColor == null || baseColor == "material_you") {
            DynamicColors.applyToActivityIfAvailable(this)
        }
        else {
            var appBarColor = 0

            try {
                appBarColor = Color.parseColor(baseColor)
            }
            catch (e: IllegalArgumentException) {
                println("Warning: Could not parse appBarColor string!")
            }
            finally {
                DynamicColors.applyToActivityIfAvailable(this,
                    DynamicColorsOptions.Builder()
                        .setContentBasedSource(appBarColor)
                        .build()
                )
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "appBarColor") {
            val newBaseColor = sharedPreferences?.getString("appBarColor", "material_you")

            setThemeBaseColor(newBaseColor)
            recreate()
        }
    }
}