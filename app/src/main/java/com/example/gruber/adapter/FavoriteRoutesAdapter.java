package com.example.gruber.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.models.FavoriteRoute;
import com.example.gruber.models.Stop;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRoutesAdapter extends RecyclerView.Adapter<FavoriteRoutesAdapter.ViewHolder> {

    public interface OnRemoveClickListener {
        void onRemove(FavoriteRoute route);
    }

    public interface OnOrderClickListener {
        void onOrder(FavoriteRoute route);
    }

    private final List<FavoriteRoute> items = new ArrayList<>();
    private final OnRemoveClickListener removeClickListener;
    private final OnOrderClickListener orderClickListener;

    public FavoriteRoutesAdapter(OnRemoveClickListener removeClickListener,
                                 OnOrderClickListener orderClickListener) {
        this.removeClickListener = removeClickListener;
        this.orderClickListener = orderClickListener;
    }

    public void submitRoutes(List<FavoriteRoute> routes) {
        items.clear();
        if (routes != null) {
            items.addAll(routes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), removeClickListener, orderClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvStops;
        private final MaterialButton btnRemove;
        private final MaterialButton btnOrder;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvFavoriteRouteTitle);
            tvStops = itemView.findViewById(R.id.tvFavoriteRouteStops);
            btnRemove = itemView.findViewById(R.id.btnRemoveFavoriteRoute);
            btnOrder = itemView.findViewById(R.id.btnOrderFavoriteRoute);
        }

        void bind(FavoriteRoute route, OnRemoveClickListener removeClickListener,
                  OnOrderClickListener orderClickListener) {
            String title = route.getDescription();
            tvTitle.setText(title != null && !title.trim().isEmpty() ? title : itemView.getContext().getString(R.string.favorite_routes));

            tvStops.setText(buildStopsText(route.getStops()));

            btnRemove.setOnClickListener(v -> removeClickListener.onRemove(route));
            btnOrder.setOnClickListener(v -> orderClickListener.onOrder(route));
        }

        private String buildStopsText(List<Stop> stops) {
            if (stops == null || stops.isEmpty()) {
                return "";
            }

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < stops.size(); i++) {
                Stop stop = stops.get(i);
                if (stop == null || stop.getAddress() == null) continue;
                if (builder.length() > 0) {
                    builder.append(" → ");
                }
                builder.append(stop.getAddress());
            }
            return builder.toString();
        }
    }
}
