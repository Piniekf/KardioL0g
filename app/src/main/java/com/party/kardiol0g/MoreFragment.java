package com.party.kardiol0g;
import android.Manifest;
import android.content.Intent;
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        checkBoxFallSensor = view.findViewById(R.id.checkBoxFallSensor);
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

    private void startFallDetectionService() {
        // Sprawdź uprawnienia do wysyłania SMS
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Brak uprawnień, poproś użytkownika o zgodę
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            // Masz uprawnienia, uruchom usługę wykrywania upadków
            Intent serviceIntent = new Intent(getActivity(), FallDetectionService.class);
            getActivity().startService(serviceIntent);
        }
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
                // Użytkownik nie udzielił zgody, możesz obsłużyć to odpowiednio (np. wyświetlić komunikat)
            }
        }
    }

    private void stopFallDetectionService() {
        Intent serviceIntent = new Intent(getActivity(), FallDetectionService.class);
        getActivity().stopService(serviceIntent);
    }
}
