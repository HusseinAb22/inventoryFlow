package com.Store.inventoryflow;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<Item> itemList;
    private FirestoreDBHelper dbHelper;
    private boolean Action;
    private OnStockUpdatedListener stockUpdatedListener;

    public ItemAdapter(List<Item> itemList, FirestoreDBHelper dbHelper,boolean Action) {
        this.itemList = itemList;
        this.dbHelper = dbHelper;
        this.Action=Action;
    }

    public void updateList(List<Item> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.itemName.setText(item.getName());
        holder.itemStock.setText("Stock: \n" + item.getStockCount());
        holder.itemCategory.setText("Category:\n" + item.getCategory().name());
        holder.itemPrice.setText("Price: \n$" + item.getPrice());
        if(Action){
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        }else{
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
        if(Action){
            //Edit Button Click
            holder.btnEdit.setOnClickListener(v -> showEditDialog(holder.itemView.getContext(), item, position));

            //Delete Button Click with Confirmation Dialog
            holder.btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(holder.itemView.getContext(), item, position));
        }


    }

    private void showDeleteConfirmationDialog(Context context, Item item, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete " + item.getName()+"  permanently?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Retrieve the Firestore ID before deleting
            dbHelper.getItemByName(item.getName().toLowerCase(),
                    existingItem -> {
                        if (existingItem == null || existingItem.getId() == null || existingItem.getId().isEmpty()) {
                            Log.e("Firestore", "Error: Document ID not found for existing item.");
                            Toast.makeText(context, "Item not found in Firestore!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String firestoreId = existingItem.getId();
                        Log.d("Firestore", "Deleting item with ID: " + firestoreId);

                        // Now delete the item using its Firestore ID
                        dbHelper.deleteItem(firestoreId,
                                aVoid -> {
                                    Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                                    itemList.remove(position);
                                    notifyDataSetChanged();
                                    if (stockUpdatedListener != null) {
                                        stockUpdatedListener.onStockUpdated();
                                    }
                                },
                                e -> {
                                    Log.e("Firestore", "Error deleting item", e);
                                    Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
                                });
                    },
                    e -> {
                        Log.e("Firestore", "Error retrieving item for deletion", e);
                        Toast.makeText(context, "Failed to find item in Firestore", Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showEditDialog(Context context, Item item, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_item, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Get references to UI elements
        EditText editName = dialogView.findViewById(R.id.edit_item_name);
        Spinner editCategory = dialogView.findViewById(R.id.edit_item_category);
        EditText editPrice = dialogView.findViewById(R.id.edit_item_price);
        EditText editStock = dialogView.findViewById(R.id.edit_item_stock);
        Button btnSave = dialogView.findViewById(R.id.btn_save_edit);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_edit);

        // Set initial values
        editName.setText(item.getName());
        editPrice.setText(String.valueOf(item.getPrice()));
        editStock.setText(String.valueOf(item.getStockCount()));

        // Populate Spinner with ItemCategory values
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, getCategoryList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editCategory.setAdapter(adapter);

        // Set correct category selection
        editCategory.setSelection(getCategoryList().indexOf(item.getCategory().name()));

        // Save Changes
        btnSave.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            String newCategoryString = editCategory.getSelectedItem().toString();
            float newPrice;
            int newStock;

            try {
                newPrice = Float.parseFloat(editPrice.getText().toString().trim());
                newStock = Integer.parseInt(editStock.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid number format!", Toast.LENGTH_SHORT).show();
                return;
            }

            ItemCategory newCategory = ItemCategory.valueOf(newCategoryString);

            dbHelper.updateItemDetails(item.getId(), newName, newCategory, newPrice, newStock,
                    aVoid -> {
                        Toast.makeText(context, "Item updated successfully!", Toast.LENGTH_SHORT).show();
                        itemList.set(position, new Item(item.getId(), newName, newCategory, newPrice, newStock));
                        notifyDataSetChanged();
                        if (stockUpdatedListener != null) {
                            stockUpdatedListener.onStockUpdated();
                        }
                        dialog.dismiss();
                    },
                    e -> Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show());
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private List<String> getCategoryList() {
        List<String> categories = new ArrayList<>();
        for (ItemCategory category : ItemCategory.values()) {
            categories.add(category.name());
        }
        return categories;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemStock, itemCategory, itemPrice;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemStock = itemView.findViewById(R.id.item_stock);
            itemCategory = itemView.findViewById(R.id.item_category);
            itemPrice = itemView.findViewById(R.id.item_price);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
