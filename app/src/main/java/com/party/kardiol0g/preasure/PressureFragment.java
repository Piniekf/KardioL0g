package com.party.kardiol0g.preasure;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

public class PressureFragment extends Fragment {

    private ListView pressureListView;
    private ArrayAdapter<Pressure> preasureAdapter;
    private List<Pressure> pressureList;

    public PressureFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pressure, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pressureListView = view.findViewById(R.id.pressureListView);
        pressureList = new ArrayList<>();

        preasureAdapter = new ArrayAdapter<Pressure>(getContext(), android.R.layout.simple_list_item_1, pressureList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);
                Pressure pressure = pressureList.get(position);

                // Ustawienie tekstu dla pojedynczego elementu listy
                TextView textView = itemView.findViewById(android.R.id.text1);
                textView.setText("Skurczowe: " + pressure.getSystolic() + ", Rozkurczowe: " + pressure.getDiastolic() +
                        ", Tętno: " + pressure.getHeartrate() +
                        ", Data pomiaru: " + pressure.getDate() + ", Godzina pomiaru: " + pressure.getTime());
                return itemView;
            }
        };

        pressureListView.setAdapter(preasureAdapter);
        loadPressureData();

        // Dodanie obsługi kliknięcia na element listy
        pressureListView.setOnItemClickListener((adapterView, view1, position, id) -> {
            // Pobranie wybranego pomiaru ciśnienia
            Pressure selectedPressure = pressureList.get(position);
            // Wywołanie metody wyświetlającej okno dialogowe z pytaniem o usunięcie pomiaru ciśnienia
            showDeletePressureDialog(selectedPressure);
        });
    }

    private void loadPressureData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference pressureRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(currentUser.getUid())
                    .child("Pressures");

            pressureRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    pressureList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Pressure pressure = snapshot.getValue(Pressure.class);
                        if (pressure != null) {
                            pressure.setId(snapshot.getKey()); // Ustawienie ID pomiaru ciśnienia
                            pressureList.add(pressure);
                        }
                    }
                    preasureAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Obsługa błędu
                }
            });
        }
    }

    // Metoda do usuwania pomiaru ciśnienia
    private void deletePressure(Pressure pressure) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userPressureRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(currentUser.getUid())
                    .child("Pressures")
                    .child(pressure.getId());

            userPressureRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Pomiar ciśnienia usunięty", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Błąd podczas usuwania pomiaru ciśnienia", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void showDeletePressureDialog(Pressure pressure) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Usuń pomiar ciśnienia")
                .setMessage("Czy na pewno chcesz usunąć ten pomiar ciśnienia?")
                .setPositiveButton("Tak", (dialog, which) -> {
                    // Jeśli użytkownik potwierdzi usunięcie, wywołujemy metodę usuwającą pomiar ciśnienia
                    deletePressure(pressure);
                })
                .setNegativeButton("Nie", (dialog, which) -> {
                    // Jeśli użytkownik anuluje usunięcie, zamykamy okno dialogowe
                    dialog.dismiss();
                })
                .show();
    }
}
