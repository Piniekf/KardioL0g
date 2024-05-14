package com.party.kardiol0g.services;

import android.Manifest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.party.kardiol0g.NotificationHelper;


public class FallDetectionService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isFallDetected = false;
    private static final float FALL_THRESHOLD = 8.0f; // Prog detekcji upadku
    private static final float ALPHA = 0.8f; // Współczynnik filtra dolnoprzepustowego
    private static final int TIME_THRESHOLD = 500; // Minimalny czas trwania przewrócenia (w milisekundach)
    private static final int ANGLE_THRESHOLD = 60; // Prog zmiany kierunku ruchu (w stopniach)
    private static final float SPEED_THRESHOLD = 1.0f; // Prog prędkości ruchu (w m/s)
    private long lastFallTime = 0;
    private float lastX, lastY, lastZ;
    private float lastSpeed = 0;
    private String contactPhoneNumber;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    contactPhoneNumber = dataSnapshot.child("numerTelefonuKontaktowej").getValue(String.class);
                    Log.d("FallDetectionService", "Pobrano numer telefonu kontaktowego: " + contactPhoneNumber);
                } else {
                    Log.w("FallDetectionService", "Numer telefonu kontaktowego nie został znaleziony w Firebase");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FallDetectionService", "Błąd podczas pobierania numeru telefonu z Firebase: " + databaseError.getMessage());
            }
        });

        // Pokaż powiadomienie
        NotificationHelper.createNotificationChannel(this, "fall_detection_channel", "Fall Detection Service");
        NotificationHelper.showNotification(this, "fall_detection_channel", 1, "Czujnik upadku", "Czujnik wykrycia upadku jest włączony.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);

        // Ukryj powiadomienie
        NotificationHelper.cancelNotification(this, 1);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Filtracja danych z akcelerometru
        x = lowPass(x, lastX);
        y = lowPass(y, lastY);
        z = lowPass(z, lastZ);

        // Obliczenie przyspieszenia
        double acceleration = Math.sqrt(x * x + y * y + z * z);

        // Sprawdzenie warunków wykrycia przewrócenia się
        if (isFallDetected(x, y, z, acceleration)) {
            // Przewrócenie się zostało wykryte
            long currentTime = System.currentTimeMillis();
            if (!isFallDetected || (currentTime - lastFallTime) > TIME_THRESHOLD) {
                isFallDetected = true;
                lastFallTime = currentTime;
                sendSMS();
            }
        } else {
            // Brak przewrócenie się
            isFallDetected = false;
        }

        // Zapisanie poprzednich danych
        lastX = x;
        lastY = y;
        lastZ = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Metoda filtrująca dolnoprzepustowa
    private float lowPass(float current, float last) {
        return last + ALPHA * (current - last);
    }

    // Metoda sprawdzająca warunki wykrycia przewrócenia się
    private boolean isFallDetected(float x, float y, float z, double acceleration) {
        // Warunek wykrycia upadku na podstawie przyspieszenia
        if (acceleration < FALL_THRESHOLD) {
            // Sprawdzenie zmiany kierunku ruchu
            float angle = calculateAngle(x, y, z);
            // Sprawdzenie zmiany prędkości ruchu
            float speed = calculateSpeed(x, y, z);
            return angle > ANGLE_THRESHOLD && speed > SPEED_THRESHOLD;
        }
        return false;
    }

    // Metoda obliczająca kąt zmiany kierunku ruchu
    private float calculateAngle(float x, float y, float z) {
        float angle = (float) Math.toDegrees(Math.atan2(x, Math.sqrt(y * y + z * z)));
        return Math.abs(angle);
    }

    // Metoda obliczająca prędkość ruchu
    private float calculateSpeed(float x, float y, float z) {
        // Obliczenie zmiany prędkości na podstawie danych z akcelerometru
        float speed = Math.abs((x + y + z) - (lastX + lastY + lastZ));
        lastSpeed = speed;
        return speed;
    }

    // Metoda wysyłająca SMS
    private void sendSMS() {
        // Sprawdź, czy numer telefonu został pobrany
        if (contactPhoneNumber != null) {
            // Pobierz aktualną lokalizację
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Utwórz adres URL do mapy
                        String mapUrl = "https://www.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
                        // Wyślij SMS
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(contactPhoneNumber, null, "Upadek wykryty! Lokalizacja: " + mapUrl, null, null);
                        Log.d("FallDetectionService", "SMS wysłany na numer: " + contactPhoneNumber + " z linkiem do mapy: " + mapUrl);
                    } else {
                        Log.w("FallDetectionService", "Nie udało się pobrać aktualnej lokalizacji");
                        // Wyślij SMS bez lokalizacji, jeśli nie udało się jej pobrać
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(contactPhoneNumber, null, "Upadek wykryty!", null, null);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            }, null);
        } else {
            Log.w("FallDetectionService", "Numer telefonu kontaktowego nie jest dostępny");
        }
    }
}
