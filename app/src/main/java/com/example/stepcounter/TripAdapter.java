package com.example.stepcounter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private final List<String> tripList;

    public TripAdapter(List<String> tripList) {
        this.tripList = tripList;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_item, parent, false);
        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        String tripData = tripList.get(position);
        holder.tripNumber.setText((position + 1) + ".");  // Display the trip number
        holder.tripTextView.setText(tripData);            // Display the trip data
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {

        final TextView tripNumber;  // Added this line for the numbering
        final TextView tripTextView;

        TripViewHolder(View itemView) {
            super(itemView);
            tripNumber = itemView.findViewById(R.id.tripNumber);  // Added this line for the numbering
            tripTextView = itemView.findViewById(R.id.tripTextView);
        }
    }
}
