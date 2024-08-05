package com.party.kardiol0g;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.party.kardiol0g.files.FileData;
import com.party.kardiol0g.files.FilesFragment;
import com.party.kardiol0g.loginregister.LoginActivity;
import com.party.kardiol0g.medicine.Medicine;
import com.party.kardiol0g.medicine.MedicineFragment;
import com.party.kardiol0g.preasure.Pressure;
import com.party.kardiol0g.preasure.PressureFragment;
import com.party.kardiol0g.settings.SettingsActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity_Doctor extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FloatingActionButton fab;
    private DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    public static final String PREFS_NAME = "MyPrefsFile";
    private SharedPreferences sharedPreferences;
    boolean nightMode;
    InterstitialAd mInterstitialAd;
    private Uri selectedFileUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            // Tutaj możesz wykonać dodatkowe operacje związane z wybranym plikiem, jeśli są wymagane
        }
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        // Obsługa reklamy
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });

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
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new HomeFragment_Doctor()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
        replaceFragment(new HomeFragment_Doctor());
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    replaceFragment(new HomeFragment_Doctor());
                    break;
                case R.id.medicines:
                    replaceFragment(new MedicineFragment());
                    break;
                case R.id.files:
                    replaceFragment(new FilesFragment());
                    break;
                case R.id.bloodpreasure:
                    replaceFragment(new PressureFragment());
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
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(MainActivity_Doctor.this);
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            loadInterstitialAd(); // Ponowne ładowanie reklamy po jej zamknięciu
                        }
                    });
                } else {
                    Log.d("TAG", "Reklama nie jest gotowa.");
                }
                showBottomDialog();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new HomeFragment_Doctor()).commit();
                break;
            case R.id.nav_settings:
                startActivity(new Intent (MainActivity_Doctor.this, SettingsActivity.class));
                finish();
                break;
            case R.id.nav_share:
                startActivity(new Intent (MainActivity_Doctor.this, QRCodeActivity.class));
                Toast.makeText(this, "QR Kod!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_about:
                Toast.makeText(this, "Jesteś lekarzem!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                startActivity(new Intent (MainActivity_Doctor.this, LoginActivity.class));
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
        // Dodawanie ciśnienia
        addPressure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog[0].dismiss(); // Zamknięcie istniejącego dialogu, jeśli istnieje

                // Pobranie bieżącego użytkownika
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null) {
                    // Jeśli użytkownik nie jest zalogowany, wyświetl komunikat
                    Toast.makeText(MainActivity_Doctor.this, "Nie można dodać pomiaru. Użytkownik niezalogowany.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tworzenie okna dialogowego
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity_Doctor.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_pressure, null);
                EditText systolicEditText = dialogView.findViewById(R.id.systolicEditText);
                EditText diastolicEditText = dialogView.findViewById(R.id.diastolicEditText);
                EditText heartRateEditText = dialogView.findViewById(R.id.pulseEditText);
                EditText noteEditText = dialogView.findViewById(R.id.noteEditText);
                Button btnSave = dialogView.findViewById(R.id.btnAddPressure);

                // Ustawienie bieżącej daty i godziny
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentDate = dateFormat.format(calendar.getTime());
                String currentTime = timeFormat.format(calendar.getTime());

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                // Obsługa przycisków do zmniejszania i zwiększania wartości ciśnienia oraz tętna
                ImageButton btnSystolicMinus = dialogView.findViewById(R.id.btnSystolicMinus);
                ImageButton btnSystolicPlus = dialogView.findViewById(R.id.btnSystolicPlus);
                ImageButton btnDiastolicMinus = dialogView.findViewById(R.id.btnDiastolicMinus);
                ImageButton btnDiastolicPlus = dialogView.findViewById(R.id.btnDiastolicPlus);
                ImageButton btnPulseMinus = dialogView.findViewById(R.id.btnPulseMinus);
                ImageButton btnPulsePlus = dialogView.findViewById(R.id.btnPulsePlus);

                // Obsługa przycisku btnSystolicMinus
                btnSystolicMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currentSystolic = Integer.parseInt(systolicEditText.getText().toString());
                        if (currentSystolic > 0) {
                            systolicEditText.setText(String.valueOf(currentSystolic - 1));
                        }
                    }
                });

                // Obsługa przycisku btnSystolicPlus
                btnSystolicPlus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currentSystolic = Integer.parseInt(systolicEditText.getText().toString());
                        systolicEditText.setText(String.valueOf(currentSystolic + 1));
                    }
                });

                // Obsługa przycisku btnDiastolicMinus
                btnDiastolicMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currentDiastolic = Integer.parseInt(diastolicEditText.getText().toString());
                        if (currentDiastolic > 0) {
                            diastolicEditText.setText(String.valueOf(currentDiastolic - 1));
                        }
                    }
                });

                // Obsługa przycisku btnDiastolicPlus
                btnDiastolicPlus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currentDiastolic = Integer.parseInt(diastolicEditText.getText().toString());
                        diastolicEditText.setText(String.valueOf(currentDiastolic + 1));
                    }
                });

                // Obsługa przycisku btnPulseMinus
                btnPulseMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currentPulse = Integer.parseInt(heartRateEditText.getText().toString());
                        if (currentPulse > 0) {
                            heartRateEditText.setText(String.valueOf(currentPulse - 1));
                        }
                    }
                });

                // Obsługa przycisku btnPulsePlus
                btnPulsePlus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currentPulse = Integer.parseInt(heartRateEditText.getText().toString());
                        heartRateEditText.setText(String.valueOf(currentPulse + 1));
                    }
                });

                // Obsługa przycisku btnSave
                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Pobranie danych z pól tekstowych
                        String systolicString = systolicEditText.getText().toString();
                        String diastolicString = diastolicEditText.getText().toString();
                        String heartRateString = heartRateEditText.getText().toString();
                        String note = noteEditText.getText().toString();

                        // Sprawdzenie, czy pola są wypełnione
                        if (TextUtils.isEmpty(systolicString) || TextUtils.isEmpty(diastolicString) || TextUtils.isEmpty(heartRateString)) {
                            Toast.makeText(MainActivity_Doctor.this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Konwersja wartości na liczby całkowite
                        int systolic = Integer.parseInt(systolicString);
                        int diastolic = Integer.parseInt(diastolicString);
                        int heartRate = Integer.parseInt(heartRateString);

                        // Utworzenie obiektu Preasure i ustawienie wartości
                        Pressure pressure = new Pressure();
                        pressure.setSystolic(systolic);
                        pressure.setDiastolic(diastolic);
                        pressure.setHeartrate(heartRate);
                        pressure.setNote(note);
                        pressure.setDate(currentDate);
                        pressure.setTime(currentTime);

                        // Dodanie pomiaru do bazy danych Firebase Realtime Database pod użytkownikiem
                        DatabaseReference userPressureRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("Pressures").push();
                        userPressureRef.setValue(pressure)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Wyświetlenie komunikatu potwierdzającego dodanie pomiaru
                                        Toast.makeText(MainActivity_Doctor.this, "Dodano pomiar ciśnienia", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Wyświetlenie komunikatu o błędzie
                                        Toast.makeText(MainActivity_Doctor.this, "Błąd podczas dodawania pomiaru: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                        // Zamknięcie dialogu
                        dialog.dismiss();
                    }
                });

                Button btnCancelAddPressure = dialogView.findViewById(R.id.btnCancelAddPressure);
                btnCancelAddPressure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Zamknięcie dialogu
                        dialog.dismiss();
                    }
                });

                // Ustawienie tła dialogu na przezroczyste
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                // Wyświetlenie dialogu
                dialog.show();
            }
        });
        // Dodawanie leku
        addMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity_Doctor.this);
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

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity_Doctor.this, android.R.layout.simple_dropdown_item_1line, medicineNames);
                            medicineNameBox.setAdapter(adapter);

                            // Po zakończeniu wczytywania danych, ustaw elementy jako aktywne
                            medicineNameBox.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(MainActivity_Doctor.this, "Błąd pobierania nazw leków: " + task.getException(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(MainActivity_Doctor.this, "Wybierz lek", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(MainActivity_Doctor.this, "Wprowadź dawkę", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (TextUtils.isEmpty(quantity)) {
                            Toast.makeText(MainActivity_Doctor.this, "Wprowadź ilość", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(MainActivity_Doctor.this, "Lek już istnieje w bazie danych", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Jeśli lek nie istnieje, dodaj go do bazy danych
                                        String medicineId = userMedicinesRef.push().getKey();
                                        userMedicinesRef.child(medicineId).setValue(medicine)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Wyświetl komunikat potwierdzający dodanie leku
                                                        Toast.makeText(MainActivity_Doctor.this, "Lek dodany pomyślnie", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Obsługa błędu dodawania leku
                                                        Toast.makeText(MainActivity_Doctor.this, "Błąd podczas dodawania leku: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Obsługa błędu pobierania danych
                                    Toast.makeText(MainActivity_Doctor.this, "Błąd pobierania danych: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Jeśli użytkownik nie jest zalogowany, wyświetl komunikat o błędzie
                            Toast.makeText(MainActivity_Doctor.this, "Nie można dodać leku. Użytkownik niezalogowany.", Toast.LENGTH_SHORT).show();
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
        // Dodawanie pliku
        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog[0].dismiss();

                // Pobranie bieżącego użytkownika
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(MainActivity_Doctor.this, "Nie można dodać pliku. Użytkownik niezalogowany.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tworzenie okna dialogowego
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity_Doctor.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_file, null);
                EditText noteEditText = dialogView.findViewById(R.id.noteEditText);
                Spinner fileTypeSpinner = dialogView.findViewById(R.id.fileTypeSpinner);
                Button btnSelectFile = dialogView.findViewById(R.id.btnSelectFile);
                Button btnSaveFile = dialogView.findViewById(R.id.btnSaveFile);
                Button btnSelectDate = dialogView.findViewById(R.id.btnSelectDate);

                builder.setView(dialogView);
                AlertDialog fileDialog = builder.create();

                // Zmienna do przechowywania wybranej daty
                final String[] selectedDate = {""};

                // Obsługa wyboru daty
                btnSelectDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity_Doctor.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                selectedDate[0] = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                btnSelectDate.setText(selectedDate[0]);
                            }
                        }, year, month, day);
                        datePickerDialog.show();
                    }
                });

                // Obsługa przycisku wyboru pliku
                btnSelectFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/pdf"); // Tylko pliki PDF
                        startActivityForResult(Intent.createChooser(intent, "Wybierz plik PDF"), 1);
                    }
                });

                // Obsługa przycisku zapisu pliku
                btnSaveFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String note = noteEditText.getText().toString();
                        String fileType = fileTypeSpinner.getSelectedItem().toString();

                        // Zapisywanie pliku do Firebase Storage
                        if (selectedFileUri != null) {
                            if (selectedDate[0].isEmpty()) {
                                Toast.makeText(MainActivity_Doctor.this, "Wybierz datę przed zapisem pliku", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            String fileName = fileType + "_" + selectedDate[0] + ".pdf";
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Users").child(currentUser.getUid()).child("Files").child(fileName);
                            storageReference.putFile(selectedFileUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String fileUrl = uri.toString();

                                                    // Tworzenie obiektu FileData
                                                    FileData fileData = new FileData(fileUrl, note, fileType, fileName);

                                                    // Dodanie metadanych do bazy danych Firebase Realtime Database pod użytkownikiem
                                                    DatabaseReference userFilesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("Files").push();
                                                    userFilesRef.setValue(fileData)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Toast.makeText(MainActivity_Doctor.this, "Dodano plik", Toast.LENGTH_SHORT).show();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(MainActivity_Doctor.this, "Błąd podczas dodawania pliku: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity_Doctor.this, "Błąd podczas przesyłania pliku: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(MainActivity_Doctor.this, "Nie wybrano pliku", Toast.LENGTH_SHORT).show();
                        }


                        fileDialog.dismiss();
                    }
                });

                if (fileDialog.getWindow() != null) {
                    fileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
                fileDialog.show();
            }
        });

        // Czat z lekarzem, ale to nie wiem czy zdążę zrobić
        doctorChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog[0].dismiss();
                Toast.makeText(MainActivity_Doctor.this,"Czat z lekarzem kliknięte",Toast.LENGTH_SHORT).show();

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