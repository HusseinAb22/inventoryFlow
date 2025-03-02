package com.Store.inventoryflow;

import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final SparseArray<Fragment> fragmentSparseArray = new SparseArray<>();
    private final OnStockUpdatedListener stockUpdatedListener;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, OnStockUpdatedListener listener) {
        super(fragmentActivity);
        this.stockUpdatedListener = listener;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new StockFragment();
                break;
            case 2:
                fragment = new AddFragment(); // Ensure it's using the listener
                break;
            default:
                fragment = new HomeFragment();
                break;
        }
        fragmentSparseArray.put(position, fragment); // Store reference
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    // Retrieve stored fragment
    public Fragment getFragment(int position) {
        return fragmentSparseArray.get(position);
    }
}
