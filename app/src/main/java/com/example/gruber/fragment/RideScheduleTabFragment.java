package com.example.gruber.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gruber.R;
import com.example.gruber.viewModels.RideViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RideScheduleTabFragment extends Fragment {

    private RideViewModel rideViewModel;
    private RadioGroup scheduleTypeGroup;
    private RadioButton nowButton;
    private RadioButton scheduledButton;
    private LinearLayout timePickerContainer;
    private TextInputLayout scheduledTimeInputLayout;
    private TextInputEditText scheduledTimeInput;
    private TextView timeInfoText;

    private Calendar selectedDateTime;
    private SimpleDateFormat dateTimeFormat;
    private SimpleDateFormat timeOnlyFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rideViewModel = new ViewModelProvider(requireActivity()).get(RideViewModel.class);
        dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        timeOnlyFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_schedule_tab, container, false);

        scheduleTypeGroup = view.findViewById(R.id.rg_schedule_type);
        nowButton = view.findViewById(R.id.rb_now);
        scheduledButton = view.findViewById(R.id.rb_scheduled);
        timePickerContainer = view.findViewById(R.id.ll_time_picker_container);
        scheduledTimeInputLayout = view.findViewById(R.id.til_scheduled_time);
        scheduledTimeInput = view.findViewById(R.id.et_scheduled_time);
        timeInfoText = view.findViewById(R.id.tv_time_info);

        // Initialize with current time
        selectedDateTime = Calendar.getInstance();
        
        // Set default to "now" - this will use current time when booking
        rideViewModel.setScheduledTime(null); // null means "now"

        scheduleTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_now) {
                timePickerContainer.setVisibility(View.GONE);
                rideViewModel.setScheduledTime(null); // null means use current time
            } else if (checkedId == R.id.rb_scheduled) {
                timePickerContainer.setVisibility(View.VISIBLE);
                // Set default scheduled time to current time
                selectedDateTime = Calendar.getInstance();
                updateScheduledTimeDisplay();
                rideViewModel.setScheduledTime(selectedDateTime.getTime());
            }
        });

        scheduledTimeInput.setOnClickListener(v -> showDateTimePicker());

        return view;
    }

    private void showDateTimePicker() {
        Calendar now = Calendar.getInstance();
        Calendar maxTime = Calendar.getInstance();
        maxTime.add(Calendar.HOUR_OF_DAY, 5); // 5 hours from now

        // Check if max time is on the same day as current time
        boolean isSameDay = now.get(Calendar.DAY_OF_YEAR) == maxTime.get(Calendar.DAY_OF_YEAR) &&
                            now.get(Calendar.YEAR) == maxTime.get(Calendar.YEAR);

        if (isSameDay) {
            // If within same day, only show time picker
            selectedDateTime = Calendar.getInstance();
            showTimePicker(now, maxTime);
        } else {
            // If spans multiple days, show date picker first
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Then show time picker
                        showTimePicker(now, maxTime);
                    },
                    selectedDateTime.get(Calendar.YEAR),
                    selectedDateTime.get(Calendar.MONTH),
                    selectedDateTime.get(Calendar.DAY_OF_MONTH)
            );

            // Set min and max dates
            datePickerDialog.getDatePicker().setMinDate(now.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(maxTime.getTimeInMillis());

            datePickerDialog.show();
        }
    }

    private void showTimePicker(Calendar now, Calendar maxTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);

                    // Validate the selected time is within 5 hours from now
                    if (selectedDateTime.before(now)) {
                        Toast.makeText(requireContext(), 
                            R.string.schedule_error_past, 
                            Toast.LENGTH_SHORT).show();
                        selectedDateTime = Calendar.getInstance(); // Reset to now
                    } else if (selectedDateTime.after(maxTime)) {
                        Toast.makeText(requireContext(), 
                            R.string.schedule_error_limit, 
                            Toast.LENGTH_SHORT).show();
                        selectedDateTime = (Calendar) maxTime.clone(); // Set to max time
                    }

                    updateScheduledTimeDisplay();
                    rideViewModel.setScheduledTime(selectedDateTime.getTime());
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true // 24 hour format
        );

        timePickerDialog.show();
    }

    private void updateScheduledTimeDisplay() {
        Calendar now = Calendar.getInstance();
        Calendar maxTime = Calendar.getInstance();
        maxTime.add(Calendar.HOUR_OF_DAY, 5);
        
        // Check if max time is on the same day
        boolean isSameDay = now.get(Calendar.DAY_OF_YEAR) == maxTime.get(Calendar.DAY_OF_YEAR) &&
                            now.get(Calendar.YEAR) == maxTime.get(Calendar.YEAR);
        
        // Format time display based on whether it spans multiple days
        String formattedTime;
        if (isSameDay) {
            formattedTime = timeOnlyFormat.format(selectedDateTime.getTime());
            scheduledTimeInputLayout.setHint(getString(R.string.schedule_time));
        } else {
            formattedTime = dateTimeFormat.format(selectedDateTime.getTime());
            scheduledTimeInputLayout.setHint(getString(R.string.schedule_datetime));
        }
        scheduledTimeInput.setText(formattedTime);
        
        // Calculate and show time difference
        long diffMillis = selectedDateTime.getTimeInMillis() - System.currentTimeMillis();
        long diffMinutes = diffMillis / (60 * 1000);
        long diffHours = diffMinutes / 60;
        long remainingMinutes = diffMinutes % 60;
        
        String timeInfo;
        if (diffHours > 0) {
            timeInfo = getString(R.string.schedule_in_hours, diffHours, remainingMinutes);
        } else {
            timeInfo = getString(R.string.schedule_in_minutes, diffMinutes);
        }
        timeInfoText.setText(timeInfo);
    }
}
