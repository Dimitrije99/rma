package com.example.timetracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WorkTrackingActivity extends AppCompatActivity {
    private TextView txtWorkStatus, txtStartTime, txtEndTime, txtUsername, txtUsernameLabel;
    private EditText editDescription;
    private Button btnStart, btnFinish, btnWorkHistory, btnLogout;

    private boolean isWorking = false;
    private long startTime = 0;
    private long endTime = 0;
    private int userId; // User ID from login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_tracking);

        txtUsernameLabel = findViewById(R.id.txtUsernameLabel);
        txtUsername = findViewById(R.id.txtUsername);
        txtWorkStatus = findViewById(R.id.txtWorkStatus);
        txtStartTime = findViewById(R.id.txtStartTime);
        txtEndTime = findViewById(R.id.txtEndTime);
        editDescription = findViewById(R.id.editDescription);
        btnStart = findViewById(R.id.btnStart);
        btnFinish = findViewById(R.id.btnFinish);
        btnWorkHistory = findViewById(R.id.btnWorkHistory);
        btnLogout = findViewById(R.id.btnLogout);

        txtWorkStatus.setText("Not working");
        txtStartTime.setText("Start: -/-");
        txtEndTime.setText("End: -/-");

        // Get userId from Intent
        userId = getIntent().getIntExtra("UserId", -1);
        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        String username = getIntent().getStringExtra("USERNAME");
        if (username != null) {
            txtUsername.setText(username); // Only set the username here
        }
        // Handle system back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showLogoutDialog();
            }
        });

        btnLogout.setOnClickListener(view -> showLogoutDialog());
        btnStart.setOnClickListener(view -> startWork());
        btnFinish.setOnClickListener(view -> finishWork());
        btnWorkHistory.setOnClickListener(view -> openWorkHistory());
    }

    private void startWork() {
        if (!isWorking) {
            isWorking = true;
            startTime = System.currentTimeMillis();
            txtWorkStatus.setText("Working...");
            txtStartTime.setText("Start: " + formatTime(startTime));

            // Disable start button, enable others
            btnStart.setEnabled(false);
            btnFinish.setEnabled(true);

            new Thread(() -> {
                boolean success = WorkTrackingService.startWork(userId);
                runOnUiThread(() -> {
                    if (!success) {
                        Toast.makeText(this, "Failed to start work", Toast.LENGTH_SHORT).show();
                        isWorking = false; // Revert state if API call fails
                        btnStart.setEnabled(true);
                        btnFinish.setEnabled(false);
                        txtWorkStatus.setText("Not Working");
                        txtStartTime.setText("Start: --:--:--");
                    } else {
                        Toast.makeText(this, "Work started", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        }
    }

    private void finishWork() {
        if (isWorking) {
            String description = editDescription.getText().toString().trim();

            if (description.isEmpty()) {
                // Show confirmation dialog
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to finish work without adding a description?")
                        .setPositiveButton("Yes, Finish", (dialog, which) -> {
                            // Proceed with finishing work without description
                            finishWorkInternal(""); // Call a new method to handle the actual finish logic
                            dialog.dismiss();
                        })
                        .setNegativeButton("No, Add Description", (dialog, which) -> {
                            // User wants to add a description, so just dismiss the dialog
                            dialog.dismiss();
                        })
                        .setCancelable(true) // Allow dismissing by tapping outside
                        .show();
            } else {
                // Proceed with finishing work with the provided description
                finishWorkInternal(description); // Call the same new method
            }
        }
    }

    private void finishWorkInternal(String description) {
        isWorking = false;
        final long currentEndTime = System.currentTimeMillis();
        txtWorkStatus.setText("Finished");
        txtEndTime.setText("End: " + formatTime(currentEndTime));

        new Thread(() -> {
            boolean success = WorkTrackingService.endWork(userId, description, currentEndTime);
            runOnUiThread(() -> {
                if (success) {
                    Snackbar.make(findViewById(android.R.id.content), "Work time saved!", Snackbar.LENGTH_SHORT).show();
                    editDescription.setText("");
                    txtStartTime.setText("Start: --:--:--");
                    txtEndTime.setText("End: --:--:--");
                    txtWorkStatus.setText("Not Working");
                    btnStart.setEnabled(true);
                    btnFinish.setEnabled(false);
                } else {
                    Toast.makeText(WorkTrackingActivity.this, "Failed to save work time", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();

        // Reset UI buttons immediately after starting the thread
        btnStart.setEnabled(false); // Should be true initially
        btnFinish.setEnabled(false);
    }

    private void openWorkHistory() {
        Intent intent = new Intent(WorkTrackingActivity.this, WorkHistoryActivity.class);
        intent.putExtra("UserId", userId); // Pass the logged-in user's ID
        startActivity(intent);
    }

    private String formatTime(long time) {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(time));
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle logout (e.g., navigate to login screen or clear session)
                        Intent intent = new Intent(WorkTrackingActivity.this, WelcomeActivity.class);
                        startActivity(intent);
                        finish(); // Close MainActivity
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}