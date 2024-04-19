package com.party.kardiol0g;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.party.kardiol0g.services.FallDetectionService;

public class MoreFragment extends Fragment {

    CheckBox checkBoxFallSensor;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 2;

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String FALL_SENSOR_ENABLED = "fallSensorEnabled";
    private boolean fallSensorEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        checkBoxFallSensor = view.findViewById(R.id.checkBoxFallSensor);

        // Odczytaj stan checkboxa z SharedPreferences
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        fallSensorEnabled = settings.getBoolean(FALL_SENSOR_ENABLED, false);
        checkBoxFallSensor.setChecked(fallSensorEnabled);

        checkBoxFallSensor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Rozpocznij usługę wykrywania upadków
                startFallDetectionService();
            } else {
                // Zatrzymaj usługę wykrywania upadków
                stopFallDetectionService();
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        // Zapisz aktualny stan checkboxa w SharedPreferences
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(FALL_SENSOR_ENABLED, checkBoxFallSensor.isChecked());
        editor.apply();
    }

    private void startFallDetectionService() {
        // Sprawdź uprawnienia do wysyłania SMS
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Brak uprawnień, poproś użytkownika o zgodę
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            return;  // Wróć z metody, jeśli nie ma uprawnień do wysyłania SMS
        }

        // Sprawdź uprawnienia do lokalizacji
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Brak uprawnień, poproś użytkownika o zgodę
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            return;  // Wróć z metody, jeśli nie ma uprawnień do lokalizacji
        }

        // Masz uprawnienia, uruchom usługę wykrywania upadków
        Intent serviceIntent = new Intent(getActivity(), FallDetectionService.class);
        getActivity().startService(serviceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            // Sprawdź, czy użytkownik udzielił zgody na wysyłanie SMS
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Użytkownik udzielił zgody, uruchom usługę wykrywania upadków
                startFallDetectionService();
            } else {
                // Użytkownik nie udzielił zgody
            }
        }
    }

    private void stopFallDetectionService() {
        Intent serviceIntent = new Intent(getActivity(), FallDetectionService.class);
        getActivity().stopService(serviceIntent);
    }
}