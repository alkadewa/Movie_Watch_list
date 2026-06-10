package com.example.moviewatchlist;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText editCurrentPassword, editNewPassword, editConfirmPassword;
    private TextInputLayout layoutCurrent, layoutNew, layoutConfirm;
    private TextView textViewEmail;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        textViewEmail = findViewById(R.id.textViewProfileEmail);
        editCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editNewPassword = findViewById(R.id.editTextNewPassword);
        editConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        
        layoutCurrent = findViewById(R.id.layoutCurrentPassword);
        layoutNew = findViewById(R.id.layoutNewPassword);
        layoutConfirm = findViewById(R.id.layoutConfirmPassword);

        Button btnUpdate = findViewById(R.id.btnUpdatePassword);
        Button btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        if (user != null) {
            textViewEmail.setText(user.getEmail());
        }

        btnUpdate.setOnClickListener(v -> validateAndUpdatePassword());
        btnDeleteAccount.setOnClickListener(v -> confirmAndDeleteAccount());
    }

    private void confirmAndDeleteAccount() {
        new MaterialAlertDialogBuilder(this, R.style.CinematicAlertDialog)
                .setTitle("Delete Account?")
                .setMessage("This will permanently delete your account and all your saved movies. This action cannot be undone.")
                .setPositiveButton("DELETE", (dialog, which) -> deleteAccount())
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void deleteAccount() {
        if (user == null) return;

        String userId = user.getUid();
        
        // 1. Delete data from Database first
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).removeValue()
                .addOnCompleteListener(task -> {
                    // 2. Delete user from Auth
                    user.delete().addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            String msg = authTask.getException() != null ? authTask.getException().getMessage() : "Deletion failed";
                            Toast.makeText(ProfileActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
                });
    }

    private void validateAndUpdatePassword() {
        String currentPass = Objects.requireNonNull(editCurrentPassword.getText()).toString().trim();
        String newPass = Objects.requireNonNull(editNewPassword.getText()).toString().trim();
        String confirmPass = Objects.requireNonNull(editConfirmPassword.getText()).toString().trim();

        layoutCurrent.setError(null);
        layoutNew.setError(null);
        layoutConfirm.setError(null);

        if (TextUtils.isEmpty(currentPass)) {
            layoutCurrent.setError("Current password required");
            return;
        }

        if (newPass.length() < 6) {
            layoutNew.setError("Password minimal harus 6 karakter");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            layoutConfirm.setError("Passwords do not match");
            return;
        }

        updatePassword(currentPass, newPass);
    }

    private void updatePassword(String currentPass, String newPass) {
        if (user == null || user.getEmail() == null) return;

        // Re-authenticate user
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update password
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, R.string.password_updated, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String msg = updateTask.getException() != null ? updateTask.getException().getMessage() : "Update failed";
                        Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                layoutCurrent.setError(getString(R.string.reauth_failed));
            }
        });
    }
}
