package com.Store.inventoryflow;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddFragment extends Fragment {

    private FirestoreDBHelper dbHelper;
    private EditText ItemName, ItemPrice, ItemCount;
    private Spinner CategorySpinner;
    private Button AddItemButton;
    private ItemCategory selectedCategory; // Store selected category
    private OnStockUpdatedListener stockUpdatedListener;


    public AddFragment(){

    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnStockUpdatedListener) {
            stockUpdatedListener = (OnStockUpdatedListener) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore helper
        dbHelper = new FirestoreDBHelper();

        // Initialize input fields
        ItemName = view.findViewById(R.id.itemName);
        ItemPrice = view.findViewById(R.id.itemPrice);
        ItemCount = view.findViewById(R.id.itemCount);
        CategorySpinner = view.findViewById(R.id.spinnerCategory);
        AddItemButton = view.findViewById(R.id.btn_add_item);

        // Populate Spinner with categories
        setupCategorySpinner();

        // Set button click listener
        AddItemButton.setOnClickListener(v -> addItemToFirestore());
    }

    private void setupCategorySpinner() {
        // Convert enum values to a String array
        String[] categoryNames = new String[ItemCategory.values().length];
        for (int i = 0; i < ItemCategory.values().length; i++) {
            categoryNames[i] = ItemCategory.values()[i].name(); // Enum names as Strings
        }

        // Create and set ArrayAdapter for Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CategorySpinner.setAdapter(adapter);

        // Handle selection
        CategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = ItemCategory.valueOf(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = null;
            }
        });
    }

    private void addItemToFirestore() {
        String newItemName = ItemName.getText().toString().trim();
        String newItemPrice = ItemPrice.getText().toString().trim();
        String newItemCount = ItemCount.getText().toString().trim();

        if (newItemName.isEmpty() || selectedCategory == null || newItemPrice.isEmpty() || newItemCount.isEmpty()) {
            Toast.makeText(this.getActivity(), "Please fill out all fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newItemName.matches("^[a-zA-Z]+(\\s[a-zA-Z]+)*$")) {
            Toast.makeText(this.getActivity(), "The name should contain only letters!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newItemName.length()<3) {
            Toast.makeText(this.getActivity(), "The name should be at least 3 letters!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(Integer.parseInt(newItemCount)==0||Float.parseFloat(newItemPrice)==0){
            Toast.makeText(this.getActivity(), "The price Or/and count should be greater than Zero!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float price = Float.parseFloat(newItemPrice);
            int stockCount = Integer.parseInt(newItemCount);

            // Step 1: Check if the item already exists
            dbHelper.getItemByName(newItemName.toLowerCase(),
                    existingItem -> {

                        String firestoreId = existingItem.getId();
                        if (firestoreId == null || firestoreId.isEmpty()) {
                            Log.e("Firestore", "Document ID not found for existing item.");
                            return;
                        }

                        // Item already exists, update stock count instead of adding a duplicate
                        int updatedStock = existingItem.getStockCount() + stockCount;
                        dbHelper.updateItemStock(firestoreId, updatedStock,
                                aVoid -> {
                                    Log.d("Firestore", "Stock updated for existing item: " + newItemName);
                                    //Toast.makeText(getActivity(), "Stock updated!", Toast.LENGTH_SHORT).show();
                                    showSuccessDialog("Stock updated successfully!");
                                    if (stockUpdatedListener != null) {
                                        stockUpdatedListener.onStockUpdated();
                                    }
                                    clearFields();
                                },
                                e -> Log.e("Firestore", "Error updating stock", e));
                    },
                    e -> {
                        // Item does not exist, add it as a new item
                        String newID = String.valueOf(System.currentTimeMillis()); // Unique ID
                        Item newItem = new Item(newID, newItemName.toLowerCase(), selectedCategory, price, stockCount);

                        dbHelper.addItem(newItem,
                                aVoid -> {
                                    Log.d("Firestore", "New item added successfully: " + newItemName);
                                    showSuccessDialog("Item added successfully!");
                                    if (stockUpdatedListener != null) {
                                        stockUpdatedListener.onStockUpdated();
                                    }
                                    clearFields();
                                },
                                error -> Log.e("Firestore", "Error adding item", error));
                    }
            );

        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Invalid number format!", Toast.LENGTH_SHORT).show();
        }
    }


    private void clearFields() {
        ItemName.setText("");
        ItemPrice.setText("");
        ItemCount.setText("");
        CategorySpinner.setSelection(0); // Reset spinner to first category
    }
    private void showSuccessDialog(String message) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Success")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
