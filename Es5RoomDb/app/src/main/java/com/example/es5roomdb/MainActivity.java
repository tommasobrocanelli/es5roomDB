package com.example.es5roomdb;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    //widget da recuperare
    private EditText uidEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private AppDatabase db;

    //consente di aggiornare gli widget in maniera sicura
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // necessario all'esecuzione in un thread separato delle query
    //così la GUI non resta bloccata in caso di operazioni time-consuming
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeDatabase();
    }

    //recupera gli widget in base all'id
    private void initializeViews() {
        uidEditText = findViewById(R.id.editTextUID);
        firstNameEditText = findViewById(R.id.editTextFirstName);
        lastNameEditText = findViewById(R.id.editTextLastName);
    }

    //inizializza l'istanza del db
    private void initializeDatabase() {
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").build();
    }

    //inserisce un utente nel db prendendo i dati dagli widget
    public void onInsertButtonClick(View view) {
        try {
            int uid = Integer.parseInt(uidEditText.getText().toString().trim());
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Inserisci tutti i campi", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User();
            user.uid = uid;
            user.firstName = firstName;
            user.lastName = lastName;

            executor.execute(() -> {
                db.userDao().insertAll(user);
                mainHandler.post(() -> Toast.makeText(this, "Utente inserito", Toast.LENGTH_SHORT).show());
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "UID non valido", Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Error parsing UID", e);
        }
    }

    // visualizza sulla console con logcat gli utenti presenti nel db
    public void onViewButtonClick(View view) {
        executor.execute(() -> {
            List<User> users = db.userDao().getAll();
            mainHandler.post(() -> {
                if (users != null && !users.isEmpty()) {
                    for (User user : users) {
                        Log.i("Database", user.toString());
                    }
                } else {
                    Log.i("Database", "Nessun utente trovato");
                }
            });
        });
    }
}