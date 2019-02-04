package com.evampsaanga.facebook

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.accountkit.AccountKit
import com.facebook.accountkit.AccountKitLoginResult
import com.facebook.accountkit.ui.AccountKitActivity
import com.facebook.accountkit.ui.AccountKitConfiguration
import com.facebook.accountkit.ui.LoginType
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton

class MainActivity : AppCompatActivity() {


    var APP_REQUEST_CODE = 1
    internal lateinit var loginButton: LoginButton
    internal lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loginButton = findViewById(R.id.facebook_login_button) as LoginButton
        loginButton.setReadPermissions("email")

        // Login Button callback registration
        callbackManager = CallbackManager.Factory.create()
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                launchAccountActivity()
            }

            override fun onCancel() {}

            override fun onError(exception: FacebookException) {
                // display error
                val toastMessage = exception.message
                Toast.makeText(this@MainActivity, toastMessage, Toast.LENGTH_LONG).show()
            }
        })

        // check for an existing access token
        val accessToken = AccountKit.getCurrentAccessToken()
        val loginToken = com.facebook.AccessToken.getCurrentAccessToken()
        if (accessToken != null || loginToken != null) {
            // if previously logged in, proceed to the account activity
            launchAccountActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Forward result to the callback manager for Login Button
        callbackManager.onActivityResult(requestCode, resultCode, data)

        // For Account Kit, confirm that this response matches your request
        if (requestCode == APP_REQUEST_CODE) {
            val loginResult = data!!.getParcelableExtra<AccountKitLoginResult>(AccountKitLoginResult.RESULT_KEY)
            if (loginResult.error != null) {
                // display login error
                val toastMessage = loginResult.error!!.errorType.message
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
            } else if (loginResult.accessToken != null) {
                // on successful login, proceed to the account activity
                launchAccountActivity()
            }
        }
    }

    private fun onLogin(loginType: LoginType) {
        // create intent for the Account Kit activity
        val intent = Intent(this, AccountKitActivity::class.java)

        // configure login type and response type
        val configurationBuilder = AccountKitConfiguration.AccountKitConfigurationBuilder(
                loginType,
                AccountKitActivity.ResponseType.TOKEN
        )
        val configuration = configurationBuilder.build()

        // launch the Account Kit activity
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configuration)
        startActivityForResult(intent, APP_REQUEST_CODE)
    }

    fun onPhoneLogin(view: View) {
        val logger = AppEventsLogger.newLogger(this)
        logger.logEvent("onPhoneLogin")
        onLogin(LoginType.PHONE)
    }

    fun onEmailLogin(view: View) {
        val logger = AppEventsLogger.newLogger(this)
        logger.logEvent("onEmailLogin")
        onLogin(LoginType.EMAIL)
    }

    private fun launchAccountActivity() {
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
        finish()
    }

}
