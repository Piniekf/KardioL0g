package com.party.kardiol0g.loginregister;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.party.kardiol0g.MainActivity;
import com.party.kardiol0g.MainActivity_Doctor;
import com.party.kardiol0g.R;
import com.party.kardiol0g.settings.InitialSettingsActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText, forgotPassword;
    private Button loginButton;
    private FirebaseAuth auth;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signUpRedirectText);
        forgotPassword = findViewById(R.id.forgot_password);

        auth = FirebaseAuth.getInstance();

        // Sprawdź, czy użytkownik jest już zalogowany przy starcie aplikacji
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            navigateToAppropriateActivity();
        }

        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString();
            String pass = loginPassword.getText().toString();

            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!pass.isEmpty()) {
                    loginUser(email, pass);
                } else {
                    loginPassword.setError("Puste pola są niedozwolone");
                }
            } else if (email.isEmpty()) {
                loginEmail.setError("Puste pola są niedozwolone");
            } else {
                loginEmail.setError("Proszę wprowadzic poprawny email");
            }
        });

        signupRedirectText.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class))
        );

        forgotPassword.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot, null);
            EditText emailBox = dialogView.findViewById(R.id.emailBox);

            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            dialogView.findViewById(R.id.btnReset).setOnClickListener(view1 -> {
                String userEmail = emailBox.getText().toString();

                if (TextUtils.isEmpty(userEmail) || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    Toast.makeText(LoginActivity.this, "Wprowadź swój email", Toast.LENGTH_SHORT).show();
                    return;
                }
                auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Sprawdź swoją pocztę", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(LoginActivity.this, "Wysyłanie nie powiodło się", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            dialogView.findViewById(R.id.btnCancel).setOnClickListener(view12 -> dialog.dismiss());

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            dialog.show();
        });
    }

    // Metoda do logowania użytkownika
    private void loginUser(String email, String pass) {
        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    // Użytkownik zalogowany - sprawdź jego rolę
                    navigateToAppropriateActivity();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(LoginActivity.this, "Logowanie nie powiodło się", Toast.LENGTH_SHORT).show()
                );
    }

    private void navigateToAppropriateActivity() {
        // Pobierz ID zalogowanego użytkownika
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        userRef.child("isInitialSettingsPassed").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Boolean isInitialSettingsPassed = task.getResult().getValue(Boolean.class);

                // Jeśli ustawienie "isInitialSettingsPassed" nie jest true, przekieruj do InitialSettingsActivity
                if (isInitialSettingsPassed == null || !isInitialSettingsPassed) {
                    startActivity(new Intent(LoginActivity.this, InitialSettingsActivity.class));
                    finish(); // Zakończ LoginActivity
                    return;
                }

                // Sprawdź, czy użytkownik jest lekarzem
                userRef.child("czyLekarz").get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        Boolean isDoctor = task2.getResult().getValue(Boolean.class);
                        if (isDoctor != null && isDoctor) {
                            // Jeśli użytkownik jest lekarzem, przejdź do MainActivity_Doctor
                            startActivity(new Intent(LoginActivity.this, MainActivity_Doctor.class));
                        } else {
                            // Jeśli użytkownik nie jest lekarzem, przejdź do MainActivity
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Nie udało się pobrać danych użytkownika", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(LoginActivity.this, "Nie udało się pobrać danych użytkownika", Toast.LENGTH_SHORT).show();
            }
        });
    }

}