package com.party.kardiol0g;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private TextView textViewName;
    private EditText editTextHeight, editTextWeight, editTextContactPerson, editTextContactPhoneNumber;
    private Button buttonSave, buttonBack;
    private Spinner doctorSpinner;
    private ArrayAdapter<String> doctorAdapter;
    private Map<String, String> doctorMap = new HashMap<>(); // Initialize doctorMap to avoid NullPointerException
    private DatabaseReference userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        textViewName = findViewById(R.id.textViewName);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextContactPerson = findViewById(R.id.editTextContactPerson);
        editTextContactPhoneNumber = findViewById(R.id.editTextContactPhoneNumber);
        buttonSave = findViewById(R.id.buttonSave);
        buttonBack = findViewById(R.id.buttonBack);
        doctorSpinner = findViewById(R.id.doctorSpinner);

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Wczytaj dane użytkownika i ustaw pola EditText oraz TextView dla imienia i nazwiska
        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String firstName = dataSnapshot.child("imie").getValue(String.class);
                String lastName = dataSnapshot.child("nazwisko").getValue(String.class);
                String fullName = firstName + " " + lastName;
                String height = dataSnapshot.child("waga").getValue(String.class);
                String weight = dataSnapshot.child("wzrost").getValue(String.class);
                String contactPerson = dataSnapshot.child("osobaKontaktowa").getValue(String.class);
                String contactPhoneNumber = dataSnapshot.child("numerTelefonuKontaktowej").getValue(String.class);
                String selectedDoctorUid = dataSnapshot.child("lekarzProwadzacyUid").getValue(String.class);

                textViewName.setText(fullName);
                editTextHeight.setText(height);
                editTextWeight.setText(weight);
                editTextContactPerson.setText(contactPerson != null ? contactPerson : "");
                editTextContactPhoneNumber.setText(contactPhoneNumber != null ? contactPhoneNumber : "");

                // Pobierz listę lekarzy z bazy danych Firebase
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
                        doctorAdapter = new ArrayAdapter<>(SettingsActivity.this, android.R.layout.simple_spinner_item, doctorNames);
                        doctorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        doctorSpinner.setAdapter(doctorAdapter);

                        // Wybierz z listy lekarza prowadzącego użytkownika
                        for (String doctorName : doctorNames) {
                            if (doctorName.equals(selectedDoctorUid)) {
                                int position = doctorAdapter.getPosition(doctorName);
                                doctorSpinner.setSelection(position);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Obsługa błędu
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Obsługa błędu
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void saveSettings() {
        String height = editTextHeight.getText().toString().trim();
        String weight = editTextWeight.getText().toString().trim();
        String contactPerson = editTextContactPerson.getText().toString().trim();
        String contactPhoneNumber = editTextContactPhoneNumber.getText().toString().trim();
        String selectedDoctorName = doctorSpinner.getSelectedItem().toString();
        String selectedDoctorUid = doctorMap.get(selectedDoctorName);

        // Sprawdź, czy pola nie są puste
        if (TextUtils.isEmpty(height)) {
            Toast.makeText(SettingsActivity.this, "Podaj wzrost", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(weight)) {
            Toast.makeText(SettingsActivity.this, "Podaj wagę", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(contactPerson)) {
            Toast.makeText(SettingsActivity.this, "Podaj osobę kontaktową", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(contactPhoneNumber)) {
            Toast.makeText(SettingsActivity.this, "Podaj numer telefonu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sprawdź poprawność danych
        if (!isValidName(contactPerson)) {
            Toast.makeText(SettingsActivity.this, "Niepoprawna nazwa kontaktowej osoby", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPhoneNumber(contactPhoneNumber)) {
            Toast.makeText(SettingsActivity.this, "Niepoprawny numer telefonu (9 cyfr)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidHeight(height)) {
            Toast.makeText(SettingsActivity.this, "Niepoprawny wzrost (wartość liczbową)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidWeight(weight)) {
            Toast.makeText(SettingsActivity.this, "Niepoprawna waga (wartość liczbową)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aktualizuj dane użytkownika w bazie danych
        userDatabase.child("wzrost").setValue(height);
        userDatabase.child("waga").setValue(weight);
        userDatabase.child("osobaKontaktowa").setValue(contactPerson);
        userDatabase.child("numerTelefonuKontaktowej").setValue(contactPhoneNumber);
        userDatabase.child("lekarzProwadzacyUid").setValue(selectedDoctorUid);
        Toast.makeText(SettingsActivity.this, "Dane zaktualizowane", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        finish();
    }

    private boolean isValidName(String name) {
        return !name.matches(".*\\d.*");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{9}");
    }

    private boolean isValidHeight(String height) {
        return height.matches("\\d+");
    }

    private boolean isValidWeight(String weight) {
        return weight.matches("\\d+");
    }
}
