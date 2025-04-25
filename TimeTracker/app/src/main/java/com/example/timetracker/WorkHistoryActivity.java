package com.example.timetracker;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class WorkHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerViewHistory;
    private WorkHistoryRecyclerAdapter adapter; // Use the new adapter
    private ArrayList<WorkHistoryItem> historyList = new ArrayList<>();
    private Toolbar toolbarWorkHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_history);

        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkHistoryRecyclerAdapter(historyList); // Initialize the new adapter
        recyclerViewHistory.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewHistory.getContext(),
                LinearLayoutManager.VERTICAL);
        recyclerViewHistory.addItemDecoration(dividerItemDecoration);

        toolbarWorkHistory = findViewById(R.id.toolbarWorkHistory);
        setSupportActionBar(toolbarWorkHistory);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarWorkHistory.setNavigationOnClickListener(view -> {
            // Handle the back button click
            finish(); // This will navigate back to the previous activity
        });

        int userId = getIntent().getIntExtra("UserId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchWorkHistory(userId);
    }

    private void fetchWorkHistory(int userId) {
        new Thread(() -> {
            String response = WorkHistoryService.getWorkHistory(userId);

            runOnUiThread(() -> {
                try {
                    historyList.clear();
                    JSONArray jsonArray = new JSONArray(response);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        WorkHistoryItem item = new WorkHistoryItem(
                                obj.getString("Date"),
                                obj.getString("StartTime"),
                                obj.getString("EndTime"),
                                obj.getString("Description")
                        );
                        historyList.add(item);
                    }

                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to load work history", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}