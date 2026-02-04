package com.example.gruber.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.gruber.fragment.admin.UserBlockListFragment;
import com.example.gruber.models.enums.UserRole;

public class UserBlockManagementPagerAdapter extends FragmentStateAdapter {

    private UserBlockListFragment passengerFragment;
    private UserBlockListFragment driverFragment;

    public UserBlockManagementPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        passengerFragment = UserBlockListFragment.newInstance(UserRole.USER);
        driverFragment = UserBlockListFragment.newInstance(UserRole.DRIVER);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? passengerFragment : driverFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public void filterUsers(String query) {
        if (query == null) query = "";
        
        if (passengerFragment != null) {
            passengerFragment.filterUsers(query);
        }
        if (driverFragment != null) {
            driverFragment.filterUsers(query);
        }
    }
}
