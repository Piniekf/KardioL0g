package com.party.kardiol0g;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.party.kardiol0g.files.FileData;

public class PatientDetailsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private TextView tvPatientDetails;
    private RecyclerView recyclerViewFiles;
    private FirebaseRecyclerAdapter<FileData, FileDataViewHolder> fileAdapter;
    private String patientUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        tvPatientDetails = findViewById(R.id.tvPatientDetails);
        recyclerViewFiles = findViewById(R.id.recyclerViewFiles);
        recyclerViewFiles.setLayoutManager(new LinearLayoutManager(this));

        patientUid = getIntent().getStringExtra("patientUid");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(patientUid);

        setupFileAdapter();
        loadPatientDetails();
    }

    private void loadPatientDetails() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                StringBuilder details = new StringBuilder();
                details.append("Imię: ").append(getStringValue(dataSnapshot.child("imie"))).append("\n");
                details.append("Nazwisko: ").append(getStringValue(dataSnapshot.child("nazwisko"))).append("\n");
                details.append("Email: ").append(getStringValue(dataSnapshot.child("email"))).append("\n");
                details.append("Data urodzenia: ").append(getStringValue(dataSnapshot.child("dataUrodzenia"))).append("\n");
                details.append("Leki:\n");

                for (DataSnapshot medicineSnapshot : dataSnapshot.child("Medicines").getChildren()) {
                    String medicine = getStringValue(medicineSnapshot.child("name")) + " " +
                            getStringValue(medicineSnapshot.child("strength")) + ", " +
                            getStringValue(medicineSnapshot.child("dose")) + "\n";
                    details.append(medicine);
                }

                details.append("\nCiśnienie:\n");
                for (DataSnapshot pressureSnapshot : dataSnapshot.child("Pressures").getChildren()) {
                    String pressure = "Data: " + getStringValue(pressureSnapshot.child("date")) + ", " +
                            "Skurczowe: " + getStringValue(pressureSnapshot.child("systolic")) + ", " +
                            "Rozkurczowe: " + getStringValue(pressureSnapshot.child("diastolic")) + "\n";
                    details.append(pressure);
                }

                tvPatientDetails.setText(details.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                tvPatientDetails.setText("Błąd podczas ładowania danych pacjenta");
            }
        });
    }

    private void setupFileAdapter() {
        DatabaseReference filesRef = databaseReference.child("Files");
        FirebaseRecyclerOptions<FileData> options =
                new FirebaseRecyclerOptions.Builder<FileData>()
                        .setQuery(filesRef, FileData.class)
                        .build();

        fileAdapter = new FirebaseRecyclerAdapter<FileData, FileDataViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FileDataViewHolder holder, int position, @NonNull FileData model) {
                holder.bind(model);
                holder.itemView.setOnClickListener(v -> openPDFFile(model.getUrl()));
            }

            @NonNull
            @Override
            public FileDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
                return new FileDataViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                notifyDataSetChanged(); // Zapewnia, że adapter jest zsynchronizowany z danymi
            }
        };

        recyclerViewFiles.setAdapter(fileAdapter);
    }

    private void openPDFFile(String fileUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(fileUrl), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Nie znaleziono aplikacji do otwarcia plików PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private String getStringValue(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        if (value instanceof Long) {
            return String.valueOf(value);
        } else if (value instanceof String) {
            return (String) value;
        }
        return "N/A";
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (fileAdapter != null) {
            fileAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fileAdapter != null) {
            fileAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatientDetails();
    }

    public static class FileDataViewHolder extends RecyclerView.ViewHolder {
        private TextView fileTypeTextView;
        private TextView fileNameTextView;
        private TextView noteTextView;

        public FileDataViewHolder(@NonNull View itemView) {
            super(itemView);
            fileTypeTextView = itemView.findViewById(R.id.fileTypeTextView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
        }

        public void bind(FileData fileData) {
            fileTypeTextView.setText("Typ pliku: " + fileData.getFileType());
            fileNameTextView.setText("Nazwa pliku: " + fileData.getFileName());
            noteTextView.setText("Notatka: " + fileData.getNote());
        }
    }
}
