package com.example.timetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WorkHistoryRecyclerAdapter extends RecyclerView.Adapter<WorkHistoryRecyclerAdapter.WorkHistoryViewHolder> {

    private final ArrayList<WorkHistoryItem> historyList;

    public WorkHistoryRecyclerAdapter(ArrayList<WorkHistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public WorkHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_work_history, parent, false); // Use the new item layout
        return new WorkHistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkHistoryViewHolder holder, int position) {
        WorkHistoryItem currentItem = historyList.get(position);
        String formattedDate = currentItem.getDate().replace("-", ".");
        holder.txtDate.setText(formattedDate);
        holder.txtStartTime.setText(currentItem.getStartTime());
        holder.txtEndTime.setText(currentItem.getEndTime());
        holder.txtDescription.setText(currentItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class WorkHistoryViewHolder extends RecyclerView.ViewHolder {
        public final TextView txtDate;
        public final TextView txtStartTime;
        public final TextView txtEndTime;
        public final TextView txtDescription;

        public WorkHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtStartTime = itemView.findViewById(R.id.txtStartTime);
            txtEndTime = itemView.findViewById(R.id.txtEndTime);
            txtDescription = itemView.findViewById(R.id.txtDescription);
        }
    }
}