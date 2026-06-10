package com.example.moviewatchlist;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextName, editTextEmail, editTextPassword;
    private TextInputLayout inputLayoutName, inputLayoutEmail, inputLayoutPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Layouts (for setting error messages)
        inputLayoutName = findViewById(R.id.inputLayoutName);
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);

        // Initialize EditTexts
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmailRegister);
        editTextPassword = findViewById(R.id.editTextPasswordRegister);

        Button buttonRegister = findViewById(R.id.buttonRegister);
        TextView textViewLogin = findViewById(R.id.textViewLogin);

        buttonRegister.setOnClickListener(v -> registerUser());

        textViewLogin.setOnClickListener(v -> finish()); // Back to Login
    }

    private void registerUser() {
        String name = Objects.requireNonNull(editTextName.getText()).toString().trim();
        String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();

        // Reset errors
        inputLayoutName.setError(null);
        inputLayoutEmail.setError(null);
        inputLayoutPassword.setError(null);

        // 1. Validasi Nama
        if (TextUtils.isEmpty(name)) {
            inputLayoutName.setError("Nama lengkap harus diisi");
            editTextName.requestFocus();
            return;
        }

        // 2. Validasi Email
        if (TextUtils.isEmpty(email)) {
            inputLayoutEmail.setError("Email harus diisi");
            editTextEmail.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputLayoutEmail.setError("Format email tidak valid");
            editTextEmail.requestFocus();
            return;
        }

        // 3. Validasi Password
        if (TextUtils.isEmpty(password)) {
            inputLayoutPassword.setError("Password harus diisi");
            editTextPassword.requestFocus();
            return;
        } else if (password.length() < 6) {
            inputLayoutPassword.setError("Password minimal harus 6 karakter");
            editTextPassword.requestFocus();
            return;
        }

        // Proses Pembuatan Akun di Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finishAffinity();
                    } else {
                        // Jika gagal (misal: email sudah terdaftar)
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Pendaftaran gagal";
                        Toast.makeText(RegisterActivity.this, "Gagal: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
