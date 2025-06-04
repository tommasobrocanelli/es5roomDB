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

public class ProductActivity extends AppCompatActivity {

    private EditText productNameEditText;
    private EditText priceEditText;
    private EditText descriptionEditText;
    private AppDatabase db;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeDatabase();
    }

    private void initializeViews() {
        productNameEditText = findViewById(R.id.editTextProductName);
        priceEditText = findViewById(R.id.editTextPrice);
        descriptionEditText = findViewById(R.id.editTextDescription);
    }

    private void initializeDatabase() {
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name")
                .fallbackToDestructiveMigration() // Per gestire il cambio di versione
                .build();
    }

    public void onInsertProductButtonClick(View view) {
        try {
            String productName = productNameEditText.getText().toString().trim();
            String priceText = priceEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            if (productName.isEmpty() || priceText.isEmpty()) {
                Toast.makeText(this, "Inserisci nome prodotto e prezzo", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceText);

            Product product = new Product();
            product.productName = productName;
            product.price = price;
            product.description = description;

            executor.execute(() -> {
                db.productDao().insertAll(product);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Prodotto inserito", Toast.LENGTH_SHORT).show();
                    clearFields();
                });
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Prezzo non valido", Toast.LENGTH_SHORT).show();
            Log.e("ProductActivity", "Error parsing price", e);
        }
    }

    public void onViewProductsButtonClick(View view) {
        executor.execute(() -> {
            List<Product> products = db.productDao().getAll();
            mainHandler.post(() -> {
                if (products != null && !products.isEmpty()) {
                    for (Product product : products) {
                        Log.i("Database", "Product: " + product.toString());
                    }
                    Toast.makeText(this, "Controlla il log per vedere i prodotti", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("Database", "Nessun prodotto trovato");
                    Toast.makeText(this, "Nessun prodotto nel database", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void clearFields() {
        productNameEditText.setText("");
        priceEditText.setText("");
        descriptionEditText.setText("");
    }

    public void onBackButtonClick(View view) {
        finish();
    }
}