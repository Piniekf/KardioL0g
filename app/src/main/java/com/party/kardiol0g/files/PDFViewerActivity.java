package com.party.kardiol0g.files;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.party.kardiol0g.R;

import java.io.File;
import java.io.IOException;

public class PDFViewerActivity extends AppCompatActivity {

    private ImageView pdfImageView;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor parcelFileDescriptor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        pdfImageView = findViewById(R.id.pdfImageView);

        String filePath = getIntent().getStringExtra("filePath");
        if (filePath != null) {
            displayPDF(filePath);
        } else {
            Toast.makeText(this, "Błąd podczas otwierania pliku PDF", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayPDF(String filePath) {
        try {
            File file = new File(filePath);
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
            currentPage = pdfRenderer.openPage(0);
            Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            pdfImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd podczas otwierania pliku PDF", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (currentPage != null) {
            currentPage.close();
        }
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
        try {
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
        super.onDestroy();
    }
}
