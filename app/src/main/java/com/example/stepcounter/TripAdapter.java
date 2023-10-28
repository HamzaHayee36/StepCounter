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
    private final OnItemClickListener listener;  // Step 1: Define the listener

    // Step 2: Modify the constructor to accept the listener
    public TripAdapter(List<String> tripList, OnItemClickListener listener) {
        this.tripList = tripList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_item, parent, false);
        return new TripViewHolder(itemView, listener);  // Pass the listener to the ViewHolder
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

    // Step 3: Set the listener in TripViewHolder
    static class TripViewHolder extends RecyclerView.ViewHolder {

        final TextView tripNumber;
        final TextView tripTextView;

        TripViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            tripNumber = itemView.findViewById(R.id.tripNumber);
            tripTextView = itemView.findViewById(R.id.tripTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position);
                }
            });
        }
    }

    // The interface for the click listener
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
