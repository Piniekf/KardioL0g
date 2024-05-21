package com.party.kardiol0g.files;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.party.kardiol0g.R;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<FileData> fileDataList;

    public FileAdapter(List<FileData> fileDataList) {
        this.fileDataList = fileDataList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileData fileData = fileDataList.get(position);
        holder.bind(fileData);
    }

    @Override
    public int getItemCount() {
        return fileDataList.size();
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
            fileNameTextView.setText("Nazwa pliku: " + fileData.getFileName());
            noteTextView.setText("Notatka: " + fileData.getNote());
        }
    }
}
