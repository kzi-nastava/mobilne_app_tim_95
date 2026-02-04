package com.example.gruber.viewModels;

import androidx.lifecycle.MutableLiveData;
import com.example.gruber.models.User;
import com.example.gruber.services.UserService;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DriverViewModel extends AccountViewModel {

    private final MutableLiveData<Integer> activeHoursLast24 = new MutableLiveData<>();
    private final MutableLiveData<String> vehicleModel = new MutableLiveData<>();
    private final MutableLiveData<String> vehiclePlate = new MutableLiveData<>();
    private final MutableLiveData<String> vehicleType = new MutableLiveData<>();
    private final MutableLiveData<Integer> numberOfSeats = new MutableLiveData<>();
    private final MutableLiveData<Boolean> allowsBabies = new MutableLiveData<>();
    private final MutableLiveData<Boolean> allowsPets = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasPendingChangeRequest = new MutableLiveData<>(false);

    @Inject
    public DriverViewModel(UserService userService) {
        super(userService);
    }

    public int getActiveHoursLast24() {
        return activeHoursLast24.getValue() != null ? activeHoursLast24.getValue().intValue() : 0;
    }

    public void setActiveHoursLast24(int activeHoursLast24) {
        this.activeHoursLast24.setValue(Integer.valueOf(activeHoursLast24));
    }

    // Override parent methods for compatibility
    @Override
    public MutableLiveData<Integer> getActiveHours() {
        return activeHoursLast24;
    }

    @Override
    public void setActiveHours(Integer activeHours) {
        if (activeHours != null) {
            this.activeHoursLast24.setValue(activeHours);
        }
    }

    @Override
    public MutableLiveData<String> getVehicleModel() {
        return vehicleModel;
    }

    @Override
    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel.setValue(vehicleModel);
    }

    @Override
    public MutableLiveData<String> getVehiclePlate() {
        return vehiclePlate;
    }

    @Override
    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate.setValue(vehiclePlate);
    }

    public MutableLiveData<String> getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType.setValue(vehicleType);
    }

    public MutableLiveData<Integer> getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(Integer seats) {
        if (seats != null) {
            this.numberOfSeats.setValue(seats);
        }
    }

    public MutableLiveData<Boolean> getAllowsBabies() {
        return allowsBabies;
    }

    public void setAllowsBabies(Boolean value) {
        this.allowsBabies.setValue(value);
    }

    public MutableLiveData<Boolean> getAllowsPets() {
        return allowsPets;
    }

    public void setAllowsPets(Boolean value) {
        this.allowsPets.setValue(value);
    }

    public MutableLiveData<Boolean> getHasPendingChangeRequest() {
        return hasPendingChangeRequest;
    }

    public void setHasPendingChangeRequest(Boolean value) {
        this.hasPendingChangeRequest.setValue(value);
    }

    @Override
    public User toUser() {
        return new User(
                getFirstName().getValue(),
                getLastName().getValue(),
                getEmail().getValue(),
                getPhone().getValue(),
                getRole().getValue(),
                getImage().getValue(),
                vehicleModel.getValue(),
                vehiclePlate.getValue(),
                0);
    }
}
