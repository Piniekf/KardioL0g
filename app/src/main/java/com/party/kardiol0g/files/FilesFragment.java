package com.party.kardiol0g.files;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
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
        if (currentUser == null) {
            // Obsługa sytuacji, gdy użytkownik nie jest zalogowany
            return;
        }

        FirebaseRecyclerOptions<FileData> options =
                new FirebaseRecyclerOptions.Builder<FileData>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("Files"), FileData.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<FileData, FileViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FileViewHolder holder, int position, @NonNull FileData model) {
                holder.bind(model);
            }

            @NonNull
            @Override
            public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
                return new FileViewHolder(view);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    // ViewHolder dla elementu listy plików
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
            // Ustawiamy typ pliku, nazwę pliku i notatkę w widoku
            fileTypeTextView.setText("Typ pliku: " + fileData.getFileType());
            fileNameTextView.setText("Nazwa: " +fileData.getFileName());
            noteTextView.setText("Notatka: " +fileData.getNote());
        }
    }

}
