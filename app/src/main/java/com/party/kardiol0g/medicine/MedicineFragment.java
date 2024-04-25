package com.party.kardiol0g.medicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
        // Required empty public constructor
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
                            medicineList.add(medicine);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                }
            });
        }
    }

    // Metoda do obsługi edycji lub usuwania leku
    private void editOrDeleteMedicine(Medicine medicine) {
        // Tutaj dodaj kod do obsługi edycji lub usuwania leku
    }
}
