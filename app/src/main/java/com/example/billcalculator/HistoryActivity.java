package com.example.billcalculator;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private List<String> billHistoryList;

    private static final String PREF_KEY_BILL_BREAKDOWN_HISTORY = "bill_breakdown_history";
    private static final String PREF_KEY_HISTORY_COUNT = "history_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize the RecyclerView and the data list
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        billHistoryList = getBillHistoryData();

        // Set up the RecyclerView with a LinearLayoutManager and the HistoryAdapter
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(billHistoryList);
        recyclerViewHistory.setAdapter(historyAdapter);

        // Set the click listener for the "Clear History" button
        Button btnClearHistory = findViewById(R.id.btnClearHistory);
        btnClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clearHistory();
            }
        });
    }

    // Retrieve the bill breakdown history from SharedPreferences
    private List<String> getBillHistoryData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int historyCount = preferences.getInt("history_count", 0);

        // Create a list to store individual bill breakdowns
        List<String> historyList = new ArrayList<>();
        for (int i = 0; i < historyCount; i++) {
            // Generate the key for each bill entry and retrieve it from shared preferences
            String key = "bill_history_" + i;
            String entry = preferences.getString(key, "");
            if (!entry.isEmpty()) {
                historyList.add(entry);
            }
        }
        return historyList;
}

    // Clear the bill breakdown history from SharedPreferences
    private void clearHistory() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        // Clear the RecyclerView data and update the adapter
        billHistoryList.clear();
        historyAdapter.notifyDataSetChanged();
    }
    // Method to save bill breakdown in shared preferences
    private void saveBillHistory(String billBreakdown) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int historyCount = preferences.getInt(PREF_KEY_HISTORY_COUNT, 0);

        // Generate the key for the new bill entry
        String key = PREF_KEY_BILL_BREAKDOWN_HISTORY + "_" + historyCount;

        // Save the new bill breakdown entry in SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, billBreakdown);

        // Increment the history count and save it in SharedPreferences
        editor.putInt(PREF_KEY_HISTORY_COUNT, historyCount + 1);
        editor.apply();
    }
}

