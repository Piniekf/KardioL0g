package com.party.kardiol0g;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class InitialSettingsActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextDateOfBirth;
    private RadioGroup radioGroupRole;
    private Button buttonSave;
    private DatabaseReference userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_settings);

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        buttonSave = findViewById(R.id.buttonSave);

        editTextDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        editTextDateOfBirth.setText(selectedDate);
                    }
                },
                year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void saveSettings() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String dateOfBirth = editTextDateOfBirth.getText().toString().trim();

        int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
        RadioButton selectedRoleRadioButton = findViewById(selectedRoleId);
        String role = selectedRoleRadioButton.getText().toString();

        userDatabase.child("imie").setValue(firstName);
        userDatabase.child("nazwisko").setValue(lastName);
        userDatabase.child("dataUrodzenia").setValue(dateOfBirth);
        userDatabase.child("czyLekarz").setValue(role.equals("Lekarz"));

        Toast.makeText(this, "Dane zapisane", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(InitialSettingsActivity.this, MainActivity.class));
        finish();
    }
}
