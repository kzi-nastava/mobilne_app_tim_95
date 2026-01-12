package com.example.gruber.fragment; // change to your package

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gruber.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VehiclePricingFragment extends Fragment {

    private FirebaseFirestore db;

    // Cache type -> docId (so updates are direct)
    private final Map<String, String> typeToDocId = new HashMap<>();

    private PriceRow standard, luxury, van;

    public VehiclePricingFragment() {
        super(R.layout.fragment_vehicle_pricing); // your layout name
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        standard = new PriceRow(
                "Standard",
                view.findViewById(R.id.tvPriceStandard),
                view.findViewById(R.id.btnEditStandard),
                view.findViewById(R.id.standard_edit),
                view.findViewById(R.id.etStandard),
                view.findViewById(R.id.btnSaveStandard),
                view.findViewById(R.id.btnCancelStandard)
        );

        luxury = new PriceRow(
                "Luxury",
                view.findViewById(R.id.tvPriceLuxury),
                view.findViewById(R.id.btnEditLuxury),
                view.findViewById(R.id.luxury_edit),
                view.findViewById(R.id.etLuxury),
                view.findViewById(R.id.btnSaveLuxury),
                view.findViewById(R.id.btnCancelLuxury)
        );

        van = new PriceRow(
                "Van",
                view.findViewById(R.id.tvPriceVan),
                view.findViewById(R.id.btnEditVan),
                view.findViewById(R.id.van_edit),
                view.findViewById(R.id.etVan),
                view.findViewById(R.id.btnSaveVan),
                view.findViewById(R.id.btnCancelVan)
        );

        bindRow(standard);
        bindRow(luxury);
        bindRow(van);

        loadPrices();
    }

    private void loadPrices() {
        standard.tvPrice.setText("—");
        luxury.tvPrice.setText("—");
        van.tvPrice.setText("—");

        db.collection("vehicleType")
                .get()
                .addOnSuccessListener(this::applyPrices)
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed to load prices: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void applyPrices(QuerySnapshot query) {
        Map<String, Double> prices = new HashMap<>();

        for (DocumentSnapshot doc : query.getDocuments()) {
            String type = doc.getString("type");
            if (type == null) continue;

            type = type.trim(); // important if you have extra spaces in DB
            typeToDocId.put(type, doc.getId());

            Double price = readPriceAsDouble(doc);
            if (price != null) {
                prices.put(type, price);
            }
        }

        setIfPresent(standard, prices);
        setIfPresent(luxury, prices);
        setIfPresent(van, prices);
    }

    // Handles Firestore price being Double, Long, or String
    private Double readPriceAsDouble(DocumentSnapshot doc) {
        Object raw = doc.get("price");
        if (raw == null) return null;

        if (raw instanceof Number) {
            return ((Number) raw).doubleValue();
        }

        if (raw instanceof String) {
            String s = ((String) raw).trim().replace(",", ".");
            if (s.isEmpty()) return null;
            try {
                return Double.parseDouble(s);
            } catch (Exception ignored) {
                return null;
            }
        }

        return null;
    }

    private void setIfPresent(PriceRow row, Map<String, Double> prices) {
        Double p = prices.get(row.type);
        row.tvPrice.setText(p != null ? formatPrice(p) : "—");
    }

    private void bindRow(PriceRow row) {
        row.btnEdit.setOnClickListener(v -> {
            // only open this row, close others
            closeRow(standard);
            closeRow(luxury);
            closeRow(van);

            // preload input from text
            row.etPrice.setText(stripCurrency(row.tvPrice.getText().toString()));
            row.editArea.setVisibility(View.VISIBLE);
            row.etPrice.requestFocus();
            if (row.etPrice.getText() != null) {
                row.etPrice.setSelection(row.etPrice.getText().length());
            }
        });

        row.btnCancel.setOnClickListener(v -> closeRow(row));

        row.btnSave.setOnClickListener(v -> {
            String raw = row.etPrice.getText() != null ? row.etPrice.getText().toString().trim() : "";
            if (TextUtils.isEmpty(raw)) {
                row.etPrice.setError("Enter price");
                return;
            }

            double newPrice;
            try {
                newPrice = Double.parseDouble(raw.replace(",", "."));
            } catch (Exception ex) {
                row.etPrice.setError("Invalid number");
                return;
            }

            setRowEnabled(row, false);

            updatePrice(row.type, newPrice)
                    .addOnSuccessListener(unused -> {
                        row.tvPrice.setText(formatPrice(newPrice));
                        closeRow(row);
                        setRowEnabled(row, true);
                        Toast.makeText(requireContext(), row.type + " saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        setRowEnabled(row, true);
                        Toast.makeText(requireContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void closeRow(PriceRow row) {
        row.editArea.setVisibility(View.GONE);
        row.etPrice.setError(null);
    }

    private void setRowEnabled(PriceRow row, boolean enabled) {
        row.btnEdit.setEnabled(enabled);
        row.btnSave.setEnabled(enabled);
        row.btnCancel.setEnabled(enabled);
        row.etPrice.setEnabled(enabled);
    }

    private Task<Void> updatePrice(String type, double price) {
        String docId = typeToDocId.get(type);

        if (docId != null) {
            return db.collection("vehicleType")
                    .document(docId)
                    .update("price", price);
        }

        // fallback: find doc by type then update
        return db.collection("vehicleType")
                .whereEqualTo("type", type)
                .limit(1)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();

                    QuerySnapshot q = task.getResult();
                    if (q == null || q.isEmpty()) {
                        throw new IllegalStateException("No doc found for type=" + type);
                    }

                    DocumentSnapshot doc = q.getDocuments().get(0);
                    typeToDocId.put(type, doc.getId());

                    return db.collection("vehicleType")
                            .document(doc.getId())
                            .update("price", price);
                });
    }

    private String formatPrice(double v) {
        // RSD typically no decimals, but keep it safe:
        if (Math.floor(v) == v) {
            return String.format(Locale.US, "%d RSD", (long) v);
        }
        return String.format(Locale.US, "%.2f RSD", v);
    }

    private String stripCurrency(String s) {
        return s.replaceAll("[^0-9.,]", "").trim();
    }

    private static class PriceRow {
        final String type;
        final TextView tvPrice;
        final FloatingActionButton btnEdit;
        final View editArea;
        final EditText etPrice;
        final Button btnSave;
        final Button btnCancel;

        PriceRow(String type,
                 TextView tvPrice,
                 FloatingActionButton btnEdit,
                 View editArea,
                 EditText etPrice,
                 Button btnSave,
                 Button btnCancel) {
            this.type = type;
            this.tvPrice = tvPrice;
            this.btnEdit = btnEdit;
            this.editArea = editArea;
            this.etPrice = etPrice;
            this.btnSave = btnSave;
            this.btnCancel = btnCancel;
        }
    }
}
