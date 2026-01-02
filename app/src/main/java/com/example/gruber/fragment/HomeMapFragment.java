package com.example.gruber.fragment;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.api.IMapController;

public class HomeMapFragment extends Fragment {

    private MapView map;

    public HomeMapFragment() {
        super(R.layout.fragment_home_map);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // IMPORTANT: set user agent / config
        Configuration.getInstance().load(
                requireContext(),
                PreferenceManager.getDefaultSharedPreferences(requireContext())
        );

        map = view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true); // zoom/pan gestures
        map.setMinZoomLevel(4.0);
        map.setMaxZoomLevel(20.0);

        IMapController controller = map.getController();
        controller.setZoom(14.0);

        // Novi Sad default (change later to GPS)
        GeoPoint noviSad = new GeoPoint(45.2671, 19.8335);
        controller.setCenter(noviSad);

//        view.findViewById(R.id.btnBookRide).setOnClickListener(v -> {
//            // For now just navigate to a placeholder "BookRideFragment"
//            NavHostFragment.findNavController(HomeMapFragment.this)
//                    .navigate(R.id.action_homeMapFragment_to_bookRideFragment);
//        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        if (map != null) map.onPause();
        super.onPause();
    }
}
