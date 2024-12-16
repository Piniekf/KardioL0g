package com.party.kardiol0g.medicine;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.party.kardiol0g.R;

import java.util.ArrayList;
import java.util.List;

public class MedicineFragment extends Fragment {

    private List<Medicine> medicineList;
    private ArrayAdapter<Medicine> adapter;

    public MedicineFragment() {

    }

    public static MedicineFragment newInstance() {
        return new MedicineFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicine, container, false);
        ListView listViewMedicines = view.findViewById(R.id.listViewMedicines);
        medicineList = new ArrayList<>();
        adapter = new ArrayAdapter<Medicine>(requireContext(), R.layout.list_item_medicine, R.id.textMedicineName, medicineList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View listItemView = convertView;
                if (listItemView == null) {
                    listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_medicine, parent, false);
                }
                TextView medicineNameTextView = listItemView.findViewById(R.id.textMedicineName);
                TextView medicineDetailsTextView = listItemView.findViewById(R.id.textMedicineDetails);
                TextView medicineDoseTextView = listItemView.findViewById(R.id.textMedicineDose);
                TextView medicineQuantityTextView = listItemView.findViewById(R.id.textMedicineQuantity);
                TextView medicineNoteTextView = listItemView.findViewById(R.id.textMedicineNote);

                TextView morningTextView = listItemView.findViewById(R.id.textMorning);
                TextView noonTextView = listItemView.findViewById(R.id.textNoon);
                TextView eveningTextView = listItemView.findViewById(R.id.textEvening);

                Medicine medicine = getItem(position);

                if (medicine != null) {
                    medicineNameTextView.setText("Nazwa: " + medicine.getName());
                    medicineDetailsTextView.setText("Moc: " + medicine.getStrength());
                    medicineDoseTextView.setText("Dawka: " + medicine.getDose());
                    medicineQuantityTextView.setText("Ilość: " + medicine.getQuantity());
                    morningTextView.setText(medicine.isMorning() ? "Rano: Tak |" : "Rano: Nie |");
                    noonTextView.setText(medicine.isNoon() ? "Południe: Tak |" : "Południe: Nie |");
                    eveningTextView.setText(medicine.isEvening() ? "Wieczór: Tak" : "Wieczór: Nie");
                    medicineNoteTextView.setText("Notatka: " + medicine.getNote());

                    // Obsługa kliknięcia elementu na liście
                    listItemView.setOnClickListener(v -> editOrDeleteMedicine(medicine));
                }

                return listItemView;
            }
        };
        listViewMedicines.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUserMedicines();
    }

    private void loadUserMedicines() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userMedicinesRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(currentUser.getUid())
                    .child("Medicines");
            userMedicinesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    medicineList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Medicine medicine = snapshot.getValue(Medicine.class);
                        if (medicine != null) {
                            medicine.setId(snapshot.getKey());
                            medicineList.add(medicine);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    // Metoda do obsługi edycji lub usuwania leku
    private void editOrDeleteMedicine(Medicine medicine) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Wybierz akcję")
                .setMessage("Co chcesz zrobić z tym lekiem?")
                .setPositiveButton("Edytuj", (dialog, which) -> editMedicine(medicine))
                .setNegativeButton("Usuń", (dialog, which) -> deleteMedicine(medicine))
                .setNeutralButton("Anuluj", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Metoda do edycji leku
    private void editMedicine(Medicine medicine) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_medicine, null);

        EditText medicineNameBox = dialogView.findViewById(R.id.medicineNameBox);
        Spinner doseSpinner = dialogView.findViewById(R.id.doseSpinner);
        EditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
        CheckBox morningCheckBox = dialogView.findViewById(R.id.morningCheckBox);
        CheckBox noonCheckBox = dialogView.findViewById(R.id.noonCheckBox);
        CheckBox eveningCheckBox = dialogView.findViewById(R.id.eveningCheckBox);
        EditText noteEditText = dialogView.findViewById(R.id.noteEditText);

        // Ustawienie wartości pól edycji na podstawie danych leku
        medicineNameBox.setText(medicine.getName());
        medicineNameBox.setEnabled(false); // Ustawienie pola nazwy leku jako nieedytowalne
        doseSpinner.getSelectedItem();
        quantityEditText.setText(medicine.getQuantity());
        morningCheckBox.setChecked(medicine.isMorning());
        noonCheckBox.setChecked(medicine.isNoon());
        eveningCheckBox.setChecked(medicine.isEvening());
        noteEditText.setText(medicine.getNote());

        builder.setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        dialog.show();

        // Ustawienie przezroczystego tła dla okna dialogowego
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btnUpdateMedicine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pobierz zmienione wartości z pól edycji
                String newName = medicineNameBox.getText().toString();
                String newDose = doseSpinner.getSelectedItem().toString();
                String newQuantity = quantityEditText.getText().toString();
                boolean newMorning = morningCheckBox.isChecked();
                boolean newNoon = noonCheckBox.isChecked();
                boolean newEvening = eveningCheckBox.isChecked();
                String newNote = noteEditText.getText().toString();

                // Zaktualizuj dane leku w bazie danych Firebase
                updateMedicine(medicine, newName, newDose, newQuantity, newMorning, newNoon, newEvening, newNote);

                // Zamknij dialog
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.btnCancelEditMedicine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Wywołaj metodę usuwania leku
                deleteMedicine(medicine);

                // Zamknij dialog
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.btnCancelEditMedicine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Zamknij dialog
                dialog.dismiss();
            }
        });
    }




    // Metoda do usuwania leku
    private void deleteMedicine(Medicine medicine) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userMedicinesRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(currentUser.getUid())
                    .child("Medicines")
                    .child(medicine.getId());

            userMedicinesRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Lek usunięty", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Błąd podczas usuwania leku", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Metoda do aktualizacji danych leku w bazie danych Firebase
    private void updateMedicine(Medicine medicine, String newName, String newDose, String newQuantity, boolean newMorning, boolean newNoon, boolean newEvening, String newNote) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userMedicinesRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(currentUser.getUid())
                    .child("Medicines")
                    .child(medicine.getId());

            // Ustaw nowe wartości leku
            medicine.setName(newName);
            medicine.setDose(newDose);
            medicine.setQuantity(newQuantity);
            medicine.setMorning(newMorning);
            medicine.setNoon(newNoon);
            medicine.setEvening(newEvening);
            medicine.setNote(newNote);

            // Zaktualizuj dane leku w bazie danych
            userMedicinesRef.setValue(medicine)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Lek zaktualizowany", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Błąd podczas aktualizacji leku", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
