package com.party.kardiol0g;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class InitialSettingsActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextDateOfBirth, editTextHeight, editTextWeight;
    private EditText editTextContactPerson, editTextContactFirstName, editTextContactLastName, editTextContactPhoneNumber;
    private RadioGroup radioGroupRole, radioGroupGender;
    private Button buttonSave;
    private DatabaseReference userDatabase;
    private LinearLayout layoutContactInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_settings);

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextContactPerson = findViewById(R.id.editTextContactPerson);
        editTextContactPhoneNumber = findViewById(R.id.editTextContactPhoneNumber);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        buttonSave = findViewById(R.id.buttonSave);
        layoutContactInfo = findViewById(R.id.layoutContactInfo);

        editTextDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        radioGroupRole.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButtonPatient) {
                    layoutContactInfo.setVisibility(View.VISIBLE);
                } else {
                    layoutContactInfo.setVisibility(View.GONE);
                }
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
        String heightStr = editTextHeight.getText().toString().trim();
        String weightStr = editTextWeight.getText().toString().trim();
        String contactPerson = editTextContactPerson.getText().toString().trim();
        String contactFirstName = editTextContactFirstName.getText().toString().trim();
        String contactLastName = editTextContactLastName.getText().toString().trim();
        String contactPhoneNumber = editTextContactPhoneNumber.getText().toString().trim();

        // Walidacja pól
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(dateOfBirth) ||
                TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr) ||
                radioGroupRole.getCheckedRadioButtonId() == -1 || radioGroupGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }

        // Konwersja wzrostu i wagi na liczby
        int height = Integer.parseInt(heightStr);
        int weight = Integer.parseInt(weightStr);

        int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
        RadioButton selectedRoleRadioButton = findViewById(selectedRoleId);
        String role = selectedRoleRadioButton.getText().toString();

        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
        String gender = selectedGenderRadioButton.getText().toString();

        userDatabase.child("imie").setValue(firstName);
        userDatabase.child("nazwisko").setValue(lastName);
        userDatabase.child("dataUrodzenia").setValue(dateOfBirth);
        userDatabase.child("wzrost").setValue(height);
        userDatabase.child("waga").setValue(weight);
        userDatabase.child("czyLekarz").setValue(role.equals("Lekarz"));
        userDatabase.child("płeć").setValue(gender);

        if (role.equals("Pacjent")) {
            userDatabase.child("osobaKontaktowa").setValue(contactPerson);
            userDatabase.child("numerTelefonuKontaktowej").setValue(contactPhoneNumber);
        }

        Toast.makeText(this, "Dane zapisane", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(InitialSettingsActivity.this, MainActivity.class));
        finish();
    }
}
