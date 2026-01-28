package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.models.Ride;
import com.example.gruber.models.enums.RideStatus;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminReportsFragment extends Fragment {

    @Inject
    SessionManager sessionManager;

    @Inject
    FirebaseFirestore firestore;

    private Spinner spinnerUserType;
    private Spinner spinnerSelectUser;
    private TextView txtFromDate;
    private TextView txtToDate;
    private Button btnSelectFromDate;
    private Button btnSelectToDate;
    private Button btnGenerateReport;
    private LinearLayout statisticsContainer;
    private TextView txtRidesTotal;
    private TextView txtRidesAverage;
    private TextView txtKilometersTotal;
    private TextView txtKilometersAverage;
    private TextView txtMoneyTotal;
    private TextView txtMoneyAverage;
    private LineChart ridesChart;
    private LineChart kilometersChart;
    private LineChart moneyChart;

    private long fromDateMillis = 0;
    private long toDateMillis = 0;

    private List<String> driverEmails = new ArrayList<>();
    private List<String> passengerEmails = new ArrayList<>();

    public AdminReportsFragment() {
        super(R.layout.fragment_admin_reports);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerUserType = view.findViewById(R.id.spinnerUserType);
        spinnerSelectUser = view.findViewById(R.id.spinnerSelectUser);
        txtFromDate = view.findViewById(R.id.txtFromDate);
        txtToDate = view.findViewById(R.id.txtToDate);
        btnSelectFromDate = view.findViewById(R.id.btnSelectFromDate);
        btnSelectToDate = view.findViewById(R.id.btnSelectToDate);
        btnGenerateReport = view.findViewById(R.id.btnGenerateReport);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        statisticsContainer = view.findViewById(R.id.statisticsContainer);
        txtRidesTotal = view.findViewById(R.id.txtRidesTotal);
        txtRidesAverage = view.findViewById(R.id.txtRidesAverage);
        txtKilometersTotal = view.findViewById(R.id.txtKilometersTotal);
        ridesChart = view.findViewById(R.id.ridesChart);
        kilometersChart = view.findViewById(R.id.kilometersChart);
        moneyChart = view.findViewById(R.id.moneyChart);
        txtKilometersAverage = view.findViewById(R.id.txtKilometersAverage);
        txtMoneyTotal = view.findViewById(R.id.txtMoneyTotal);
        txtMoneyAverage = view.findViewById(R.id.txtMoneyAverage);

        setupUserTypeSpinner();
        loadUsers();

        btnSelectFromDate.setOnClickListener(v -> showDatePicker(true));
        btnSelectToDate.setOnClickListener(v -> showDatePicker(false));
        btnGenerateReport.setOnClickListener(v -> generateReport());
    }

    private void setupUserTypeSpinner() {
        List<String> userTypes = new ArrayList<>();
        userTypes.add(getString(R.string.all_drivers));
        userTypes.add(getString(R.string.all_passengers));
        userTypes.add("Select Individual Driver");
        userTypes.add("Select Individual Passenger");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                userTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserType.setAdapter(adapter);

        spinnerUserType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateUserSelectionSpinner(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void updateUserSelectionSpinner(int userTypePosition) {
        List<String> users = new ArrayList<>();
        
        switch (userTypePosition) {
            case 0: // All Drivers
            case 1: // All Passengers
                spinnerSelectUser.setVisibility(View.GONE);
                break;
            case 2: // Select Individual Driver
                spinnerSelectUser.setVisibility(View.VISIBLE);
                users = driverEmails;
                break;
            case 3: // Select Individual Passenger
                spinnerSelectUser.setVisibility(View.VISIBLE);
                users = passengerEmails;
                break;
        }

        if (spinnerSelectUser.getVisibility() == View.VISIBLE) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    users
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSelectUser.setAdapter(adapter);
        }
    }

    private void loadUsers() {
        // Load drivers
        firestore.collection("users")
                .whereEqualTo("role", "DRIVER")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    driverEmails.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String email = document.getString("email");
                        if (email != null) {
                            driverEmails.add(email);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load drivers", Toast.LENGTH_SHORT).show();
                });

        // Load passengers
        firestore.collection("users")
                .whereEqualTo("role", "USER")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    passengerEmails.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String email = document.getString("email");
                        if (email != null) {
                            passengerEmails.add(email);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load passengers", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDatePicker(boolean isFromDate) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isFromDate ? getString(R.string.from_date) : getString(R.string.to_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (isFromDate) {
                fromDateMillis = selection;
                txtFromDate.setText(formatDate(selection));
            } else {
                toDateMillis = selection;
                txtToDate.setText(formatDate(selection));
            }
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    private void generateReport() {
        if (fromDateMillis == 0 || toDateMillis == 0) {
            Toast.makeText(getContext(), "Please select both from and to dates", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fromDateMillis > toDateMillis) {
            Toast.makeText(getContext(), "From date must be before to date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Timestamp objects for date range
        Timestamp fromDate = new Timestamp(new Date(fromDateMillis));
        Timestamp toDate = new Timestamp(new Date(toDateMillis + 86400000L)); // Add 1 day to include end date

        int userTypePosition = spinnerUserType.getSelectedItemPosition();
        String selectedUser = null;
        if (spinnerSelectUser.getVisibility() == View.VISIBLE && spinnerSelectUser.getSelectedItem() != null) {
            selectedUser = (String) spinnerSelectUser.getSelectedItem();
        }

        switch (userTypePosition) {
            case 0: // All Drivers
                fetchAllDriversRides(fromDate, toDate);
                break;
            case 1: // All Passengers
                fetchAllPassengersRides(fromDate, toDate);
                break;
            case 2: // Individual Driver
                if (selectedUser != null) {
                    fetchDriverRides(selectedUser, fromDate, toDate);
                } else {
                    Toast.makeText(getContext(), "Please select a driver", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3: // Individual Passenger
                if (selectedUser != null) {
                    fetchPassengerRides(selectedUser, fromDate, toDate);
                } else {
                    Toast.makeText(getContext(), "Please select a passenger", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void fetchAllDriversRides(Timestamp fromDate, Timestamp toDate) {
        firestore.collection("rides")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Filter to only include completed rides in date range where driverEmail is in our drivers list
                    List<QueryDocumentSnapshot> driverRides = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Ride ride = doc.toObject(Ride.class);
                        String driverEmail = doc.getString("driverEmail");
                        if (driverEmail != null && driverEmails.contains(driverEmail) &&
                            ride.getStatus() == RideStatus.COMPLETED &&
                            ride.getFinishedAt() != null &&
                            ride.getFinishedAt().compareTo(fromDate) >= 0 &&
                            ride.getFinishedAt().compareTo(toDate) < 0) {
                            driverRides.add(doc);
                        }
                    }
                    calculateAndDisplayStatistics(driverRides, true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchAllPassengersRides(Timestamp fromDate, Timestamp toDate) {
        firestore.collection("rides")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Filter to only include completed rides in date range where at least one passenger is in our passengers list
                    List<QueryDocumentSnapshot> passengerRides = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Ride ride = doc.toObject(Ride.class);
                        if (ride.getStatus() == RideStatus.COMPLETED &&
                            ride.getFinishedAt() != null &&
                            ride.getFinishedAt().compareTo(fromDate) >= 0 &&
                            ride.getFinishedAt().compareTo(toDate) < 0 &&
                            ride.getPassengerEmails() != null) {
                            for (String email : ride.getPassengerEmails()) {
                                if (passengerEmails.contains(email)) {
                                    passengerRides.add(doc);
                                    break;
                                }
                            }
                        }
                    }
                    calculateAndDisplayStatistics(passengerRides, false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch rides: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchDriverRides(String driverEmail, Timestamp fromDate, Timestamp toDate) {
        firestore.collection("rides")
                .whereEqualTo("driverEmail", driverEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<QueryDocumentSnapshot> rides = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Ride ride = doc.toObject(Ride.class);
                        if (ride.getStatus() == RideStatus.COMPLETED &&
                            ride.getFinishedAt() != null &&
                            ride.getFinishedAt().compareTo(fromDate) >= 0 &&
                            ride.getFinishedAt().compareTo(toDate) < 0) {
                            rides.add(doc);
                        }
                    }
                    calculateAndDisplayStatistics(rides, true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch rides: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchPassengerRides(String passengerEmail, Timestamp fromDate, Timestamp toDate) {
        firestore.collection("rides")
                .whereArrayContains("passengerEmails", passengerEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<QueryDocumentSnapshot> rides = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Ride ride = doc.toObject(Ride.class);
                        if (ride.getStatus() == RideStatus.COMPLETED &&
                            ride.getFinishedAt() != null &&
                            ride.getFinishedAt().compareTo(fromDate) >= 0 &&
                            ride.getFinishedAt().compareTo(toDate) < 0) {
                            rides.add(doc);
                        }
                    }
                    calculateAndDisplayStatistics(rides, false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch rides: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void calculateAndDisplayStatistics(List<QueryDocumentSnapshot> queryDocumentSnapshots, boolean isDriverMode) {
        Map<String, Integer> ridesPerDay = new TreeMap<>();
        Map<String, Double> kilometersPerDay = new TreeMap<>();
        Map<String, Integer> moneyPerDay = new TreeMap<>();
        int totalRides = 0;
        double totalKilometers = 0;
        int totalMoney = 0;

        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
            Ride ride = document.toObject(Ride.class);
            
            // Count rides per day
            if (ride.getFinishedAt() != null) {
                String dateKey = formatDate(ride.getFinishedAt().toDate().getTime());
                ridesPerDay.put(dateKey, ridesPerDay.getOrDefault(dateKey, 0) + 1);
                
                // Sum kilometers per day
                double km = ride.getDistanceMeters() > 0 ? ride.getDistanceMeters() / 1000.0 : 0;
                kilometersPerDay.put(dateKey, kilometersPerDay.getOrDefault(dateKey, 0.0) + km);
                
                // Sum money per day
                moneyPerDay.put(dateKey, moneyPerDay.getOrDefault(dateKey, 0) + ride.getPriceDin());
            }

            // Sum up kilometers
            if (ride.getDistanceMeters() > 0) {
                totalKilometers += ride.getDistanceMeters() / 1000.0; // Convert to km
            }

            // Sum up money
            totalMoney += ride.getPriceDin();
            totalRides++;
        }

        // Calculate number of days in range
        long daysDiff = (toDateMillis - fromDateMillis) / (1000 * 60 * 60 * 24) + 1;
        
        // Calculate averages
        double avgRidesPerDay = daysDiff > 0 ? (double) totalRides / daysDiff : 0;
        double avgKilometersPerDay = daysDiff > 0 ? totalKilometers / daysDiff : 0;
        double avgMoneyPerDay = daysDiff > 0 ? (double) totalMoney / daysDiff : 0;

        // Display statistics
        statisticsContainer.setVisibility(View.VISIBLE);
        
        txtRidesTotal.setText(String.format(Locale.getDefault(), "%s: %d", getString(R.string.cumulative_total), totalRides));
        txtRidesAverage.setText(String.format(Locale.getDefault(), "%s: %.2f per day", getString(R.string.average), avgRidesPerDay));
        
        txtKilometersTotal.setText(String.format(Locale.getDefault(), "%s: %.2f km", getString(R.string.cumulative_total), totalKilometers));
        txtKilometersAverage.setText(String.format(Locale.getDefault(), "%s: %.2f km per day", getString(R.string.average), avgKilometersPerDay));
        
        String moneyLabel = isDriverMode ? "Earned" : "Spent";
        txtMoneyTotal.setText(String.format(Locale.getDefault(), "%s: %d RSD", getString(R.string.cumulative_total), totalMoney));
        txtMoneyAverage.setText(String.format(Locale.getDefault(), "%s: %.2f RSD per day", getString(R.string.average), avgMoneyPerDay));
        
        // Setup charts
        setupChart(ridesChart, ridesPerDay, "Rides");
        setupChartDouble(kilometersChart, kilometersPerDay, "Kilometers");
        setupChart(moneyChart, moneyPerDay, "RSD");
    }
    
    private void setupChart(LineChart chart, Map<String, Integer> dataMap, String label) {
        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        int index = 0;
        
        for (Map.Entry<String, Integer> entry : dataMap.entrySet()) {
            entries.add(new Entry(index, entry.getValue()));
            dates.add(entry.getKey());
            index++;
        }
        
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(0xFF6200EE);
        dataSet.setValueTextColor(0xFF000000);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(0xFF6200EE);
        dataSet.setDrawValues(true);
        
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < dates.size()) {
                    return dates.get(idx);
                }
                return "";
            }
        });
        xAxis.setLabelRotationAngle(-45);
        
        chart.invalidate();
    }
    
    private void setupChartDouble(LineChart chart, Map<String, Double> dataMap, String label) {
        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        int index = 0;
        
        for (Map.Entry<String, Double> entry : dataMap.entrySet()) {
            entries.add(new Entry(index, entry.getValue().floatValue()));
            dates.add(entry.getKey());
            index++;
        }
        
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(0xFF03DAC5);
        dataSet.setValueTextColor(0xFF000000);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(0xFF03DAC5);
        dataSet.setDrawValues(true);
        
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < dates.size()) {
                    return dates.get(idx);
                }
                return "";
            }
        });
        xAxis.setLabelRotationAngle(-45);
        
        chart.invalidate();
    }
}
