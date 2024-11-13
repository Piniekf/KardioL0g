package com.party.kardiol0g;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class HomeFragment_Doctor extends Fragment {

    private TextView tvDashboardTitle;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_doctor, container, false);

        tvDashboardTitle = view.findViewById(R.id.tvDashboardTitle);
        Button scanButton = new Button(getContext());
        scanButton.setText("Zeskanuj QR Kod");
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQRScanner();
            }
        });

        RelativeLayout layout = view.findViewById(R.id.relativeLayout);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(scanButton, params);

        return view;
    }

    private void startQRScanner() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Zeskanuj QR kod");
        integrator.setCameraId(0); // Użyj tylnej kamery
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scannedUid = result.getContents();
                checkIfDoctor(scannedUid);
            } else {
                tvDashboardTitle.setText("Brak wyniku skanowania");
            }
        }
    }

    private void checkIfDoctor(String scannedUid) {
        databaseReference.child(scannedUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String lekarzProwadzacyUid = dataSnapshot.child("lekarzProwadzacyUid").getValue(String.class);
                    if (currentUser.getUid().equals(lekarzProwadzacyUid)) {
                        Intent intent = new Intent(getActivity(), PatientDetailsActivity.class);
                        intent.putExtra("patientUid", scannedUid);
                        startActivity(intent);
                    } else {
                        tvDashboardTitle.setText("Nie jesteś lekarzem prowadzącym");
                    }
                } else {
                    tvDashboardTitle.setText("Nie znaleziono pacjenta");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                tvDashboardTitle.setText("Błąd podczas sprawdzania pacjenta");
            }
        });
    }
}
