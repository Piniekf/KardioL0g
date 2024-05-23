package com.party.kardiol0g.widgets;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CameraPermissionActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // W trakcie tworzenia aktywności sprawdzamy, czy już mamy wymagane uprawnienia
        if (checkPermission()) {
            // Jeśli mamy uprawnienia, możemy zakończyć tę aktywność
            finish();
        } else {
            // Jeśli nie mamy uprawnień, żądamy ich
            requestPermission();
        }
    }

    // Metoda do sprawdzania uprawnień
    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // Metoda do żądania uprawnień
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
    }

    // Obsługa wyniku żądania uprawnień
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Uprawnienia zostały udzielone, więc kończymy tę aktywność
                finish();
            } else {
                // Uprawnienia nie zostały udzielone, możesz tutaj wyświetlić komunikat lub podjąć inne działania
                finish();
            }
        }
    }
}
