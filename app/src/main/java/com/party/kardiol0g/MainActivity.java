package com.party.kardiol0g;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.party.kardiol0g.loginregister.LoginActivity;
import com.party.kardiol0g.medicine.Medicine;
import com.party.kardiol0g.medicine.MedicineFragment;
import com.party.kardiol0g.preasure.PreasureFragment;
import com.party.kardiol0g.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FloatingActionButton fab;
    private DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    public static final String PREFS_NAME = "MyPrefsFile";
    private SharedPreferences sharedPreferences;
    boolean nightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Inicjalizacja sharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);

        // Odczytanie preferencji motywu
        nightMode = sharedPreferences.getBoolean("nightMode", false);

        // Ustawienie motywu na podstawie preferencji
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Pobranie danych użytkownika i ustawienie ich w nav_header
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("imie").getValue(String.class);
                        String surname = dataSnapshot.child("nazwisko").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);

                        View headerView = navigationView.getHeaderView(0);
                        TextView nameTextView = headerView.findViewById(R.id.name);
                        TextView emailTextView = headerView.findViewById(R.id.email);
                        nameTextView.setText(name + " " + surname);
                        emailTextView.setText(email);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Obsługa błędu pobierania danych
                }
            });
        }

        // Obsługa fragmnetów
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
        replaceFragment(new HomeFragment());
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.medicines:
                    replaceFragment(new MedicineFragment());
                    break;
                case R.id.files:
                    replaceFragment(new FilesFragment());
                    break;
                case R.id.bloodpreasure:
                    replaceFragment(new PreasureFragment());
                    break;
                case R.id.more:
                    replaceFragment(new MoreFragment());
                    break;
            }
            return true;
        });
        // Obsługa guzika plus
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new HomeFragment()).commit();
                break;
            case R.id.nav_settings:
                startActivity(new Intent (MainActivity.this, SettingsActivity.class));
                finish();
                break;
            case R.id.nav_share:
                startActivity(new Intent (MainActivity.this, QRCodeActivity.class));
                Toast.makeText(this, "QR Kod!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_about:
                Toast.makeText(this, "O nas!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                startActivity(new Intent (MainActivity.this, LoginActivity.class));
                finish();
                Toast.makeText(this, "Wylogowano!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.theme_switch:
                if(nightMode){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    nightMode = false;
                } else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    nightMode = true;
                }

                // Zapisanie wartości nightMode w SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("nightMode", nightMode);
                editor.apply();

                Toast.makeText(this,"Zmiana motywu", Toast.LENGTH_SHORT).show();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // metoda od przycisku wstecz przy bocznej szufladzie
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    // metody od zmiany framgnetu
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
    // metody od dolnego menu
    private void showBottomDialog() {
        final Dialog[] dialog = {new Dialog(this)};
        dialog[0].requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog[0].setContentView(R.layout.bottomsheetlayout);
        LinearLayout addPressure = dialog[0].findViewById(R.id.addPreasure);
        LinearLayout addMedicine = dialog[0].findViewById(R.id.addMedicine);
        LinearLayout addFile = dialog[0].findViewById(R.id.addFile);
        LinearLayout doctorChat = dialog[0].findViewById(R.id.doctorChat);
        ImageView cancelButton = dialog[0].findViewById(R.id.cancelButton);

        addPressure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog[0].dismiss();
                Toast.makeText(MainActivity.this,"Dodaj ciśnienie kliknięte",Toast.LENGTH_SHORT).show();
            }
        });
        // Dodawanie leku
        addMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medicine, null);
                AutoCompleteTextView medicineNameBox = dialogView.findViewById(R.id.medicineNameBox);
                Button btnAddMedicine = dialogView.findViewById(R.id.btnAddMedicine);
                ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
                Spinner doseSpinner = dialogView.findViewById(R.id.doseSpinner);
                EditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
                CheckBox morningCheckBox = dialogView.findViewById(R.id.morningCheckBox);
                CheckBox noonCheckBox = dialogView.findViewById(R.id.noonCheckBox);
                CheckBox eveningCheckBox = dialogView.findViewById(R.id.eveningCheckBox);
                EditText noteEditText = dialogView.findViewById(R.id.noteEditText);


                // Deklaracja listy medicineNames
                List<String> medicineNames = new ArrayList<>();

                // Pobierz listę leków z Firestore i ustaw ją jako źródło podpowiedzi dla AutoCompleteTextView
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference medicinesRef = db.collection("medicines");

                // Ustawienie wszystkich elementów jako nieaktywne początkowo
                medicineNameBox.setEnabled(false);
                btnAddMedicine.setEnabled(false);

                medicinesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String medicineName = document.getString("Nazwa Produktu Leczniczego");
                                String medicineStrength = document.getString("Moc");
                                if (medicineName != null && medicineStrength != null) {
                                    medicineNames.add(medicineName + " - " + medicineStrength); // Dodaj zarówno nazwę, jak i moc do listy
                                }
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, medicineNames);
                            medicineNameBox.setAdapter(adapter);

                            // Po zakończeniu wczytywania danych, ustaw elementy jako aktywne
                            medicineNameBox.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(MainActivity.this, "Błąd pobierania nazw leków: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Pokaż progressBar na początku wczytywania danych
                progressBar.setVisibility(View.VISIBLE);

                // Dodaj nasłuchiwacz na zmiany w polu AutoCompleteTextView
                medicineNameBox.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void afterTextChanged(Editable editable) {
                        // Sprawdź, czy wpisany tekst pasuje do jednej z sugerowanych opcji
                        String enteredText = editable.toString();
                        if (medicineNames.contains(enteredText)) {
                            // Włącz przycisk dodawania, jeśli wpisany tekst pasuje do jednej z sugerowanych opcji
                            btnAddMedicine.setEnabled(true);
                            btnAddMedicine.setAlpha(1f); // Przywróć normalną widoczność przycisku
                        } else {
                            // Wyłącz przycisk dodawania, jeśli wpisany tekst nie pasuje do żadnej z sugerowanych opcji
                            btnAddMedicine.setEnabled(false);
                            btnAddMedicine.setAlpha(0.5f); // Wyszarz przycisk
                        }
                    }
                });

                builder.setView(dialogView);
                androidx.appcompat.app.AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.btnAddMedicine).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String medicineNameWithStrength = medicineNameBox.getText().toString();
                        if (TextUtils.isEmpty(medicineNameWithStrength)){
                            Toast.makeText(MainActivity.this, "Wybierz lek", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Rozdzielanie nazwy leku i mocy
                        String[] parts = medicineNameWithStrength.split(" - ");
                        String medicineName = parts[0];
                        String medicineStrength = parts[1];

                        // Pobierz wartość dawki ze spinnera
                        String dose = doseSpinner.getSelectedItem().toString();

                        // Pobierz ilość z EditText
                        String quantity = quantityEditText.getText().toString();

                        if (TextUtils.isEmpty(dose)) {
                            Toast.makeText(MainActivity.this, "Wprowadź dawkę", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (TextUtils.isEmpty(quantity)) {
                            Toast.makeText(MainActivity.this, "Wprowadź ilość", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Sprawdź, czy pole wyboru jest zaznaczone
                        boolean isMorningChecked = morningCheckBox.isChecked();
                        boolean isNoonChecked = noonCheckBox.isChecked();
                        boolean isEveningChecked = eveningCheckBox.isChecked();

                        // Pobierz notatkę z EditText
                        String note = noteEditText.getText().toString();

                        // Tworzymy obiekt Medicine
                        Medicine medicine = new Medicine();
                        medicine.setName(medicineName);
                        medicine.setStrength(medicineStrength);
                        medicine.setDose(dose);
                        medicine.setQuantity(quantity);
                        medicine.setMorning(isMorningChecked);
                        medicine.setNoon(isNoonChecked);
                        medicine.setEvening(isEveningChecked);
                        medicine.setNote(note);

                        // Sprawdź, czy wybrany lek już istnieje w bazie danych użytkownika
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            DatabaseReference userMedicinesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("Medicines");

                            // Pobierz listę leków użytkownika z bazy danych
                            userMedicinesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    boolean medicineExists = false;
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Medicine existingMedicine = snapshot.getValue(Medicine.class);
                                        if (existingMedicine != null && existingMedicine.getName().equals(medicineName) && existingMedicine.getStrength().equals(medicineStrength)) {
                                            // Jeśli lek już istnieje w bazie danych, ustaw flagę na true
                                            medicineExists = true;
                                            break;
                                        }
                                    }
                                    if (medicineExists) {
                                        // Jeśli lek już istnieje, wyświetl odpowiedni komunikat
                                        Toast.makeText(MainActivity.this, "Lek już istnieje w bazie danych", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Jeśli lek nie istnieje, dodaj go do bazy danych
                                        String medicineId = userMedicinesRef.push().getKey();
                                        userMedicinesRef.child(medicineId).setValue(medicine)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Wyświetl komunikat potwierdzający dodanie leku
                                                        Toast.makeText(MainActivity.this, "Lek dodany pomyślnie", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Obsługa błędu dodawania leku
                                                        Toast.makeText(MainActivity.this, "Błąd podczas dodawania leku: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Obsługa błędu pobierania danych
                                    Toast.makeText(MainActivity.this, "Błąd pobierania danych: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Jeśli użytkownik nie jest zalogowany, wyświetl komunikat o błędzie
                            Toast.makeText(MainActivity.this, "Nie można dodać leku. Użytkownik niezalogowany.", Toast.LENGTH_SHORT).show();
                        }

                        // Zamknij dialog
                        dialog.dismiss();
                    }
                });


                dialogView.findViewById(R.id.btnCancelAddMedicine).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Zamknij dialog
                        dialog.dismiss();
                    }
                });

                if (dialog.getWindow() != null){
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                dialog.show();
            }
        });

        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog[0].dismiss();
                Toast.makeText(MainActivity.this,"Dodaj plik kliknięte",Toast.LENGTH_SHORT).show();
            }
        });
        doctorChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog[0].dismiss();
                Toast.makeText(MainActivity.this,"Czat z lekarzem kliknięte",Toast.LENGTH_SHORT).show();

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog[0].dismiss();
            }
        });
        dialog[0].show();
        dialog[0].getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog[0].getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog[0].getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog[0].getWindow().setGravity(Gravity.BOTTOM);
    }
}
