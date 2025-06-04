package com.example.es5roomdb;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private ListView usersListView;
    private ListView productsListView;
    private AppDatabase db;

    private ArrayAdapter<User> usersAdapter;
    private ArrayAdapter<Product> productsAdapter;
    private List<User> usersList = new ArrayList<>();
    private List<Product> productsList = new ArrayList<>();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
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
        setupListViews();
        loadData();
    }

    private void initializeViews() {
        firstNameEditText = findViewById(R.id.editTextFirstName);
        lastNameEditText = findViewById(R.id.editTextLastName);
        usersListView = findViewById(R.id.listViewUsers);
        productsListView = findViewById(R.id.listViewProducts);
    }

    private void initializeDatabase() {
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name")
                .fallbackToDestructiveMigration()
                .build();
    }

    private void setupListViews() {
        usersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usersList);
        usersListView.setAdapter(usersAdapter);

        productsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productsList);
        productsListView.setAdapter(productsAdapter);

        // Click listener per modificare gli utenti
        usersListView.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = usersList.get(position);
            showEditUserDialog(selectedUser);
        });

        // Click listener per modificare i prodotti
        productsListView.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = productsList.get(position);
            showEditProductDialog(selectedProduct);
        });
    }

    public void onInsertButtonClick(View view) {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Inserisci tutti i campi", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        user.firstName = firstName;
        user.lastName = lastName;

        executor.execute(() -> {
            db.userDao().insertAll(user);
            mainHandler.post(() -> {
                Toast.makeText(this, "Utente inserito", Toast.LENGTH_SHORT).show();
                clearUserFields();
                loadUsers();
            });
        });
    }

    public void onOpenProductActivityClick(View view) {
        Intent intent = new Intent(this, ProductActivity.class);
        startActivity(intent);
    }

    private void loadData() {
        loadUsers();
        loadProducts();
    }

    private void loadUsers() {
        executor.execute(() -> {
            List<User> users = db.userDao().getAll();
            mainHandler.post(() -> {
                usersList.clear();
                if (users != null) {
                    usersList.addAll(users);
                }
                usersAdapter.notifyDataSetChanged();
            });
        });
    }

    private void loadProducts() {
        executor.execute(() -> {
            List<Product> products = db.productDao().getAll();
            mainHandler.post(() -> {
                productsList.clear();
                if (products != null) {
                    productsList.addAll(products);
                }
                productsAdapter.notifyDataSetChanged();
            });
        });
    }

    private void showEditUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifica Utente");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        EditText editFirstName = dialogView.findViewById(R.id.editFirstName);
        EditText editLastName = dialogView.findViewById(R.id.editLastName);

        editFirstName.setText(user.firstName);
        editLastName.setText(user.lastName);

        builder.setView(dialogView);
        builder.setPositiveButton("Salva", (dialog, which) -> {
            user.firstName = editFirstName.getText().toString().trim();
            user.lastName = editLastName.getText().toString().trim();

            if (!user.firstName.isEmpty() && !user.lastName.isEmpty()) {
                executor.execute(() -> {
                    db.userDao().update(user);
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Utente aggiornato", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    });
                });
            } else {
                Toast.makeText(this, "Tutti i campi sono obbligatori", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Elimina", (dialog, which) -> {
            executor.execute(() -> {
                db.userDao().delete(user);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Utente eliminato", Toast.LENGTH_SHORT).show();
                    loadUsers();
                });
            });
        });

        builder.setNeutralButton("Annulla", null);
        builder.show();
    }

    private void showEditProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifica Prodotto");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_product, null);
        EditText editProductName = dialogView.findViewById(R.id.editProductName);
        EditText editPrice = dialogView.findViewById(R.id.editPrice);
        EditText editDescription = dialogView.findViewById(R.id.editDescription);

        editProductName.setText(product.productName);
        editPrice.setText(String.valueOf(product.price));
        editDescription.setText(product.description);

        builder.setView(dialogView);
        builder.setPositiveButton("Salva", (dialog, which) -> {
            try {
                product.productName = editProductName.getText().toString().trim();
                product.price = Double.parseDouble(editPrice.getText().toString().trim());
                product.description = editDescription.getText().toString().trim();

                if (!product.productName.isEmpty()) {
                    executor.execute(() -> {
                        db.productDao().update(product);
                        mainHandler.post(() -> {
                            Toast.makeText(this, "Prodotto aggiornato", Toast.LENGTH_SHORT).show();
                            loadProducts();
                        });
                    });
                } else {
                    Toast.makeText(this, "Nome prodotto obbligatorio", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Prezzo non valido", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Elimina", (dialog, which) -> {
            executor.execute(() -> {
                db.productDao().delete(product);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Prodotto eliminato", Toast.LENGTH_SHORT).show();
                    loadProducts();
                });
            });
        });

        builder.setNeutralButton("Annulla", null);
        builder.show();
    }

    private void clearUserFields() {
        firstNameEditText.setText("");
        lastNameEditText.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Ricarica i dati quando si torna alla main activity
    }
}