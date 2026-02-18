package com.example.gruber.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import com.example.gruber.models.Stop;

import java.util.ArrayList;
import java.util.List;

public class AddressAutoCompleteAdapter extends ArrayAdapter<Stop> {

    public AddressAutoCompleteAdapter(@NonNull Context context) {
        super(context, android.R.layout.simple_dropdown_item_1line);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                results.values = getCount() > 0 ? new ArrayList<>(getAllItems()) : new ArrayList<>();
                results.count = getCount();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
    }

    private List<Stop> getAllItems() {
        List<Stop> items = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            items.add(getItem(i));
        }
        return items;
    }
}
