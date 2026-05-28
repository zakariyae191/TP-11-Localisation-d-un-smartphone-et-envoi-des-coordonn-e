package com.example.localisation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    // Remplacer VOTRE_IP par l'adresse IP reelle du PC serveur, par exemple :
    // http://192.168.1.10/localisation/createPosition.php
    private static final String SERVER_URL = "http://VOTRE_IP/localisation/createPosition.php";

    private TextView textPosition;
    private LocationManager locationManager;
    private RequestQueue requestQueue;
    private String androidId;
    private String currentDatePosition = "";
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textPosition = findViewById(R.id.textPosition);
        Button btnGetSend = findViewById(R.id.btnGetSend);
        Button btnTestServer = findViewById(R.id.btnTestServer);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        requestQueue = Volley.newRequestQueue(this);

        // ANDROID_ID est plus adapte que l'IMEI sur Android moderne.
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        btnGetSend.setOnClickListener(view -> startLocationProcess());
        btnTestServer.setOnClickListener(view -> testServer());

        updateDisplay(null, "en attente");
    }

    private void startLocationProcess() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }

        if (locationManager == null) {
            Toast.makeText(this, "Service de localisation indisponible", Toast.LENGTH_LONG).show();
            return;
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS désactivé. Activez la localisation.", Toast.LENGTH_LONG).show();
            updateServerState("GPS désactivé");
            return;
        }

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    60000,
                    150,
                    this
            );

            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                onLocationChanged(lastLocation);
            } else {
                Toast.makeText(this, "Recherche de la position GPS...", Toast.LENGTH_SHORT).show();
                updateServerState("recherche de position");
            }
        } catch (SecurityException exception) {
            Toast.makeText(this, "Permission localisation manquante", Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (hasLocationPermission()) {
                Toast.makeText(this, "Permission accordée", Toast.LENGTH_SHORT).show();
                startLocationProcess();
            } else {
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_LONG).show();
                updateServerState("permission refusée");
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location == null) {
            Toast.makeText(this, "Position introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        currentDatePosition = formatDate();
        currentLocation = location;

        Toast.makeText(this, "Position détectée", Toast.LENGTH_SHORT).show();
        updateDisplay(location, "envoi en cours");
        addPosition(latitude, longitude);
    }

    private void addPosition(double lat, double lon) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                SERVER_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.optBoolean("success", false);
                        String message = jsonObject.optString("message", "Réponse serveur reçue");

                        updateServerState(message);
                        Toast.makeText(this, success ? "Envoi réussi" : message, Toast.LENGTH_LONG).show();
                    } catch (Exception exception) {
                        updateServerState("réponse serveur invalide");
                        Toast.makeText(this, "Réponse serveur invalide", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    String message = "Erreur lors de l'envoi";
                    if (error.getMessage() != null) {
                        message += " : " + error.getMessage();
                    }

                    updateServerState(message);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date_position", currentDatePosition);
                // Le nom "imei" est conserve pour respecter la table du TP.
                params.put("imei", androidId);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void testServer() {
        currentDatePosition = formatDate();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                SERVER_URL,
                response -> {
                    updateServerState("test serveur réussi : " + response);
                    Toast.makeText(this, "Test serveur terminé", Toast.LENGTH_LONG).show();
                },
                error -> {
                    updateServerState("test serveur échoué");
                    Toast.makeText(this, "Erreur réseau Android : vérifiez IP, Apache et pare-feu", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("latitude", "31.64467319");
                params.put("longitude", "-8.01915503");
                params.put("date_position", currentDatePosition);
                params.put("imei", androidId);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void updateDisplay(Location location, String serverState) {
        String latitude = "-";
        String longitude = "-";
        String altitude = "-";
        String accuracy = "-";
        String date = currentDatePosition.isEmpty() ? "-" : currentDatePosition;

        if (location != null) {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            altitude = location.hasAltitude() ? location.getAltitude() + " m" : "non disponible";
            accuracy = location.hasAccuracy() ? location.getAccuracy() + " m" : "non disponible";
        }

        textPosition.setText(
                "Latitude : " + latitude + "\n" +
                        "Longitude : " + longitude + "\n" +
                        "Altitude : " + altitude + "\n" +
                        "Précision : " + accuracy + "\n" +
                        "Date : " + date + "\n" +
                        "Identifiant appareil : " + androidId + "\n" +
                        "État serveur : " + serverState
        );
    }

    private void updateServerState(String serverState) {
        updateDisplay(currentLocation, serverState);
    }

    private String formatDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(new Date());
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Toast.makeText(this, "GPS activé", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, "GPS désactivé", Toast.LENGTH_LONG).show();
        updateServerState("GPS désactivé");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Methode conservee pour rester proche des anciens exemples de TP.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}
