package com.example.gruber.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class RideHistoryFragment extends Fragment {

    private RideViewModel rideViewModel;
    private SearchViewModel searchViewModel;
    private HashMap<String, Boolean> statusSearchMap;

    private MaterialButton statusBtn, sortBtn, sortOrderBtn, applyBtn;
    private LinearLayout dropDownStatusContainer, dropDownSortContainer;

    private SortCategory sortCategory = SortCategory.DATE;
    private boolean isAscending = false;

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

        applyBtn = view.findViewById(R.id.btnApply);
        applyBtn.setOnClickListener(v -> applySort());

        RecyclerView recyclerView = view.findViewById(R.id.rides);
        setUpRidesAdapter(recyclerView);

        searchViewModel.getRides().observe(getViewLifecycleOwner(), rides -> {
            if (rides.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                view.findViewById(R.id.emptyRecyclerMessage).setVisibility(View.VISIBLE);
            }
            else {
                recyclerView.setVisibility(View.VISIBLE);
                view.findViewById(R.id.emptyRecyclerMessage).setVisibility(View.GONE);
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dropDownStatusContainer!=null) dropDownStatusContainer.setVisibility(View.GONE);
        if (dropDownSortContainer!=null) dropDownSortContainer.setVisibility(View.GONE);
    }

    private void setUpStatusDropDownMenu() {
        LayoutInflater statusMenuInflater = LayoutInflater.from(statusBtn.getContext());

        dropDownStatusContainer = new LinearLayout(statusBtn.getContext());
        dropDownStatusContainer.setOrientation(LinearLayout.VERTICAL);

        for (RideStatus status : RideStatus.values()) {
            View item = statusMenuInflater.inflate(R.layout.menu_item_checkbox, dropDownStatusContainer, false);

            MaterialTextView textView = item.findViewById(R.id.cb_text);
            var text = status.toString().substring(0, 1).toUpperCase(Locale.ROOT)
                    + status.toString().substring(1).toLowerCase(Locale.ROOT).replace("_", " ");
            textView.setText(text);

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
            var c = category.toString();
            var text = c.substring(0,1).toUpperCase() + c.substring(1).toLowerCase();
            textView.setText(text);

            MaterialCheckBox checkBox = item.findViewById(R.id.check_box);
            checkBox.setVisibility(View.GONE);

            item.setOnClickListener(click -> {
                try {
                    SortCategory newSortCategory = SortCategory.valueOf(textView.getText().toString().toUpperCase());
                    if (newSortCategory.equals(sortCategory)) {
                        sortCategory = SortCategory.DATE;
                        dropDownSortContainer.setVisibility(View.GONE);
                        sortBtn.setText("Sort by");
                    }
                    else {
                        sortCategory = newSortCategory;
                        dropDownSortContainer.setVisibility(View.GONE);
                        sortBtn.setText(textView.getText().toString());
                    }

                } catch (Exception e) {
                    sortCategory = SortCategory.DATE;
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
        UsersRideAdapter adapter = new UsersRideAdapter(ride -> {
            rideViewModel.setRide(ride);
            NavHostFragment.findNavController(RideHistoryFragment.this).navigate(R.id.action_usersRidesHistory_to_rideDetailsFragment);
        });
        recyclerView.setAdapter(adapter);

        searchViewModel.getRides().observe(getViewLifecycleOwner(), adapter::submitRides);
        searchViewModel.getRidesForUser();


    }

    private void applySort() {
        //getStatuses for search
        List<RideStatus> statuses = new ArrayList<>();
        for (String statusKey : statusSearchMap.keySet()) {
            if (Boolean.TRUE.equals(statusSearchMap.get(statusKey))) statuses.add(RideStatus.valueOf(statusKey));
        }
        if (statuses.isEmpty()) statuses = List.of(RideStatus.values());

        //getInterval for search
        //fix the input date mechanism
        LocalDateTime from = LocalDateTime.now().minusMonths(2);
        LocalDateTime to = LocalDateTime.now();
        //getSort type
        //call VM function
        searchViewModel.sortRidesForUser(sortCategory, isAscending, statuses, from, to);
    }

}