package com.party.kardiol0g.files;

import android.app.AlertDialog;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.party.kardiol0g.R;

public class FilesFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<FileData, FileViewHolder> adapter;

    public FilesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setupAdapter();
        recyclerView.setAdapter(adapter);
        return view;
    }

    private void setupAdapter() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference filesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("Files");
            FirebaseRecyclerOptions<FileData> options =
                    new FirebaseRecyclerOptions.Builder<FileData>()
                            .setQuery(filesRef, FileData.class)
                            .build();

            adapter = new FirebaseRecyclerAdapter<FileData, FileViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull FileViewHolder holder, int position, @NonNull FileData model) {
                    holder.bind(model);
                    holder.itemView.setOnClickListener(v -> openOrDeleteFile(model));
                }

                @NonNull
                @Override
                public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
                    return new FileViewHolder(view);
                }
            };
        }
    }

    private void openOrDeleteFile(FileData fileData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Wybierz akcję")
                .setMessage("Co chcesz zrobić z tym plikiem?")
                .setPositiveButton("Otwórz plik", (dialog, which) -> {
                    openPDFFile(fileData);
                    dialog.dismiss();
                })
                .setNegativeButton("Usuń", (dialog, which) -> {
                    deleteFile(fileData);
                    dialog.dismiss();
                })
                .setNeutralButton("Anuluj", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void deleteFile(FileData fileData) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference filesRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(currentUser.getUid())
                    .child("Files");

            filesRef.orderByChild("fileName").equalTo(fileData.getFileName()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("Users")
                    .child(currentUser.getUid())
                    .child("Files")
                    .child(fileData.getFileName());
            storageRef.delete().addOnSuccessListener(aVoid -> {
                // Plik usunięty pomyślnie z Storage
            }).addOnFailureListener(exception -> {

            });
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
            recyclerView.setAdapter(null);
        }
    }


    public static class FileViewHolder extends RecyclerView.ViewHolder {
        private TextView fileTypeTextView;
        private TextView fileNameTextView;
        private TextView noteTextView;

        public FileViewHolder(@NonNull View itemView) {
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
    private void openPDFFile(FileData fileData) {
        // Tworzymy Intencję
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Określamy typ pliku jako PDF
        intent.setDataAndType(Uri.parse(fileData.getUrl()), "application/pdf");
        // Dodajemy flagę do zabezpieczenia przed jakimkolwiek błędem
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            // Sprawdzamy, czy jest aplikacja zdolna do obsługi plików PDF
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Jeśli nie ma aplikacji do obsługi plików PDF, wyświetlamy odpowiedni komunikat
            Toast.makeText(requireContext(), "Nie znaleziono aplikacji do otwarcia plików PDF", Toast.LENGTH_SHORT).show();
        }
    }

}
