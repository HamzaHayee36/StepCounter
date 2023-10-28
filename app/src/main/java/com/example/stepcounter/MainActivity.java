package com.example.stepcounter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String NOTIFICATION_CHANNEL_ID = "StepCounterChannelID";
    public static final String TRIPS_KEY = "trips";
    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private TextView stepCountTextView;
    private int stepsTaken = 0;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private SharedPreferences sharedPreferences;
    static final String SHARED_PREFERENCES_NAME = "TripsData";

    private static final String DAILY_GOAL_PREF = "DailyGoalPreferences";
    private static final String DAILY_GOAL_KEY = "DailyGoal";
    private static final String DONT_SHOW_DIALOG_KEY = "DontShowDialog";
    private int userStepGoal = 10; // default is 10
    private ProgressBar stepProgressBar;
    private TextView percentageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showSetGoalDialogIfNeeded();
        promptUserToEnableNotifications();
        createNotificationChannel();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        if (stepDetectorSensor == null) {
            Toast.makeText(this, "Step Detector Sensor not available!", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        stepCountTextView = findViewById(R.id.stepCountTextView);
        stepCountTextView.setText("Steps: 0");

        stepProgressBar = findViewById(R.id.stepProgressBar);
        percentageTextView = findViewById(R.id.percentageTextView);
        Button startButton = findViewById(R.id.startButton);
        Button endButton = findViewById(R.id.endButton);
        FloatingActionButton stepsFab = findViewById(R.id.stepsFab);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
                sendNotification("Counting Started", "The step counting has started.");
            }
        });

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                sendNotification("Counting Stopped", "The step counting has stopped.");
            }
        });

        stepsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTripsList();
            }
        });

        requestRequiredPermissions();
    }

    private final SensorEventListener stepDetectorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                stepsTaken += 1;
                stepCountTextView.setText("Steps: " + stepsTaken);
                // Update the progress bar
                stepProgressBar.setProgress(stepsTaken);
                // Calculate the percentage of the goal achieved
                int percentage = (int) ((double) stepsTaken / userStepGoal * 100);
                percentageTextView.setText(percentage + "%");

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // No need for this in our scenario
        }
    };

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                // Save location details when a new location is received
                saveTripDetails(location);
            }
        }
    };

    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            stepsTaken = 0;
            stepCountTextView.setText("Steps: 0");
            sensorManager.registerListener(stepDetectorListener, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
            fusedLocationProviderClient.requestLocationUpdates(createLocationRequest(), locationCallback, Looper.getMainLooper());
        } else {
            requestRequiredPermissions();
        }
    }

    private void stopRecording() {
        sensorManager.unregisterListener(stepDetectorListener);
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        stepCountTextView.setText("Steps: 0"); // Reset the step count displayed on the screen
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void navigateToTripsList() {
        // Start the TripListActivity
        Intent intent = new Intent(MainActivity.this, TripListActivity.class);
        startActivity(intent);
    }


    // Save trip details to SharedPreferences
    private void saveTripDetails(Location location) {
        Set<String> existingTrips = sharedPreferences.getStringSet(TRIPS_KEY, new HashSet<>());
        Set<String> updatedTrips = new HashSet<>(existingTrips);

        String tripDetails = System.currentTimeMillis() + "," + location.getLatitude() + "," + location.getLongitude();
        updatedTrips.add(tripDetails);

        sharedPreferences.edit().putStringSet(TRIPS_KEY, updatedTrips).apply();
    }

    private void requestRequiredPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }
        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, you can start recording if needed
            } else {
                Toast.makeText(this, "Permissions denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)  // Replace with your app's icon
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(0, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "StepCounterChannel";
            String description = "Channel for Step Counter notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void promptUserToEnableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (!notificationManager.areNotificationsEnabled()) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
                Toast.makeText(this, "Please enable notifications for this app", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showSetGoalDialogIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(DAILY_GOAL_PREF, MODE_PRIVATE);
        boolean dontShowDialog = prefs.getBoolean(DONT_SHOW_DIALOG_KEY, false);

        if (!dontShowDialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Set Your Daily Step Goal");
            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("Set Goal", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    userStepGoal = Integer.parseInt(input.getText().toString());
                    stepProgressBar.setMax(userStepGoal);
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.setNeutralButton("Don't Ask Again", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    prefs.edit().putBoolean(DONT_SHOW_DIALOG_KEY, true).apply();
                }
            });
            builder.show();
        }
    }

}
