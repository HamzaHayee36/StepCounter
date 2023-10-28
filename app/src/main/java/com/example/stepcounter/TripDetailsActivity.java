package com.example.stepcounter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TripDetailsActivity extends AppCompatActivity {

    private TextView tripDetailsTextView;
    private Button viewMapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        tripDetailsTextView = findViewById(R.id.tripDetailsTextView);
        viewMapButton = findViewById(R.id.viewMapButton);

        if (getIntent().hasExtra("trip_data")) {
            String tripData = getIntent().getStringExtra("trip_data");
            tripDetailsTextView.setText(tripData);
        } else {
            tripDetailsTextView.setText("No trip details available.");
        }

        viewMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchGoogleMaps();
            }
        });
    }

    private void launchGoogleMaps() {
        // Extracting latitude and longitude from trip details
        String tripData = tripDetailsTextView.getText().toString();
        String[] details = tripData.split(", ");
        String latitude = details[1].replace("°", "");
        String longitude = details[2].replace("°", "");

        // Constructing the Google Maps URI
        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Handle the error when Google Maps isn't installed
            tripDetailsTextView.setText("Google Maps isn't installed.");
        }
    }
}
