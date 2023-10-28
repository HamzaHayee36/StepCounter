package com.example.stepcounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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

    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isInternetAvailable()) {
                Toast.makeText(TripListActivity.this, "Internet is disconnected.", Toast.LENGTH_SHORT).show();
            }
        }
    };

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
            if (isInternetAvailable()) {
                String tripData = tripList.get(position);
                Intent intent = new Intent(TripListActivity.this, TripDetailsActivity.class);
                intent.putExtra("trip_data", tripData);
                startActivity(intent);
            } else {
                Toast.makeText(TripListActivity.this, "Internet is disconnected. Please turn it on.", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadTrips() {
        String key = MainActivity.TRIPS_KEY;
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        Set<String> trips = sharedPreferences.getStringSet(key, new HashSet<>());
        if (trips != null) {
            for (String trip : trips) {
                String[] details = trip.split(",");
                String formattedTime = formatDate(Long.parseLong(details[0]));
                String formattedLatitude = formatCoordinate(Double.parseDouble(details[1]));
                String formattedLongitude = formatCoordinate(Double.parseDouble(details[2]));
                String formattedTrip = formattedTime + ", " + formattedLatitude + ", " + formattedLongitude;
                tripList.add(formattedTrip);
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

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectivityReceiver);
    }
}
