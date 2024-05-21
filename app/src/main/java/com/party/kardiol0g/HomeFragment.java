package com.party.kardiol0g;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.party.kardiol0g.medicine.Medicine;
import com.party.kardiol0g.preasure.Pressure;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private LineChart pressureChart;
    private PieChart medicineChart;
    private DatabaseReference databaseReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        pressureChart = view.findViewById(R.id.pressureChart);
        medicineChart = view.findViewById(R.id.medicineChart);

        // Inicjalizacja Firebase Realtime Database
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users").child(currentUser.getUid());

        // Pobierz dane z Firebase i zaktualizuj wykresy
        loadPressureData();
        loadMedicineData();

        return view;
    }

    private void loadPressureData() {
        databaseReference.child("Pressures").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Entry> systolicEntries = new ArrayList<>();
                List<Entry> diastolicEntries = new ArrayList<>();
                List<String> dates = new ArrayList<>(); // Lista przechowująca daty

                // Ograniczenie do 7 wpisów
                int counter = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (counter >= 7) break; // Ograniczenie do 7 wpisów

                    Pressure pressure = snapshot.getValue(Pressure.class);
                    if (pressure != null) {
                        // Formatuj datę tak, aby zawierała tylko miesiąc i dzień
                        String formattedDate = formatToMonthDay(pressure.getDate());
                        // Dodaj sformatowaną datę do listy
                        dates.add(formattedDate);
                        systolicEntries.add(new Entry(counter, (float) pressure.getSystolic()));
                        diastolicEntries.add(new Entry(counter, (float) pressure.getDiastolic()));

                        counter++;
                    }
                }

                LineDataSet systolicDataSet = new LineDataSet(systolicEntries, "Skurczowe (mmHg)");
                LineDataSet diastolicDataSet = new LineDataSet(diastolicEntries, "Rozkurczowe (mmHg)");
                systolicDataSet.setColor(ColorTemplate.COLORFUL_COLORS[0]);
                systolicDataSet.setValueTextSize(12f);
                diastolicDataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
                diastolicDataSet.setValueTextSize(12f);

                // Ustawienia kolorów napisów dla LineDataSet
                systolicDataSet.setValueTextColor(Color.GRAY);
                diastolicDataSet.setValueTextColor(Color.GRAY);

                LineData lineData = new LineData(systolicDataSet, diastolicDataSet);

                // Ustawienia osi X
                XAxis xAxis = pressureChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
                xAxis.setTextColor(Color.GRAY);

                // Ustawienia osi Y (lewa)
                YAxis leftAxis = pressureChart.getAxisLeft();
                leftAxis.setTextColor(Color.GRAY);

                // Ustawienia osi Y (prawa)
                YAxis rightAxis = pressureChart.getAxisRight();
                rightAxis.setTextColor(Color.GRAY);

                // Ustawienia opisu wykresu
                Description pressureDescription = new Description();
                pressureDescription.setText("Wykres ciśnienia krwi");
                pressureDescription.setTextColor(Color.GRAY);
                pressureChart.setDescription(pressureDescription);

                // Ustawienia legendy wykresu
                Legend legend = pressureChart.getLegend();
                legend.setTextColor(Color.GRAY);

                pressureChart.setData(lineData);
                pressureChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Obsługa błędu
            }
        });
    }


    private void loadMedicineData() {
        databaseReference.child("Medicines").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<PieEntry> entries = new ArrayList<>();
                List<String> medicineNames = new ArrayList<>(); // Lista przechowująca nazwy leków

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Medicine medicine = snapshot.getValue(Medicine.class);
                    if (medicine != null) {
                        String name = medicine.getName() + " " + medicine.getStrength();
                        float quantity = Float.parseFloat(medicine.getQuantity());
                        entries.add(new PieEntry(quantity, name));
                        medicineNames.add(name); // Dodaj nazwę leku do listy
                    }
                }
                medicineChart.setDrawHoleEnabled(false);
                medicineChart.setEntryLabelColor(Color.GRAY);
                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                dataSet.setValueTextColor(Color.BLACK);
                dataSet.setValueTextSize(12f);

                PieData pieData = new PieData(dataSet);
                medicineChart.setData(pieData);

                // Ustawienia opisu wykresu
                Description medicineDescription = new Description();
                medicineDescription.setText("Wykres leków");
                medicineDescription.setTextColor(Color.GRAY);
                medicineChart.setDescription(medicineDescription);

                // Dodaj legendę z nazwami leków i odpowiadającymi kolorami
                List<LegendEntry> legendEntries = new ArrayList<>();
                for (int i = 0; i < medicineNames.size(); i++) {
                    LegendEntry entry = new LegendEntry();
                    entry.label = medicineNames.get(i);
                    entry.formColor = dataSet.getColors().get(i % dataSet.getColors().size());
                    legendEntries.add(entry);
                }
                Legend legend = medicineChart.getLegend();
                legend.setCustom(legendEntries);
                legend.setTextColor(Color.GRAY);
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                legend.setDrawInside(false);

                medicineChart.invalidate(); // Odświeżenie wykresu
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Obsługa błędu
            }
        });
    }



    // Metoda do formatowania daty do postaci "MM-dd" lub "dd.MM"
    private String formatToMonthDay(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat newDateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
            return newDateFormat.format(dateFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
