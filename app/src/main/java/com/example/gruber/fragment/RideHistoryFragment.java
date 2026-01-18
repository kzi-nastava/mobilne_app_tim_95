package com.example.gruber.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.gruber.R;
import com.example.gruber.adapter.RideAdapter;
import com.example.gruber.adapter.UsersRideAdapter;
import com.example.gruber.models.Ride;
import com.example.gruber.models.enums.RideStatus;
import com.example.gruber.models.enums.SortCategory;
import com.example.gruber.viewModels.RideViewModel;
import com.example.gruber.viewModels.SearchViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RideHistoryFragment extends Fragment {

    private RideViewModel rideViewModel;
    private SearchViewModel searchViewModel;
    private HashMap<String, Boolean> statusSearchMap;

    private MaterialButton statusBtn, sortBtn, sortOrderBtn;
    private LinearLayout dropDownStatusContainer, dropDownSortContainer;

    private SortCategory sortCategory;
    private boolean isAscending;

    public RideHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rideViewModel = new ViewModelProvider(requireActivity()).get(RideViewModel.class);
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        statusSearchMap = new HashMap<>();
        for (RideStatus status : RideStatus.values()) {
            statusSearchMap.put(status.toString(), false);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ride_history, container, false);

        statusBtn = view.findViewById(R.id.btn_status);
        statusBtn.setOnClickListener(v -> setUpStatusDropDownMenu());

        sortBtn = view.findViewById(R.id.btn_sort_by);
        sortBtn.setOnClickListener(v -> setUpSortDropDownMenu());

        sortOrderBtn = view.findViewById(R.id.btn_sort_order);
        isAscending = false;
        setUpSortOrderBtn();

        setUpRidesAdapter(view.findViewById(R.id.rides));

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        dropDownStatusContainer.setVisibility(View.GONE);
    }

    private void setUpStatusDropDownMenu() {
        LayoutInflater statusMenuInflater = LayoutInflater.from(statusBtn.getContext());

        dropDownStatusContainer = new LinearLayout(statusBtn.getContext());
        dropDownStatusContainer.setOrientation(LinearLayout.VERTICAL);

        for (RideStatus status : RideStatus.values()) {
            View item = statusMenuInflater.inflate(R.layout.menu_item_checkbox, dropDownStatusContainer, false);

            MaterialTextView textView = item.findViewById(R.id.cb_text);
            textView.setText(status.toString());

            MaterialCheckBox checkBox = item.findViewById(R.id.check_box);
            checkBox.setChecked(statusSearchMap.get(status.toString()));

            textView.setOnClickListener(click -> {
                boolean newValue = !checkBox.isChecked();
                checkBox.setChecked(newValue);
                statusSearchMap.put(status.toString(), newValue);
            });
            checkBox.setOnClickListener(click -> {
                boolean newValue = checkBox.isChecked();
                statusSearchMap.put(status.toString(), newValue);
            });

            dropDownStatusContainer.addView(item);

        }

        PopupWindow popup = new PopupWindow(dropDownStatusContainer, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setElevation(8f);
        popup.setOutsideTouchable(true);
        popup.showAsDropDown(statusBtn);
    }

    private void setUpSortDropDownMenu() {
        LayoutInflater statusMenuInflater = LayoutInflater.from(sortBtn.getContext());

        dropDownSortContainer = new LinearLayout(sortBtn.getContext());
        dropDownSortContainer.setOrientation(LinearLayout.VERTICAL);

        for (SortCategory category : SortCategory.values()) {
            View item = statusMenuInflater.inflate(R.layout.menu_item_checkbox, dropDownSortContainer, false);

            MaterialTextView textView = item.findViewById(R.id.cb_text);
            textView.setText(category.toString());

            MaterialCheckBox checkBox = item.findViewById(R.id.check_box);
            checkBox.setVisibility(View.GONE);

            item.setOnClickListener(click -> {
                try {
                    SortCategory newSortCategory = SortCategory.valueOf(textView.getText().toString());
                    if (newSortCategory.equals(sortCategory)) {
                        sortCategory = null;
                        sortBtn.setText("Sort by");
                    }
                    else {
                        sortCategory = newSortCategory;
                        sortBtn.setText(sortCategory.toString());
                    }
                } catch (Exception e) {
                    sortCategory = null;
                    sortBtn.setText("Sort by");
                }

            });

            dropDownSortContainer.addView(item);

        }

        PopupWindow popup = new PopupWindow(dropDownSortContainer, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setElevation(8f);
        popup.setOutsideTouchable(true);
        popup.showAsDropDown(sortBtn);
    }

    private void setUpSortOrderBtn() {
        sortOrderBtn.setRotation(isAscending ? 0f : 180f);

        sortOrderBtn.setOnClickListener(v -> {

            isAscending = !isAscending;

            sortOrderBtn.animate()
                    .rotation(isAscending ? 0f : 180f)
                    .setDuration(200)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        });
    }

    private void setUpRidesAdapter(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        UsersRideAdapter adapter = new UsersRideAdapter();
        recyclerView.setAdapter(adapter);

        searchViewModel.getRides().observe(getViewLifecycleOwner(), adapter::submitRides);
        searchViewModel.getRidesForUser();


    }

}