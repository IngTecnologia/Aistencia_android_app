package com.inemec.verificacionasistencia

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.inemec.verificacionasistencia.api.ApiClient
import com.inemec.verificacionasistencia.databinding.ActivityMainBinding
import com.inemec.verificacionasistencia.model.VersionResponse
import com.inemec.verificacionasistencia.ui.dialogs.UpdateDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var updateDialog: UpdateDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configurar ActionBar con NavController
        setupActionBarWithNavController(navController)

        // Verificar versión al iniciar
        checkVersion()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun checkVersion() {
        ApiClient.apiService.getVersion().enqueue(object : Callback<VersionResponse> {
            override fun onResponse(call: Call<VersionResponse>, response: Response<VersionResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { versionInfo ->
                        handleVersionCheck(versionInfo)
                    }
                }
            }

            override fun onFailure(call: Call<VersionResponse>, t: Throwable) {
                // Fallar silenciosamente si no se puede verificar la versión
                // En un entorno de producción, podrías log este error
            }
        })
    }

    private fun handleVersionCheck(versionInfo: VersionResponse) {
        val currentVersion = getCurrentAppVersion()

        // Comparar versiones (implementación simple)
        if (isVersionOutdated(currentVersion, versionInfo.latestVersion)) {
            val forceUpdate = versionInfo.forceUpdate ||
                    isVersionOutdated(currentVersion, versionInfo.minimumVersion)

            showUpdateDialog(versionInfo.updateMessage, forceUpdate, versionInfo.downloadUrl)
        }
    }

    private fun getCurrentAppVersion(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "1.0.0"  // Si versionName es null, usar "1.0.0" como default
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }

    private fun isVersionOutdated(current: String, required: String): Boolean {
        // Implementación simple de comparación de versiones
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val requiredParts = required.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(currentParts.size, requiredParts.size)) {
            val currentPart = currentParts.getOrNull(i) ?: 0
            val requiredPart = requiredParts.getOrNull(i) ?: 0

            when {
                currentPart < requiredPart -> return true
                currentPart > requiredPart -> return false
            }
        }
        return false
    }

    private fun showUpdateDialog(message: String, forceUpdate: Boolean, downloadUrl: String?) {
        if (updateDialog?.isShowing == true) return

        updateDialog = UpdateDialog(this, message, forceUpdate, downloadUrl)
        updateDialog?.show()
    }
}