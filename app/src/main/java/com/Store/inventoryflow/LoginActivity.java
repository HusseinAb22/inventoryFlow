package com.Store.inventoryflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.ktx.Firebase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth dbAuth;
    private Firebase db;
    private EditText email, password;
    private Button Login;
    private TextView Signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view first
        setContentView(R.layout.activity_login);

        // Now initialize the views
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        Login = findViewById(R.id.loginButton);
        Signup = findViewById(R.id.signup);

        dbAuth = FirebaseAuth.getInstance();

        // Enable edge-to-edge mode if necessary
        EdgeToEdge.enable(this);

        // Set up window insets listener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set click listener for the Login button
        Login.setOnClickListener(v -> {
            Toast.makeText(this, "It works", Toast.LENGTH_SHORT).show();
        });

        // Set click listener for the Signup button
        Signup.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(intent);
        });

    }
}
