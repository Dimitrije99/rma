package com.example.timetracker;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

public class WorkHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerViewHistory;
    private WorkHistoryRecyclerAdapter adapter;
    private ArrayList<WorkHistoryItem> historyList = new ArrayList<>();
    private Toolbar toolbarWorkHistory;
    private int userId;

    private final OkHttpClient client = new OkHttpClient(); // Initialize OkHttpClient here
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8"); // Initialize MediaType here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_history);

        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkHistoryRecyclerAdapter(historyList);
        recyclerViewHistory.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewHistory.getContext(),
                LinearLayoutManager.VERTICAL);
        recyclerViewHistory.addItemDecoration(dividerItemDecoration);

        toolbarWorkHistory = findViewById(R.id.toolbarWorkHistory);
        setSupportActionBar(toolbarWorkHistory);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarWorkHistory.setNavigationOnClickListener(view -> finish());

        userId = getIntent().getIntExtra("UserId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchWorkHistory(userId);

        // Implement swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final WorkHistoryItem itemToDelete = historyList.get(position);

                historyList.remove(position);
                adapter.notifyItemRemoved(position);

                deleteWorkHistoryEntry(itemToDelete.getId(), position);

                Snackbar.make(recyclerViewHistory, "Entry deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            historyList.add(position, itemToDelete);
                            adapter.notifyItemInserted(position);
                        })
                        .show();
            }
        }).attachToRecyclerView(recyclerViewHistory);
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
                                obj.getInt("Id"), // Ensure this matches your Flask response key
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

    private void deleteWorkHistoryEntry(int idToDelete, int adapterPosition) {
        new Thread(() -> {
            try {
                String url = "http://10.0.2.2:5000/delete_work_history";
                RequestBody body = RequestBody.create(JSON, "{\"id\": " + idToDelete + "}");
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                if (jsonResponse.has("message")) {
                                    Toast.makeText(WorkHistoryActivity.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                } else if (jsonResponse.has("error")) {
                                    Toast.makeText(WorkHistoryActivity.this, "Delete failed: " + jsonResponse.getString("error"), Toast.LENGTH_SHORT).show();
                                    // Undo local deletion
                                    historyList.add(adapterPosition, historyList.get(adapterPosition));
                                    adapter.notifyItemInserted(adapterPosition);
                                }
                            } catch (Exception e) {
                                Toast.makeText(WorkHistoryActivity.this, "Error parsing delete response", Toast.LENGTH_SHORT).show();
                                // Undo local deletion
                                historyList.add(adapterPosition, historyList.get(adapterPosition));
                                adapter.notifyItemInserted(adapterPosition);
                            }
                        } else {
                            Toast.makeText(WorkHistoryActivity.this, "Failed to delete entry", Toast.LENGTH_SHORT).show();
                            // Undo local deletion
                            historyList.add(adapterPosition, historyList.get(adapterPosition));
                            adapter.notifyItemInserted(adapterPosition);
                        }
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> Toast.makeText(WorkHistoryActivity.this, "Network error during delete", Toast.LENGTH_SHORT).show());
                    // Undo local deletion
                    historyList.add(adapterPosition, historyList.get(adapterPosition));
                    adapter.notifyItemInserted(adapterPosition);
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(WorkHistoryActivity.this, "Error creating delete request", Toast.LENGTH_SHORT).show());
                // Undo local deletion
                historyList.add(adapterPosition, historyList.get(adapterPosition));
                adapter.notifyItemInserted(adapterPosition);
            }
        }).start();
    }
}