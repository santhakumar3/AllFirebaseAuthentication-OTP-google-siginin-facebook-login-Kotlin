package com.example.allfirebaseauthentication

import android.content.ContentValues.TAG
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit


class MainActivity2 : AppCompatActivity() {

    private lateinit var sendotp: TextView;
    private lateinit var mobilenumberlayout: TextInputLayout;
    private lateinit var mobilenumber: TextInputEditText;
    private lateinit var otplayout: TextInputLayout;
    private lateinit var otp: TextInputEditText;

    private lateinit var phoneNum: String;
    private lateinit var otpNum: String;
    private var receivedOTP: String ="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        initialization()
        clickListeners();
    }

    private fun clickListeners() {

        sendotp.setOnClickListener {

            if (sendotp.getText().toString().trim().equals("Send OTP")) {

                phoneNum = mobilenumber.getText().toString().trim()


                if (phoneNum.isEmpty()) {
                    Toast.makeText(this, "Please enter mobile number", Toast.LENGTH_SHORT).show()
                } else if (phoneNum.length < 10) {
                    Toast.makeText(this, "Invalid mobile number", Toast.LENGTH_SHORT).show()
                } else {
                    sendOTPMethod("+91" + phoneNum)
//                    val phonauthactivity = PhoneAuthActivity()
//                    phonauthactivity.startPhoneNumberVerification("+91" + phoneNum)

                }

            } else {
                otpNum = otp.getText().toString().trim()
                if (otpNum.isEmpty()) {
                    Toast.makeText(this, "Please enter otp number", Toast.LENGTH_SHORT).show()
                } else if (otpNum.length < 6) {
                    Toast.makeText(this, "Invalid otp number", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("otpone: ","enterotp: "+otpNum)
                    Log.d("otpone: ","receivedotp: "+receivedOTP)
                    if (otpNum.equals(receivedOTP)) {
                        Toast.makeText(
                            this@MainActivity2,
                            "OTP verification completed",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity2,
                            "Invalid OTP",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }
    }
    private fun initialization() {
        sendotp = findViewById<TextView>(R.id.sendotp)
        mobilenumberlayout = findViewById<TextInputLayout>(R.id.mobilenumberlayout)
        mobilenumber = findViewById<TextInputEditText>(R.id.mobilenumber)
        otplayout = findViewById<TextInputLayout>(R.id.otplayout)
        otp = findViewById<TextInputEditText>(R.id.otp)
    }
    private fun sendOTPMethod(phoneNum: String) {
        autoentryOTP(phoneNum);

    }

    private fun autoentryOTP(phoneNum: String) {


        val auth = FirebaseAuth.getInstance()

        val firebaseAuthSettings = auth.firebaseAuthSettings
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNum, receivedOTP)

        // Configure faking the auto-retrieval with the whitelisted numbers.
//        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNum, smsCode)

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNum)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Instant verification is applied and a credential is directly returned.
                    mobilenumber.isClickable = true
                    mobilenumber.setText("")
                    mobilenumberlayout.isVisible = true
                    otplayout.isVisible = false
                    mobilenumberlayout.isClickable = true
                    mobilenumber.setFreezesText(false)
                    sendotp.setText("Send OTP")
                    Toast.makeText(
                        this@MainActivity2,
                        "Direct OTP verification completed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // [START_EXCLUDE]
                override fun onVerificationFailed(e: FirebaseException) {

                    manualentryOTP(phoneNum)
                }
                // [END_EXCLUDE]
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        // [END auth_test_phone_auto]
    }

    private fun manualentryOTP(phoneNum: String) {
        val smsCode = "123456"
        // Configure faking the auto-retrieval with the whitelisted numbers.

        // Whenever verification is triggered with the whitelisted number,
        // provided it is not set for auto-retrieval, onCodeSent will be triggered.

        // Whenever verification is triggered with the whitelisted number,
        // provided it is not set for auto-retrieval, onCodeSent will be triggered.


        val auth = FirebaseAuth.getInstance()
//
//        val firebaseAuthSettings = auth.firebaseAuthSettings
//        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNum, receivedOTP)

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNum)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(
                    @NonNull verificationId: String,
                    @NonNull forceResendingToken: ForceResendingToken
                ) {
                    // Save the verification id somewhere
                    // The corresponding whitelisted code above should be used to complete sign-in.
                    receivedOTP = verificationId
                    mobilenumberlayout.isClickable = false
                    mobilenumber.setFreezesText(true)
                    mobilenumber.setText("")
                    mobilenumberlayout.isVisible = false
                    otplayout.isVisible = true
                    sendotp.setText("Okay")
                    Toast.makeText(
                        this@MainActivity2,
                        "OTP sent successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                }

                override fun onVerificationCompleted(@NonNull phoneAuthCredential: PhoneAuthCredential) {
                    // Sign in with the credential
                    mobilenumber.isClickable = true
                    mobilenumber.setText("")
                    mobilenumberlayout.isVisible = true
                    otplayout.isVisible = false
                    mobilenumberlayout.isClickable = true
                    mobilenumber.setFreezesText(false)
                    sendotp.setText("Send OTP")
                    Toast.makeText(
                        this@MainActivity2,
                        "OTP verification completed",
                        Toast.LENGTH_SHORT
                    ).show()

                }

                override fun onVerificationFailed(@NonNull e: FirebaseException) {
                    mobilenumber.isClickable = true
                    mobilenumber.setText("")
                    mobilenumberlayout.isVisible = true
                    otplayout.isVisible = false
                    mobilenumberlayout.isClickable = true
                    mobilenumber.setFreezesText(false)
                    sendotp.setText("Send OTP")
                    Toast.makeText(
                        this@MainActivity2,
                        "OTP verification failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

}
