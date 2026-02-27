package com.vfs.taskmanagerfinal

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.transition.Fade
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.UserProfileChangeRequest

class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var btnSubmit: MaterialButton
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var rootLayout: ViewGroup

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var authStatusTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_register)

        rootLayout = findViewById(R.id.main)
        nameInputLayout = findViewById(R.id.nameInputLayout)
        btnSubmit = findViewById(R.id.btnSubmit)
        toggleGroup = findViewById(R.id.toggleGroup)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        authStatusTextView = findViewById(R.id.authStatusTextView_id)

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val transition = Fade()
                transition.duration = 300
                TransitionManager.beginDelayedTransition(rootLayout, transition)

                if (checkedId == R.id.btnRegisterTab) {
                    nameInputLayout.visibility = View.VISIBLE
                    btnSubmit.text = "Register"
                } else {
                    nameInputLayout.visibility = View.GONE
                    btnSubmit.text = "Login"
                }
            }
        }

        btnSubmit.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val name = etName.text.toString().trim()

            if (toggleGroup.checkedButtonId == R.id.btnRegisterTab) {
                if (validateRegister(name, email, password)) {
                    performRegister(name, email, password)
                }
            } else {
                if (validateLogin(email, password)) {
                    performLogin(email, password)
                }
            }
        }

        intent.getStringExtra("type")?.let { type ->
            if (type == "registration") {
                toggleGroup.check(R.id.btnRegisterTab)
            } else {
                toggleGroup.check(R.id.btnLoginTab)
            }
        }
    }

    private fun validateLogin(email: String, pass: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email required"
            return false
        }
        if (pass.isEmpty()) {
            etPassword.error = "Password required"
            return false
        }
        return true
    }

    private fun validateRegister(name: String, email: String, pass: String): Boolean {
        if (name.isEmpty()) {
            etName.error = "Name required"
            return false
        }
        if (email.isEmpty()) {
            etEmail.error = "Email required"
            return false
        }
        if (pass.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return false
        }
        return true
    }

    private fun performLogin(email: String, pass: String) {

        Cloud.auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                authStatusTextView.text = "Login Succesful"
                authStatusTextView.setTextColor(Color.GREEN)
                readGroups()
                navigateToGroups()
            }
            else
            {
                authStatusTextView.text = "Something went wrong with login"
                authStatusTextView.setTextColor(Color.RED)
            }

        }
    }

    private fun performRegister(name: String, email: String, pass: String) {

        Cloud.auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                {
                    authStatusTextView.text = "Registration Successful"
                    authStatusTextView.setTextColor(Color.GREEN)


                    val profileChange = UserProfileChangeRequest
                        .Builder()
                        .setDisplayName(name)
                        .build()

                    Cloud.auth.currentUser?.updateProfile(profileChange)
                        ?.addOnCompleteListener (this) { profTask ->
                            if (profTask.isSuccessful)
                            {
                                authStatusTextView.text = "${Cloud.auth.currentUser?.displayName} is now online"
                                writeUserObjAfterRegistration()
                                navigateToGroups()
                            }
                        }
                }
                else
                {
                    authStatusTextView.text = "Something went wrong with registration"
                    authStatusTextView.setTextColor(Color.RED)
                }
            }
    }

    private fun navigateToGroups() {
        val intent = Intent(this, GroupsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}