package com.example.gruber.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gruber.R;
import com.example.gruber.models.Stop;
import java.util.ArrayList;
import java.util.List;

public class StopAdapter extends RecyclerView.Adapter<StopAdapter.StopViewHolder> {

    private List<Stop> stops = new ArrayList<>();
    private OnStopClickListener onStopClickListener;

    public interface OnStopClickListener {
        void onStopClick(Stop stop);
    }

    public StopAdapter(OnStopClickListener onStopClickListener) {
        this.onStopClickListener = onStopClickListener;
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new StopViewHolder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        holder.bind(stops.get(position), position + 1, onStopClickListener);
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    public void submitList(List<Stop> newStops) {
        if (newStops != null) {
            this.stops = new ArrayList<>(newStops);
        } else {
            this.stops = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    static class StopViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStopNumber;
        private TextView tvStopAddress;
        private ImageButton btnRemoveStop;

        StopViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_stop, parent, false));
            tvStopNumber = itemView.findViewById(R.id.tv_stop_number);
            tvStopAddress = itemView.findViewById(R.id.tv_stop_address);
            btnRemoveStop = itemView.findViewById(R.id.btn_remove_stop);
        }

        void bind(Stop stop, int stopNumber, OnStopClickListener onStopClickListener) {
            tvStopNumber.setText("Stop " + stopNumber);
            tvStopAddress.setText(stop.getAddress());
            btnRemoveStop.setOnClickListener(v -> onStopClickListener.onStopClick(stop));
        }
    }
}
