package com.example.stepcounter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TripListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<String> tripList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadTrips();

        if (tripList.isEmpty()) {
            Toast.makeText(this, "No trips recorded", Toast.LENGTH_SHORT).show();
            return;
        }

        TripAdapter adapter = new TripAdapter(tripList, position -> {
            String tripData = tripList.get(position);
            Intent intent = new Intent(TripListActivity.this, TripDetailsActivity.class);
            intent.putExtra("trip_data", tripData);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadTrips() {
        // Accessing the TRIPS_KEY from MainActivity
        String key = MainActivity.TRIPS_KEY;

        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        Set<String> trips = sharedPreferences.getStringSet(key, new HashSet<>());
        int counter = 1;
        if (trips != null) {
            for (String trip : trips) {
                String[] details = trip.split(",");
                String formattedTime = formatDate(Long.parseLong(details[0]));
                String formattedLatitude = formatCoordinate(Double.parseDouble(details[1]));
                String formattedLongitude = formatCoordinate(Double.parseDouble(details[2]));
                String formattedTrip = formattedTime + ", " + formattedLatitude + ", " + formattedLongitude;
                tripList.add(formattedTrip);
                counter++;
            }
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        return sdf.format(new Date(timestamp));
    }

    private String formatCoordinate(double coordinate) {
        return String.format("%.2f°", coordinate);
    }
}
