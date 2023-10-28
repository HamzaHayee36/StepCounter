package com.example.stepcounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean noConnectivity = intent.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY, false
            );

            if (noConnectivity) {
                Toast.makeText(context, "Internet is disconnected. Please turn it on.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Internet is connected.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
