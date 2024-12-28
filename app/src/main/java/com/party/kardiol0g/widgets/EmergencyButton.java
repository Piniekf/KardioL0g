package com.party.kardiol0g.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.party.kardiol0g.R;

import java.util.Objects;

public class EmergencyButton extends AppWidgetProvider {

    private static final String ACTION_SEND_SMS = "com.party.kardiol0g.widgets.ACTION_SEND_SMS";
    private static String contactPhoneNumber = null;
    private static String userName = null;
    private static boolean isDataLoaded = false;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.emergency_button);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        Intent intent = new Intent(context, EmergencyButton.class);
        intent.setAction(ACTION_SEND_SMS);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        views.setOnClickPendingIntent(R.id.emergency_button, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
        fetchUserData(context, false);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Może być wiele aktywnych widżetów, więc zaktualizuj wszystkie
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_SEND_SMS.equals(intent.getAction())) {
            if (isDataLoaded) {
                sendSMS(context, contactPhoneNumber, userName);
            } else {
                fetchUserData(context, true);
            }
        }
    }

    private static void fetchUserData(Context context, boolean shouldSendSMSAfter) {
        String currentUserUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    contactPhoneNumber = dataSnapshot.child("numerTelefonuKontaktowej").getValue(String.class);
                    String firstName = dataSnapshot.child("imie").getValue(String.class);
                    String lastName = dataSnapshot.child("nazwisko").getValue(String.class);
                    userName = firstName + " " + lastName;
                    isDataLoaded = true;
                    Log.d("EmergencyButton", "Pobrano dane użytkownika: " + userName + ", numer telefonu: " + contactPhoneNumber);
                    if (shouldSendSMSAfter) {
                        sendSMS(context, contactPhoneNumber, userName);
                    }
                } else {
                    Log.w("EmergencyButton", "Dane użytkownika nie zostały znalezione w Firebase");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("EmergencyButton", "Błąd podczas pobierania danych użytkownika z Firebase: " + databaseError.getMessage());
            }
        });
    }

    private static void sendSMS(Context context, String contactPhoneNumber, String userName) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                {
                    String mapUrl = "https://www.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(contactPhoneNumber, null, userName + " potrzebuje pomocy! Lokalizacja: " + mapUrl, null, null);
                    Log.d("EmergencyButton", "SMS wysłany na numer: " + contactPhoneNumber + " z linkiem do mapy: " + mapUrl);
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, android.os.Bundle extras) {}
            @Override
            public void onProviderEnabled(@NonNull String provider) {}
            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        }, null);
    }

    @Override
    public void onEnabled(Context context) {
        // Wprowadź odpowiednią funkcjonalność, gdy zostanie utworzony pierwszy widżet
    }

    @Override
    public void onDisabled(Context context) {
        // Wprowadź odpowiednią funkcjonalność, gdy zostanie wyłączony ostatni widżet
    }
}
