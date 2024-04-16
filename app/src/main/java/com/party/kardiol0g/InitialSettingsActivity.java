package com.party.kardiol0g;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitialSettingsActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextDateOfBirth, editTextHeight, editTextWeight, editTextContactPerson, editTextContactPhoneNumber;
    private RadioGroup radioGroupRole, radioGroupGender;
    private Button buttonSave;
    private DatabaseReference userDatabase;
    private LinearLayout layoutContactInfo;
    private Spinner doctorSpinner;
    private ArrayAdapter<String> doctorAdapter;
    private Map<String, String> doctorMap; // Mapa przechowująca imiona i nazwiska lekarzy z ich UID

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
        doctorSpinner = findViewById(R.id.doctorSpinner);
        doctorMap = new HashMap<>();

        DatabaseReference doctorsRef = FirebaseDatabase.getInstance().getReference().child("Users");
        doctorsRef.orderByChild("czyLekarz").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> doctorNames = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String doctorName = snapshot.child("imie").getValue(String.class) + " " + snapshot.child("nazwisko").getValue(String.class);
                    String doctorUid = snapshot.getKey(); // Pobierz UID lekarza
                    doctorMap.put(doctorName, doctorUid); // Dodaj parę imię + nazwisko -> UID do mapy
                    doctorNames.add(doctorName);
                }
                doctorAdapter = new ArrayAdapter<>(InitialSettingsActivity.this, android.R.layout.simple_spinner_item, doctorNames);
                doctorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                doctorSpinner.setAdapter(doctorAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Obsługa błędu
            }
        });

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
        String contactPhoneNumber = editTextContactPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                radioGroupRole.getCheckedRadioButtonId() == -1 || radioGroupGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidName(firstName) || !isValidName(lastName)) {
            Toast.makeText(this, "Imię i nazwisko nie mogą zawierać cyfr", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDate(dateOfBirth)) {
            Toast.makeText(this, "Niepoprawny format daty urodzenia. Wymagany format: DD/MM/RRRR", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidHeight(heightStr)) {
            Toast.makeText(this, "Wzrost musi być liczbą całkowitą", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidWeight(weightStr)) {
            Toast.makeText(this, "Waga musi być liczbą całkowitą", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(contactPhoneNumber) && !isValidPhoneNumber(contactPhoneNumber)) {
            Toast.makeText(this, "Numer telefonu musi składać się z 9 cyfr", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidContactPerson(contactPerson)) {
            Toast.makeText(this, "Osoba kontaktowa nie może zawierać cyfr", Toast.LENGTH_SHORT).show();
            return;
        }


        int height = 0;
        int weight = 0;
        if (!TextUtils.isEmpty(heightStr) && !TextUtils.isEmpty(weightStr)) {
            try {
                height = Integer.parseInt(heightStr);
                weight = Integer.parseInt(weightStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Wzrost i waga muszą być liczbami całkowitymi", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!TextUtils.isEmpty(contactPhoneNumber) && !contactPhoneNumber.matches("\\d{9}")) {
            Toast.makeText(this, "Numer telefonu musi składać się z 9 cyfr", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
        RadioButton selectedRoleRadioButton = findViewById(selectedRoleId);
        String role = selectedRoleRadioButton.getText().toString();

        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
        String gender = selectedGenderRadioButton.getText().toString();

        userDatabase.child("imie").setValue(firstName);
        userDatabase.child("nazwisko").setValue(lastName);
        userDatabase.child("czyLekarz").setValue(role.equals("Lekarz"));
        userDatabase.child("płeć").setValue(gender);

        if (role.equals("Pacjent")) {
            if (TextUtils.isEmpty(dateOfBirth) || height == 0 || weight == 0) {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }
            userDatabase.child("dataUrodzenia").setValue(dateOfBirth);
            userDatabase.child("wzrost").setValue(height);
            userDatabase.child("waga").setValue(weight);
            userDatabase.child("osobaKontaktowa").setValue(contactPerson);
            userDatabase.child("numerTelefonuKontaktowej").setValue(contactPhoneNumber);

            // Zapisz UID wybranego lekarza
            String selectedDoctorName = doctorSpinner.getSelectedItem().toString();
            String selectedDoctorUid = doctorMap.get(selectedDoctorName);
            userDatabase.child("lekarzProwadzacyUid").setValue(selectedDoctorUid);
        }

        Toast.makeText(this, "Dane zapisane", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(InitialSettingsActivity.this, MainActivity.class));
        finish();
    }
    private boolean isValidName(String name) {
        return !name.matches(".*\\d.*");
    }

    private boolean isValidDate(String date) {
        return date.matches("([0-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/\\d{4}");
    }

    private boolean isValidHeight(String height) {
        return height.matches("\\d+");
    }

    private boolean isValidWeight(String weight) {
        return weight.matches("\\d+");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{9}");
    }
    private boolean isValidContactPerson(String contactPerson) {
        return !contactPerson.isEmpty() && !contactPerson.matches(".*\\d.*");
    }
}


