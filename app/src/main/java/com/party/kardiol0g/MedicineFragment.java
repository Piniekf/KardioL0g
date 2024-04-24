package com.party.kardiol0g;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import java.util.List;
import java.util.Map;

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
        // Inflate the layout for this fragment
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
                ImageView morningImageView = listItemView.findViewById(R.id.imageMorning);
                ImageView noonImageView = listItemView.findViewById(R.id.imageNoon);
                ImageView eveningImageView = listItemView.findViewById(R.id.imageEvening);

                String medicineInfo = getItem(position);
                String[] medicineInfoArray = medicineInfo.split("\n");

                medicineNameTextView.setText(medicineInfoArray[0]);
                medicineDetailsTextView.setText(medicineInfoArray[1]);
                medicineDoseTextView.setText(medicineInfoArray[2]);
                medicineQuantityTextView.setText(medicineInfoArray[3]);
                medicineNoteTextView.setText(medicineInfoArray[7]);

                morningImageView.setVisibility(medicineInfoArray[4].equals("true") ? View.VISIBLE : View.GONE);
                noonImageView.setVisibility(medicineInfoArray[5].equals("true") ? View.VISIBLE : View.GONE);
                eveningImageView.setVisibility(medicineInfoArray[6].equals("true") ? View.VISIBLE : View.GONE);


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
                        Map<String, Object> medicineData = (Map<String, Object>) snapshot.getValue();
                        if (medicineData != null) {
                            String medicineName = (String) medicineData.get("name");
                            String medicineStrength = (String) medicineData.get("strength");
                            String dose = (String) medicineData.get("dose");
                            String quantity = (String) medicineData.get("quantity");
                            boolean morning = (boolean) medicineData.get("morning");
                            boolean noon = (boolean) medicineData.get("noon");
                            boolean evening = (boolean) medicineData.get("evening");
                            String note = (String) medicineData.get("note");

                            String medicineInfo = "Nazwa: " + medicineName + "\n" +
                                    "Moc: " + medicineStrength + "\n" +
                                    "Dawka: " + dose + "\n" +
                                    "Ilość: " + quantity + "\n" +
                                    "Rano: " + morning + "\n" +
                                    "Południe: " + noon + "\n" +
                                    "Wieczór: " + evening + "\n" +
                                    "Notatka: " + note + "\n";
                            medicineList.add(medicineInfo);
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
}
