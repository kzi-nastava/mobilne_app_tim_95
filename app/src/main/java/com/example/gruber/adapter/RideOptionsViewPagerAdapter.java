package com.example.gruber.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.gruber.fragment.RideOptionsTabFragment;
import com.example.gruber.fragment.RidePassengersTabFragment;
import com.example.gruber.fragment.RideStopsTabFragment;

public class RideOptionsViewPagerAdapter extends FragmentStateAdapter {

    public RideOptionsViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RideStopsTabFragment();
            case 1:
                return new RidePassengersTabFragment();
            case 2:
                return new RideOptionsTabFragment();
            default:
                return new RideStopsTabFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
