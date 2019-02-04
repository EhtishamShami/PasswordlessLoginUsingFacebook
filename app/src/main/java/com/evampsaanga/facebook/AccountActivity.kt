package com.evampsaanga.facebook

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.Profile
import com.facebook.ProfileTracker
import com.facebook.accountkit.Account
import com.facebook.accountkit.AccountKit
import com.facebook.accountkit.AccountKitCallback
import com.facebook.accountkit.AccountKitError
import com.facebook.login.LoginManager
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import java.util.*


class AccountActivity : AppCompatActivity() {

    internal lateinit var profileTracker: ProfileTracker
    internal lateinit var profilePic: ImageView
    internal lateinit var id: TextView
    internal lateinit var infoLabel: TextView
    internal lateinit var info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        profilePic = findViewById(R.id.profile_image) as ImageView
        id = findViewById(R.id.id) as TextView
        infoLabel = findViewById(R.id.info_label) as TextView
        info = findViewById(R.id.info) as TextView

        // register a receiver for the onCurrentProfileChanged event
        profileTracker = object : ProfileTracker() {
            override fun onCurrentProfileChanged(oldProfile: Profile, currentProfile: Profile?) {
                if (currentProfile != null) {
                    displayProfileInfo(currentProfile)
                }
            }
        }

        if (AccessToken.getCurrentAccessToken() != null) {
            // If there is an access token then Login Button was used
            // Check if the profile has already been fetched
            val currentProfile = Profile.getCurrentProfile()
            if (currentProfile != null) {
                displayProfileInfo(currentProfile)
            } else {
                // Fetch the profile, which will trigger the onCurrentProfileChanged receiver
                Profile.fetchProfileForCurrentAccessToken()
            }
        } else {
            // Otherwise, get Account Kit login information
            AccountKit.getCurrentAccount(object : AccountKitCallback<Account> {
                override fun onSuccess(account: Account) {
                    // get Account Kit ID
                    val accountKitId = account.id
                    id.text = accountKitId

                    val phoneNumber = account.phoneNumber
                    if (account.phoneNumber != null) {
                        // if the phone number is available, display it
                        val formattedPhoneNumber = formatPhoneNumber(phoneNumber.toString())
                        info.text = formattedPhoneNumber
                        infoLabel.setText("Phone")
                    } else {
                        // if the email address is available, display it
                        val emailString = account.email
                        info.text = emailString
                        infoLabel.setText("Email")
                    }

                }

                override fun onError(error: AccountKitError) {
                    val toastMessage = error.errorType.message
                    Toast.makeText(this@AccountActivity, toastMessage, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        // unregister the profile tracker receiver
        profileTracker.stopTracking()
    }

    fun onLogout(view: View) {
        // logout of Account Kit
        AccountKit.logOut()
        // logout of Login Button
        LoginManager.getInstance().logOut()

        launchLoginActivity()
    }

    private fun displayProfileInfo(profile: Profile) {
        // get Profile ID
        val profileId = profile.id
        id.text = profileId

        // display the Profile name
        val name = profile.name
        info.text = name
        infoLabel.setText("Name")

        // display the profile picture
        val profilePicUri = profile.getProfilePictureUri(100, 100)
        displayProfilePic(profilePicUri)
    }

    private fun launchLoginActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        var phoneNumber = phoneNumber
        // helper method to format the phone number for display
        try {
            val pnu = PhoneNumberUtil.getInstance()
            val pn = pnu.parse(phoneNumber, Locale.getDefault().country)
            phoneNumber = pnu.format(pn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
        } catch (e: NumberParseException) {
            e.printStackTrace()
        }

        return phoneNumber
    }

    private fun displayProfilePic(uri: Uri) {
        // helper method to load the profile pic in a circular imageview
        val transformation = RoundedTransformationBuilder()
                .cornerRadiusDp(30f)
                .oval(false)
                .build()
        Picasso.with(this@AccountActivity)
                .load(uri)
                .transform(transformation)
                .into(profilePic)
    }

}
