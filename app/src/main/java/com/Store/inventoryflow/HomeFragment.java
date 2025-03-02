package com.Store.inventoryflow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FirestoreDBHelper dbHelper;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList = new ArrayList<>();

    private static final int STOCK_THRESHOLD = 5; // Change this to 10 if needed

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new FirestoreDBHelper();
        recyclerView = view.findViewById(R.id.recyclerView_outOfStock);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        itemAdapter = new ItemAdapter(itemList, dbHelper,false);
        recyclerView.setAdapter(itemAdapter);

        loadLowStockItems();
    }
    @Override
    public void onResume() {
        super.onResume();
        loadLowStockItems(); // Refresh low-stock items when fragment is visible
    }



    private void loadLowStockItems() {
        dbHelper.getLowStockItems(STOCK_THRESHOLD, items -> {
            itemList.clear();
            itemList.addAll(items);
            itemAdapter.notifyDataSetChanged();
        }, e -> Log.e("Firestore", "Error fetching low stock items", e));
    }
}
