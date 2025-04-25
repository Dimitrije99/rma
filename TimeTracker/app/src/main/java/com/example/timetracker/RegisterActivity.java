package com.example.timetracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText editNewUsername, editNewPassword;
    private Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editNewUsername = findViewById(R.id.editNewUsername);
        editNewPassword = findViewById(R.id.editNewPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnCreateAccount.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String username = editNewUsername.getText().toString().trim();
        String password = editNewPassword.getText().toString().trim();

        // Debugging log
        Log.d("RegisterActivity", "Username: " + username);
        Log.d("RegisterActivity", "Password: " + password);

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            boolean success = LoginService.registerUser(username, password);

            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Registration failed. Try another username.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

}
