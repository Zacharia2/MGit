package xyz.realms.mgit

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Environment
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.conscrypt.BuildConfig
import org.conscrypt.Conscrypt
import org.eclipse.jgit.transport.CredentialsProvider
import timber.log.Timber
import xyz.realms.mgit.database.Repo
import xyz.realms.mgit.errors.SecurePrefsException
import xyz.realms.mgit.transport.AndroidJschCredentialsProvider
import xyz.realms.mgit.transport.MGitHttpConnectionFactory
import xyz.realms.mgit.ui.preference.PreferenceHelper
import xyz.realms.mgit.ui.utils.SecurePrefsHelper
import java.io.File
import java.security.Security

/**
 * Custom Application Singleton
 */
open class MGitApplication : Application() {
    var securePrefsHelper: SecurePrefsHelper? = null
    var prefenceHelper: PreferenceHelper? = null


    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var mContext: Context
        private lateinit var mCredentialsProvider: CredentialsProvider
        val context: Context
            get() = mContext

        @JvmStatic
        fun getContext(): MGitApplication {
            return mContext as MGitApplication
        }

        @JvmStatic
        fun getJschCredentialsProvider(): CredentialsProvider {
            return mCredentialsProvider
        }

        init {
            MGitHttpConnectionFactory.install()
            Security.addProvider(BouncyCastleProvider())
            Security.addProvider(Conscrypt.newProvider())
        }
    }

    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
        setAppVersionPref()
        prefenceHelper = PreferenceHelper(this)

        // /storage/emulated/0/Documents
        if (prefenceHelper?.getRepoRoot() == null || prefenceHelper?.getRepoRoot().toString()
                .isEmpty()
        ) {
            val documentsDir = File(Environment.getExternalStorageDirectory(), "Documents")
            Repo.setLocalRepoRoot(this, documentsDir)
        }
        try {
            securePrefsHelper = SecurePrefsHelper(this)
            mCredentialsProvider = AndroidJschCredentialsProvider(securePrefsHelper)
        } catch (e: SecurePrefsException) {
            Timber.e(e)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        initAcra {
            //core configuration:
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            // each plugin you chose above can be configured in a block like this:
            dialog {
                text = getString(R.string.dialog_error_send_report)
                //opening this block automatically enables the plugin.
            }
            mailSender {
                withMailTo(getString(R.string.crash_report_email))
            }
        }
    }

    private fun setAppVersionPref() {
        val sharedPreference = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        val version = BuildConfig.VERSION_NAME
        sharedPreference.edit().putString(getString(R.string.preference_key_app_version), version)
            .apply()
    }
}
