package com.example.gruber.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.adapter.UsersRideAdapter;
import com.example.gruber.models.enums.RideStatus;
import com.example.gruber.models.enums.SortCategory;
import com.example.gruber.viewModels.RideViewModel;
import com.example.gruber.viewModels.SearchViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class RideHistoryFragment extends Fragment {

    protected RideViewModel rideViewModel;
    protected SearchViewModel searchViewModel;
    protected HashMap<String, Boolean> statusSearchMap;

    protected MaterialButton statusBtn, sortBtn, sortOrderBtn, applyBtn;
    protected LinearLayout dropDownStatusContainer, dropDownSortContainer;

    protected TextView tvDateStart, tvDateEnd;

    protected MaterialDatePicker<Long> dpStart, dpEnd;
    protected Timestamp timestampStart, timestampEnd;

    protected SortCategory sortCategory = SortCategory.DATE;
    protected boolean isAscending = true;

    protected SessionManager sessionManager;

    protected SensorManager sensorManager;
    protected Sensor accelerometer;

    protected static final float SHAKE_THRESHOLD = 3.4f;
    protected static final int SHAKE_SLOP_TIME_MS = 500;
    protected long lastShakeTime = 0;

    protected final String displayDatePattern = "dd.MM.yyyy";

    public RideHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rideViewModel = new ViewModelProvider(requireActivity()).get(RideViewModel.class);
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        sessionManager = new SessionManager(requireContext());
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

        // Only past dates are selectable
        CalendarConstraints calendarConstraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();

        tvDateStart = view.findViewById(R.id.tvFromDate);
        dpStart = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(getResources().getString(R.string.select_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(calendarConstraints)
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .build();
        tvDateStart.setOnClickListener(v-> dpStart.show(getParentFragmentManager(), "DATE_PICKER"));

        tvDateEnd = view.findViewById(R.id.tvToDate);
        dpEnd = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(getResources().getString(R.string.select_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(calendarConstraints)
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .build();
        tvDateEnd.setOnClickListener(v -> dpEnd.show(getParentFragmentManager(), "DATE_PICKER"));

        dpStart.addOnPositiveButtonClickListener(this::setDateSelectionStart);
        dpStart.addOnCancelListener(v -> clearDateSelectionStart());

        dpEnd.addOnPositiveButtonClickListener(this::setDateSelectionEnd);
        dpEnd.addOnCancelListener(v -> clearDateSelectionEnd());

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
    public void onResume() {
        super.onResume();

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(
                    shakeListener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_UI
            );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dropDownStatusContainer!=null) dropDownStatusContainer.setVisibility(View.GONE);
        if (dropDownSortContainer!=null) dropDownSortContainer.setVisibility(View.GONE);
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeListener);
        }
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

    protected void setUpRidesAdapter(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        UsersRideAdapter adapter = new UsersRideAdapter(ride -> {
            rideViewModel.setRide(ride);
            NavHostFragment.findNavController(RideHistoryFragment.this).navigate(R.id.action_usersRidesHistory_to_rideDetailsFragment);
        });
        adapter.setOnFavoriteClickListener(ride -> {
            if (ride == null || ride.stopList == null || ride.stopList.isEmpty()) {
                Toast.makeText(requireContext(), R.string.no_stops_added, Toast.LENGTH_SHORT).show();
                return;
            }

            String email = sessionManager.getUserEmail();
            String description = ride.getStartAddress() + " → " + ride.getEndAddress();
            rideViewModel.saveFavoriteRoute(email, ride.stopList, description, success -> {
                if (success) {
                    Toast.makeText(requireContext(), R.string.favorite_route_saved, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), R.string.favorite_route_save_failed, Toast.LENGTH_SHORT).show();
                }
            });
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

        //getSort type
        //call VM function
        searchViewModel.sortRidesForUser(sortCategory, isAscending, statuses, timestampStart, timestampEnd);
    }

    private final SensorEventListener shakeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when device is still
            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);


            if (gForce > SHAKE_THRESHOLD) {
                long now = System.currentTimeMillis();

                if (lastShakeTime + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }

                lastShakeTime = now;
                onPhoneShaken();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // no-op
        }
    };

    private void onPhoneShaken() {
        isAscending = !isAscending;
        sortOrderBtn.setRotation(isAscending ? 0f : 180f);
        searchViewModel.sortExistingRides(isAscending);
    }

    private void setDateSelectionStart(Long selection) {
        //set long value to searchViewModel
        timestampStart = new Timestamp(new Date(selection));

        tvDateStart.setText(formatTimestamp(timestampStart));
    }
    private void clearDateSelectionStart() {
        timestampStart = null;
        tvDateStart.setText("");
    }
    private void setDateSelectionEnd(Long selection) {
        timestampEnd = new Timestamp(new Date(selection));

        tvDateEnd.setText(formatTimestamp(timestampEnd));
    }
    private void clearDateSelectionEnd() {
        timestampEnd = null;
        tvDateEnd.setText("");
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(displayDatePattern);

        return timestamp
                .toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .format(formatter);
    }

}