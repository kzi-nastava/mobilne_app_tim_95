package com.example.gruber.fragment.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.adapter.FavoriteRoutesAdapter;
import com.example.gruber.fragment.RideOrderDialogFragment;
import com.example.gruber.models.FavoriteRoute;
import com.example.gruber.viewModels.RideViewModel;
import android.widget.ImageButton;

import java.util.List;

public class FavoriteRoutesFragment extends Fragment {

    private RideViewModel rideViewModel;
    private SessionManager sessionManager;
    private FavoriteRoutesAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyMessage;

    public FavoriteRoutesFragment() {
        super(R.layout.fragment_favorite_routes);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite_routes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rideViewModel = new ViewModelProvider(requireActivity()).get(RideViewModel.class);
        sessionManager = new SessionManager(requireContext());

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_favoriteRoutesFragment_to_settingsFragment));

        recyclerView = view.findViewById(R.id.recyclerFavoriteRoutes);
        emptyMessage = view.findViewById(R.id.emptyFavoriteRoutesMessage);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FavoriteRoutesAdapter(this::removeRoute, this::orderRoute);
        recyclerView.setAdapter(adapter);

        loadRoutes();
    }

    private void loadRoutes() {
        String email = sessionManager.getUserEmail();
        rideViewModel.getFavoriteRoutes(email, this::renderRoutes);
    }

    private void renderRoutes(List<FavoriteRoute> routes) {
        adapter.submitRoutes(routes);
        if (routes == null || routes.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyMessage.setVisibility(View.GONE);
        }
    }

    private void removeRoute(FavoriteRoute route) {
        rideViewModel.deleteFavoriteRoute(route.getRouteId(), success -> {
            if (success) {
                Toast.makeText(requireContext(), R.string.favorite_route_removed, Toast.LENGTH_SHORT).show();
                loadRoutes();
            } else {
                Toast.makeText(requireContext(), R.string.favorite_route_remove_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void orderRoute(FavoriteRoute route) {
        if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_stops_added, Toast.LENGTH_SHORT).show();
            return;
        }

        rideViewModel.prefillFromStops(route.getStops());
        new RideOrderDialogFragment()
                .show(getParentFragmentManager(), "BookRideDialogFromFavorite");
    }
}
