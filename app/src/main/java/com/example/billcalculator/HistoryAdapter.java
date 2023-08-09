package com.example.billcalculator; // Replace this with your actual package name

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<String> historyItems; // Replace String with your actual data type for history items

    public HistoryAdapter(List<String> historyItems) {
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String billEntry = historyItems.get(position);
        String formattedEntry = String.format("%d. %s", position + 1, billEntry);
        holder.textViewHistoryItem.setText(formattedEntry);
    }

    @Override
    public int getItemCount() {

        return historyItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewHistoryItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewHistoryItem = itemView.findViewById(R.id.textViewHistoryDetails);
        }
    }
}
