package com.party.kardiol0g;

import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MedicineFragment extends Fragment {

    private ListView listViewMedicines;
    private List<String> medicineList;
    private ArrayAdapter<String> adapter;

    public MedicineFragment() {
        // Wymagany pusty konstruktor
    }

    public static MedicineFragment newInstance() {
        return new MedicineFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_medicine, container, false);
        listViewMedicines = view.findViewById(R.id.listViewMedicines);
        medicineList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(requireContext(), R.layout.list_item_medicine, R.id.textMedicineName, medicineList) {
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

                String medicineInfo = getItem(position);
                String[] medicineInfoArray = medicineInfo.split("\n");
                Log.d("MedicineFragment", "Medicine Info Array: " + Arrays.toString(medicineInfoArray));

                medicineNameTextView.setText(medicineInfoArray[0]);
                medicineDetailsTextView.setText(medicineInfoArray[1]);
                medicineDoseTextView.setText(medicineInfoArray[2]);
                medicineQuantityTextView.setText(medicineInfoArray[3]);
                morningTextView.setText(medicineInfoArray[4].contains("Tak") ? "Rano: Tak |" : "Rano: Nie |");
                noonTextView.setText(medicineInfoArray[5].contains("Tak") ? "Południe: Tak |" : "Południe: Nie |");
                eveningTextView.setText(medicineInfoArray[6].contains("Tak") ? "Wieczór: Tak" : "Wieczór: Nie");
                medicineNoteTextView.setText(medicineInfoArray[7]);

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
                        String medicineName = snapshot.child("name").getValue(String.class);
                        String medicineStrength = snapshot.child("strength").getValue(String.class);
                        String dose = snapshot.child("dose").getValue(String.class);
                        String quantity = snapshot.child("quantity").getValue(String.class);
                        boolean morning = Boolean.TRUE.equals(snapshot.child("morning").getValue(Boolean.class));
                        boolean noon = Boolean.TRUE.equals(snapshot.child("noon").getValue(Boolean.class));
                        boolean evening = Boolean.TRUE.equals(snapshot.child("evening").getValue(Boolean.class));
                        String note = snapshot.child("note").getValue(String.class);

                        Log.d("MedicineFragment", "Morning: " + morning);
                        Log.d("MedicineFragment", "Noon: " + noon);
                        Log.d("MedicineFragment", "Evening: " + evening);

                        String medicineInfo = "Nazwa: " + medicineName + "\n" +
                                "Moc: " + medicineStrength + "\n" +
                                "Dawka: " + dose + "\n" +
                                "Ilość: " + quantity + "\n" +
                                "Rano: " + (morning ? "Tak" : "Nie") + "\n" +
                                "Południe: " + (noon ? "Tak" : "Nie") + "\n" +
                                "Wieczór: " + (evening ? "Tak" : "Nie") + "\n" +
                                "Notatka: " + note + "\n";
                        medicineList.add(medicineInfo);
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

}