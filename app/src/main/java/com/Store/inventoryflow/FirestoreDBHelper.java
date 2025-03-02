package com.Store.inventoryflow;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreDBHelper {

    private static final String TAG = "FirestoreDBHelper";
    private static final String COLLECTION_NAME = "items"; // Firestore collection name

    private final FirebaseFirestore db;

    public FirestoreDBHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    /** ✅ Add an Item to Firestore */
    public void addItem(Item item, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(item.getId())
                .set(item)
                .addOnSuccessListener(documentReference -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }


    /** ✅ Retrieve an Item by Name (with Firestore Document ID) */
    public void getItemByName(String itemName, OnSuccessListener<Item> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("name", itemName.toLowerCase()) // Store a lowercase field in Firestore
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Item item = document.toObject(Item.class);
                        if (item != null) {
                            item.setId(document.getId());
                        }
                        onSuccess.onSuccess(item);
                    } else {
                        Log.e(TAG, "Item not found");
                        onFailure.onFailure(new Exception("Item not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }





    /** ✅ Retrieve All Items */
    public void getAllItems(OnSuccessListener<List<Item>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Item> itemList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Item item = document.toObject(Item.class);
                        itemList.add(item);
                    }
                    onSuccess.onSuccess(itemList);
                })
                .addOnFailureListener(onFailure);
    }


    /** ✅ Update Item Stock */
    public void updateItemStock(String itemId, int newStockCount, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(itemId) // Now using correct Firestore ID
                .update("stockCount", newStockCount)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }




    /** ✅ Delete an Item */
    public void deleteItem(String itemId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(itemId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
    /** ✅ Update All Item Details */
    public void updateItemDetails(String itemId, String name, ItemCategory category, float price, int stockCount,
                                  OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(itemId)
                .update("name", name,
                        "category", category,
                        "price", price,
                        "stockCount", stockCount)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getLowStockItems(int threshold, OnSuccessListener<List<Item>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .whereLessThanOrEqualTo("stockCount", threshold)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Item> lowStockItems = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Item item = document.toObject(Item.class);
                        if (item != null) {
                            item.setId(document.getId());
                            lowStockItems.add(item);
                        }
                    }
                    onSuccess.onSuccess(lowStockItems);
                })
                .addOnFailureListener(onFailure);
    }


}
