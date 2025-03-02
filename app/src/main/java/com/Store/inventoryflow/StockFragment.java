package com.Store.inventoryflow;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;




public class StockFragment extends Fragment implements OnStockUpdatedListener {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private FirestoreDBHelper dbHelper;
    private EditText searchBar;
    private Spinner categoryFilter;
    private Button btnNext, btnPrev;

    private List<Item> fullItemList = new ArrayList<>();
    private List<Item> displayedItems = new ArrayList<>();
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 10;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stock, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new FirestoreDBHelper();
        recyclerView = view.findViewById(R.id.recyclerView_stock);
        searchBar = view.findViewById(R.id.search_bar);
        categoryFilter = view.findViewById(R.id.category_filter);
        btnNext = view.findViewById(R.id.btn_next);
        btnPrev = view.findViewById(R.id.btn_prev);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemAdapter = new ItemAdapter(displayedItems, dbHelper,true);
        recyclerView.setAdapter(itemAdapter);

        loadItems();
        setupSearch();
        setupCategoryFilter();
        setupPagination();
    }

    public void loadItems() {
        dbHelper.getAllItems(items -> {
            fullItemList.clear();
            fullItemList.addAll(items);
            applyFilters();
        }, e -> Log.e("Firestore", "Error fetching items", e));
    }
    @Override
    public void onStockUpdated() {
        Log.d("StockFragment", "Stock updated - refreshing list.");
        loadItems();
    }

    private void applyFilters() {
        if (categoryFilter.getSelectedItem() == null) {
            Log.e("StockFragment", "Category filter is not initialized yet");
            return;
        }

        String query = searchBar.getText().toString().toLowerCase();
        String selectedCategory = categoryFilter.getSelectedItem().toString();

        displayedItems.clear();
        for (Item item : fullItemList) {
            if (item.getName().toLowerCase().contains(query) &&
                    (selectedCategory.equals("All") || item.getCategory().name().equals(selectedCategory))) {
                displayedItems.add(item);
            }
        }

        paginateItems();
    }



    private void paginateItems() {
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, displayedItems.size());

        List<Item> paginatedList = new ArrayList<>(displayedItems.subList(start, end));
        itemAdapter.updateList(paginatedList);

        btnPrev.setVisibility(currentPage > 0 ? View.VISIBLE : View.GONE);
        btnNext.setVisibility(end < displayedItems.size() ? View.VISIBLE : View.GONE);
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void setupCategoryFilter() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("All"); // Default option to show all items
        for (ItemCategory category : ItemCategory.values()) {
            categoryNames.add(category.name());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilter.setAdapter(adapter);

        categoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onStockUpdated();
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }



    private void setupPagination() {
        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                paginateItems();
            }
        });

        btnNext.setOnClickListener(v -> {
            if ((currentPage + 1) * ITEMS_PER_PAGE < displayedItems.size()) {
                currentPage++;
                paginateItems();
            }
        });
    }
}
