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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pressure, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pressureListView = view.findViewById(R.id.pressureListView);
        pressureList = new ArrayList<>();

        preasureAdapter = new ArrayAdapter<Pressure>(getContext(), R.layout.item_pressure, pressureList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_pressure, parent, false);
                }
                Pressure pressure = pressureList.get(position);

                TextView textDayOfWeek = convertView.findViewById(R.id.textDayOfWeek);
                TextView textDate = convertView.findViewById(R.id.textDate);
                TextView textTime = convertView.findViewById(R.id.textTime);
                TextView textSystolic = convertView.findViewById(R.id.textSystolic);
                TextView textDiastolic = convertView.findViewById(R.id.textDiastolic);
                TextView textHeartrate = convertView.findViewById(R.id.textHeartrate);
                TextView textNote = convertView.findViewById(R.id.textNote);

                textDayOfWeek.setText(pressure.getDayOfWeek());
                textDate.setText("Data: " + pressure.getDate());
                textTime.setText("Godzina: " + pressure.getTime());
                textSystolic.setText("Ciśnienie skurczowe: " + pressure.getSystolic());
                textDiastolic.setText("Ciśnienie rozkurczowe: " + pressure.getDiastolic());
                textHeartrate.setText("Tętno: " + pressure.getHeartrate());
                textNote.setText("Notatka: " + pressure.getNote());

                return convertView;
            }
        };

        pressureListView.setAdapter(preasureAdapter);
        loadPressureData();

        pressureListView.setOnItemClickListener((adapterView, view1, position, id) -> {
            Pressure selectedPressure = pressureList.get(position);
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
                            pressure.setId(snapshot.getKey());
                            pressureList.add(pressure);
                        }
                    }
                    preasureAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

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
                    deletePressure(pressure);
                })
                .setNegativeButton("Nie", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}
