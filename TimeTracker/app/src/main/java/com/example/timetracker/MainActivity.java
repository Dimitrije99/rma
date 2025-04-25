package com.example.timetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private EditText editUsername, editPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(view -> attemptLogin());
    }

    private void attemptLogin() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Run login in a separate thread to prevent UI blocking
        new Thread(() -> {
            Integer userId = LoginService.loginUser(username, password);

            runOnUiThread(() -> {
                if (userId != null) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                    // Pass UserId to WorkTrackingActivity
                    Intent intent = new Intent(MainActivity.this, WorkTrackingActivity.class);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("UserId", userId); // Pass user ID
                    startActivity(intent);
                    finish(); // Close Login screen
                } else {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
