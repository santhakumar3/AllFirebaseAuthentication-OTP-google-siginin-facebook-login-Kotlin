package com.example.allfirebaseauthentication

import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var sendotp: TextView;
    private lateinit var mobilenumberlayout: TextInputLayout;
    private lateinit var mobilenumber: TextInputEditText;
    private lateinit var otplayout: TextInputLayout;
    private lateinit var otp: TextInputEditText;
    private lateinit var timertxt: TextView;
    private lateinit var resendotp: TextView;

    private lateinit var phoneNum: String;
    private lateinit var otpNum: String;
    private var receivedOTP: String ="";

    private lateinit var verificationidstr: String;
    private lateinit var token: PhoneAuthProvider.ForceResendingToken;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialization()
        sendotp.setOnClickListener {
            if (sendotp.getText().toString().trim().equals("Send OTP")) {
                phoneNumValidation()
            }else{
                otpNumValidation()
            }
        }

        resendotp.setOnClickListener {
            phoneNum = mobilenumber.getText().toString().trim()
            resendVerificationCode("+91"+phoneNum,token)
        }
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?,
    ) {
        val auth = FirebaseAuth.getInstance()
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // (optional) Activity for callback binding
            // If no activity is passed, reCAPTCHA verification can not be used.
            .setCallbacks(object : OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(
                    @NonNull verificationId: String,
                    @NonNull forceResendingToken: ForceResendingToken
                ) {
                    // Save the verification id somewhere
                    // The corresponding whitelisted code above should be used to complete sign-in.
                    verificationidstr = verificationId
//                    token = forceResendingToken

                    receivedOTP = verificationId
                    mobilenumberlayout.isClickable = true
                    mobilenumber.setFreezesText(true)
                    mobilenumberlayout.isVisible = true
                    otplayout.isVisible = true
                    sendotp.isVisible = true
                    sendotp.setText("Okay")
                    resendotp.isVisible = false
                    Toast.makeText(
                        this@MainActivity,
                        "Resend OTP sent successfully",
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
                        this@MainActivity,
                        "Resend OTP verification completed",
                        Toast.LENGTH_SHORT
                    ).show()


                }

                override fun onVerificationFailed(@NonNull e: FirebaseException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Resend OTP verification failed",
                        Toast.LENGTH_SHORT
                    ).show()

                    mobilenumber.isClickable = true
                    mobilenumberlayout.isVisible = true
                    otplayout.isVisible = false
                    mobilenumberlayout.isClickable = true
                    mobilenumber.setFreezesText(true)
                    sendotp.setText("Send OTP")
                    sendotp.isVisible = true
                    resendotp.isVisible = false


                }
            })
            .build()


        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder)
        if (token != null) {
//            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }

    }

    private fun otpNumValidation() {
        otpNum = otp.getText().toString().trim()
        if (otpNum.isEmpty()) {
            Toast.makeText(this, "Please enter otp number", Toast.LENGTH_SHORT).show()
        } else if (otpNum.length < 6) {
            Toast.makeText(this, "Invalid otp number", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("otpone: ","enterotp: "+otpNum)
            Log.d("otpone: ","receivedotp: "+receivedOTP)
            verifyPhoneNumberWithCode(verificationidstr,otpNum)

        }
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]

        signInWithPhoneAuthCredential(credential)

    }

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    Toast.makeText(
                        this@MainActivity,
                        "OTP verification completed",
                        Toast.LENGTH_SHORT
                    ).show()
                    mobilenumber.setText("")
                    mobilenumberlayout.isVisible = true
                    otp.setText("")
                    otplayout.isVisible = false
                    sendotp.setText("Send OTP")

                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI

                    Toast.makeText(
                        this@MainActivity,
                        "Invalid OTP",
                        Toast.LENGTH_SHORT
                    ).show()
                    mobilenumber.setText("")
                    mobilenumberlayout.isVisible = false
                    otp.setText("")
                    otplayout.isVisible = true
                    sendotp.setText("Okay")
                }
            }
    }
    // [END sign_in_with_phone]

    private fun phoneNumValidation() {
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
    }

    private fun initialization() {
        sendotp = findViewById<TextView>(R.id.sendotp)
        mobilenumberlayout = findViewById<TextInputLayout>(R.id.mobilenumberlayout)
        mobilenumber = findViewById<TextInputEditText>(R.id.mobilenumber)
        otplayout = findViewById<TextInputLayout>(R.id.otplayout)
        otp = findViewById<TextInputEditText>(R.id.otp)
        timertxt = findViewById<TextView>(R.id.timertxt)
        resendotp = findViewById<TextView>(R.id.resendotp)
    }
    private fun sendOTPMethod(phoneNum: String) {
        manualentryOTP(phoneNum)
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
                        this@MainActivity,
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
        // Configure faking the auto-retrieval with the whitelisted numbers.
        // Whenever verification is triggered with the whitelisted number,
        // provided it is not set for auto-retrieval, onCodeSent will be triggered.

        // Whenever verification is triggered with the whitelisted number,
        // provided it is not set for auto-retrieval, onCodeSent will be triggered.
        val auth = FirebaseAuth.getInstance()
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
                    verificationidstr = verificationId
                    token = forceResendingToken
                    
                    receivedOTP = verificationId
                    mobilenumberlayout.isClickable = false
                    mobilenumber.setFreezesText(true)
                    mobilenumberlayout.isVisible = false
                    otplayout.isVisible = true
                    sendotp.setText("Okay")
                    Toast.makeText(
                        this@MainActivity,
                        "OTP sent successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    object : CountDownTimer(20000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            timertxt.setText("seconds remaining 00 : " + millisUntilFinished / 1000)
                            timertxt.isVisible = true
                        }
                        override fun onFinish() {
                            timertxt.isVisible = false
                            timertxt.setText("00 : 00")
                            resendotp.isVisible = true
                            sendotp.isVisible = false


                        }
                    }.start()

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
                        this@MainActivity,
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
                        this@MainActivity,
                        "OTP verification failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

}
