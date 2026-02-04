package com.example.gruber.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDriverChangeRequestAdapter extends RecyclerView.Adapter<AdminDriverChangeRequestAdapter.VH> {

    public interface ChangeRequestActionListener {
        void onApprove(String driverEmail);
        void onReject(String driverEmail);
    }

    private final List<Map<String, Object>> items = new ArrayList<>();
    private final ChangeRequestActionListener listener;

    public AdminDriverChangeRequestAdapter(ChangeRequestActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Map<String, Object>> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_driver_change_request, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Map<String, Object> item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        private final TextView txtDriverEmail;
        private final LinearLayout changesContainer;
        private final MaterialButton btnApprove;
        private final MaterialButton btnReject;
        private final FirebaseFirestore db = FirebaseFirestore.getInstance();

        VH(@NonNull View itemView) {
            super(itemView);
            txtDriverEmail = itemView.findViewById(R.id.txtDriverEmail);
            changesContainer = itemView.findViewById(R.id.changesContainer);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        void bind(Map<String, Object> item, ChangeRequestActionListener listener) {
            String driverEmail = item.get("driverEmail") != null ? item.get("driverEmail").toString() : "";
            String driverUid = item.get("driverUid") != null ? item.get("driverUid").toString() : "";
            txtDriverEmail.setText(driverEmail);

            changesContainer.removeAllViews();

            // Load current data and compare with requested changes
            loadAndCompareData(driverUid, driverEmail, item);

            btnApprove.setOnClickListener(v -> listener.onApprove(driverEmail));
            btnReject.setOnClickListener(v -> listener.onReject(driverEmail));
        }

        private void loadAndCompareData(String driverUid, String driverEmail, Map<String, Object> item) {
            Object userUpdates = item.get("userUpdates");
            Object vehicleUpdates = item.get("vehicleUpdates");

            // Fetch current user data
            db.collection("users")
                    .document(driverUid)
                    .get()
                    .addOnSuccessListener(userSnapshot -> {
                        Map<String, Object> currentUserData = userSnapshot.exists() ? userSnapshot.getData() : new HashMap<>();

                        // Fetch current vehicle data
                        db.collection("vehicles")
                                .document(driverEmail)
                                .get()
                                .addOnSuccessListener(vehicleSnapshot -> {
                                    Map<String, Object> currentVehicleData = vehicleSnapshot.exists() 
                                            ? vehicleSnapshot.getData() : new HashMap<>();

                                    // Now compare and display
                                    if (userUpdates instanceof Map) {
                                        Map<?, ?> userChanges = (Map<?, ?>) userUpdates;
                                        Map<String, String[]> userDifferences = compareData(userChanges, currentUserData);
                                        if (!userDifferences.isEmpty()) {
                                            addSectionHeader(changesContainer, "User Information");
                                            addChangesWithComparison(changesContainer, userDifferences);
                                        }
                                    }

                                    if (vehicleUpdates instanceof Map) {
                                        Map<?, ?> vehicleChanges = (Map<?, ?>) vehicleUpdates;
                                        Map<String, String[]> vehicleDifferences = compareData(vehicleChanges, currentVehicleData);
                                        if (!vehicleDifferences.isEmpty()) {
                                            if (changesContainer.getChildCount() > 0) {
                                                addSpacing(changesContainer);
                                            }
                                            addSectionHeader(changesContainer, "Vehicle Information");
                                            addChangesWithComparison(changesContainer, vehicleDifferences);
                                        }
                                    }

                                    if (changesContainer.getChildCount() == 0) {
                                        TextView noChanges = new TextView(changesContainer.getContext());
                                        noChanges.setText("No differences found");
                                        noChanges.setTextAppearance(changesContainer.getContext(), 
                                                android.R.style.TextAppearance_Material_Body1);
                                        changesContainer.addView(noChanges);
                                    }
                                });
                    });
        }

        private Map<String, String[]> compareData(Map<?, ?> requestedChanges, Map<String, Object> currentData) {
            Map<String, String[]> differences = new HashMap<>();
            
            for (Map.Entry<?, ?> entry : requestedChanges.entrySet()) {
                String key = entry.getKey().toString();
                Object newValue = entry.getValue();
                Object oldValue = currentData.get(key);

                // Compare values
                String newValueStr = newValue != null ? newValue.toString() : "";
                String oldValueStr = oldValue != null ? oldValue.toString() : "";

                if (!newValueStr.equals(oldValueStr)) {
                    differences.put(key, new String[]{oldValueStr, newValueStr});
                }
            }
            
            return differences;
        }

        private void addChangesWithComparison(LinearLayout container, Map<String, String[]> differences) {
            Context ctx = container.getContext();
            
            for (Map.Entry<String, String[]> entry : differences.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                String oldValue = values[0];
                String newValue = values[1];

                // Create container for this field
                LinearLayout fieldContainer = new LinearLayout(ctx);
                fieldContainer.setOrientation(LinearLayout.VERTICAL);
                fieldContainer.setPadding(dpToPx(ctx, 12), dpToPx(ctx, 8), dpToPx(ctx, 12), dpToPx(ctx, 8));
                
                // Get theme colors
                int primaryColor = getThemeColor(ctx, android.R.attr.colorPrimary);
                int backgroundColor = getThemeColor(ctx, android.R.attr.colorBackground);
                
                // Stronger highlighting for changed fields
                fieldContainer.setBackgroundColor(Color.argb(40, Color.red(primaryColor), 
                        Color.green(primaryColor), Color.blue(primaryColor))); // 16% opacity

                // Field name
                TextView fieldName = new TextView(ctx);
                String formattedKey = formatFieldName(key);
                fieldName.setText(formattedKey);
                fieldName.setTextAppearance(ctx, android.R.style.TextAppearance_Material_Body2);
                fieldName.setTypeface(null, Typeface.BOLD);
                fieldContainer.addView(fieldName);

                // Old value
                if (!oldValue.isEmpty()) {
                    TextView oldValueTv = new TextView(ctx);
                    oldValueTv.setText("Current: " + oldValue);
                    oldValueTv.setTextAppearance(ctx, android.R.style.TextAppearance_Material_Small);
                    oldValueTv.setTextColor(Color.GRAY);
                    oldValueTv.setPadding(0, dpToPx(ctx, 2), 0, 0);
                    fieldContainer.addView(oldValueTv);
                }

                // New value
                TextView newValueTv = new TextView(ctx);
                newValueTv.setText("New: " + newValue);
                newValueTv.setTextAppearance(ctx, android.R.style.TextAppearance_Material_Body1);
                newValueTv.setTextColor(primaryColor);
                newValueTv.setTypeface(null, Typeface.BOLD);
                newValueTv.setPadding(0, dpToPx(ctx, 2), 0, 0);
                fieldContainer.addView(newValueTv);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.topMargin = dpToPx(ctx, 4);
                params.bottomMargin = dpToPx(ctx, 4);
                fieldContainer.setLayoutParams(params);

                container.addView(fieldContainer);
            }
        }

        private void addSectionHeader(LinearLayout container, String title) {
            TextView header = new TextView(container.getContext());
            header.setText(title);
            header.setTypeface(null, android.graphics.Typeface.BOLD);
            header.setTextSize(14);
            header.setTextAppearance(container.getContext(), android.R.style.TextAppearance_Material_Body2);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = 8;
            params.bottomMargin = 4;
            header.setLayoutParams(params);
            
            container.addView(header);
        }

        private void addChanges(LinearLayout container, Map<?, ?> changes) {
            for (Map.Entry<?, ?> entry : changes.entrySet()) {
                LinearLayout changeRow = new LinearLayout(container.getContext());
                changeRow.setOrientation(LinearLayout.HORIZONTAL);
                
                // Use theme-aware color with transparency for background
                int surfaceColor = getThemeColor(container.getContext(), android.R.attr.colorBackground);
                changeRow.setBackgroundColor(surfaceColor);
                changeRow.setAlpha(0.4f);
                changeRow.setPadding(12, 8, 12, 8);
                
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.topMargin = 4;
                rowParams.bottomMargin = 4;
                changeRow.setLayoutParams(rowParams);

                // Field name
                TextView fieldName = new TextView(container.getContext());
                fieldName.setText(formatFieldName(entry.getKey().toString()) + ":");
                fieldName.setTypeface(null, android.graphics.Typeface.BOLD);
                fieldName.setTextSize(13);
                fieldName.setTextAppearance(container.getContext(), android.R.style.TextAppearance_Material_Body1);
                fieldName.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1));

                // Field value
                TextView fieldValue = new TextView(container.getContext());
                fieldValue.setText(entry.getValue() != null ? entry.getValue().toString() : "");
                fieldValue.setTextSize(13);
                fieldValue.setTextAppearance(container.getContext(), android.R.style.TextAppearance_Material_Body1);
                fieldValue.setTextColor(getThemeColor(container.getContext(), android.R.attr.colorPrimary));
                fieldValue.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                changeRow.addView(fieldName);
                changeRow.addView(fieldValue);
                container.addView(changeRow);
            }
        }

        private void addSpacing(LinearLayout container) {
            View spacer = new View(container.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 8);
            params.topMargin = 4;
            spacer.setLayoutParams(params);
            container.addView(spacer);
        }

        private String formatFieldName(String fieldName) {
            // Convert camelCase to Title Case
            String formatted = fieldName
                    .replaceAll("([a-z])([A-Z])", "$1 $2")
                    .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");
            
            // Capitalize first letter
            if (formatted.length() > 0) {
                formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
            }
            return formatted;
        }

        private int getThemeColor(android.content.Context context, int attrId) {
            android.util.TypedValue typedValue = new android.util.TypedValue();
            context.getTheme().resolveAttribute(attrId, typedValue, true);
            return typedValue.data;
        }

        private int dpToPx(Context context, int dp) {
            float density = context.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
